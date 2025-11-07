package io.charlie.web.oj.modular.task.library.handle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.task.library.dto.Library;
import io.charlie.web.oj.modular.task.library.mq.LibraryQueueProperties;
import io.charlie.web.oj.utils.similarity.utils.CodeTokenUtil;
import io.charlie.web.oj.utils.similarity.utils.TokenDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 * @description TODO
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryHandleMessage {
    private final RabbitTemplate rabbitTemplate;
    private final LibraryQueueProperties libraryQueueProperties;
    private final DataLibraryMapper dataLibraryMapper;

    private final CodeTokenUtil codeTokenUtil;

    public void sendLibrary(Library dataSubmit) {
        rabbitTemplate.convertAndSend(
                libraryQueueProperties.getCommon().getExchange(),
                libraryQueueProperties.getCommon().getRoutingKey(),
                dataSubmit
        );
        log.debug("发送样本库消息成功");
    }

    @Transactional
    @RabbitListener(queues = "${oj.mq.library.common.queue}", concurrency = "1")
    public void receiveJudge(Library submit) {
        TokenDetail tokensDetail = codeTokenUtil.getCodeTokensDetail(submit.getLanguage().toLowerCase(), submit.getCode());

        LambdaQueryWrapper<DataLibrary> queryWrapper = new LambdaQueryWrapper<DataLibrary>()
                .eq(DataLibrary::getUserId, submit.getUserId())
                .eq(DataLibrary::getModuleType, submit.getModuleType())
                .eq(DataLibrary::getModuleId, submit.getModuleId())
                .eq(DataLibrary::getProblemId, submit.getProblemId())
                .eq(DataLibrary::getLanguage, submit.getLanguage())
                .eq(DataLibrary::getModuleType, submit.getModuleType());

        if (dataLibraryMapper.exists(queryWrapper)) {
            log.debug("存在记录，执行更新");
            DataLibrary library = dataLibraryMapper.selectOne(queryWrapper);
            library.setSubmitId(submit.getSubmitId());
            library.setSubmitTime(new Date());

            // 如果代码长度和原本的代码长度不一致，则更新代码
            if (!library.getCodeLength().equals(submit.getCode().length())) {
                library.setCodeLength(submit.getCode().length());
                library.setCode(submit.getCode());
                library.setCodeToken(tokensDetail.getTokens());
                library.setCodeTokenName(tokensDetail.getTokenNames());
                library.setCodeTokenTexts(tokensDetail.getTokenTexts());
                library.setAccessCount(0);// 重置访问次数
            }

            dataLibraryMapper.updateById(library);
        } else {
            log.debug("不存在记录，创建新记录");

            DataLibrary library = new DataLibrary();

            library.setUserId(submit.getUserId());

            library.setModuleType(submit.getModuleType());
            library.setModuleId(submit.getModuleId());

            library.setProblemId(submit.getProblemId());
            library.setLanguage(submit.getLanguage());
            library.setSubmitId(submit.getSubmitId());
            library.setSubmitTime(new Date());
            library.setCode(submit.getCode());

            library.setCodeToken(tokensDetail.getTokens());
            library.setCodeTokenName(tokensDetail.getTokenNames());
            library.setCodeTokenTexts(tokensDetail.getTokenTexts());

            library.setCodeLength(submit.getCode().length());
            library.setAccessCount(0);

            dataLibraryMapper.insert(library);
        }

        log.debug("样本库处理完成");
    }
}
