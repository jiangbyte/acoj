package io.charlie.web.oj.modular.task.library.handle;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.oj.modular.data.library.entity.DataLibrary;
import io.charlie.web.oj.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.task.library.mq.LibraryQueueProperties;
import io.charlie.web.oj.utils.similarity.utils.CodeTokenUtil;
import io.charlie.web.oj.utils.similarity.utils.TokenDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    public void sendLibrary(DataSubmit dataSubmit) {
        log.info("发送消息：{}", JSONUtil.toJsonStr(dataSubmit));
        rabbitTemplate.convertAndSend(
                libraryQueueProperties.getCommon().getExchange(),
                libraryQueueProperties.getCommon().getRoutingKey(),
                dataSubmit
        );
        log.info("发送消息成功");
    }

    @Transactional
    @RabbitListener(queues = "${oj.mq.library.common.queue}", concurrency = "5-10")
    public void receiveJudge(DataSubmit submit) {
        TokenDetail tokensDetail = codeTokenUtil.getCodeTokensDetail(submit.getLanguage().toLowerCase(), submit.getCode());
        log.info("代码库克隆检测 -> 语言 {} 获取代码Token {}", submit.getLanguage(), JSONUtil.toJsonStr(tokensDetail.getTokens()));

        if (submit.getIsSet()) {
            LambdaQueryWrapper<DataLibrary> queryWrapper = new LambdaQueryWrapper<DataLibrary>()
                    .eq(DataLibrary::getUserId, submit.getUserId())
                    .eq(DataLibrary::getSetId, submit.getSetId())
                    .eq(DataLibrary::getProblemId, submit.getProblemId())
                    .eq(DataLibrary::getLanguage, submit.getLanguage())
                    .eq(DataLibrary::getIsSet, Boolean.TRUE);

            if (dataLibraryMapper.exists(queryWrapper)) {
                log.info("存在记录，执行更新");
                DataLibrary library = dataLibraryMapper.selectOne(queryWrapper);
                library.setSubmitId(submit.getId());
                library.setSubmitTime(submit.getCreateTime());

                library.setCodeLength(submit.getCodeLength());
                library.setCode(submit.getCode());

                library.setCodeToken(tokensDetail.getTokens());
                library.setCodeTokenName(tokensDetail.getTokenNames());
                library.setCodeTokenTexts(tokensDetail.getTokenTexts());

                library.setAccessCount(0);// 重置访问次数

                dataLibraryMapper.updateById(library);
            } else {
                log.info("不存在记录，创建新记录");

                DataLibrary library = new DataLibrary();

                library.setUserId(submit.getUserId());
                library.setSetId(submit.getIsSet() ? submit.getSetId() : null);
                library.setProblemId(submit.getProblemId());
                library.setLanguage(submit.getLanguage());
                library.setSubmitId(submit.getId());
                library.setSubmitTime(submit.getCreateTime());
                library.setCode(submit.getCode());

                library.setCodeToken(tokensDetail.getTokens());
                library.setCodeTokenName(tokensDetail.getTokenNames());
                library.setCodeTokenTexts(tokensDetail.getTokenTexts());

                library.setCodeLength(submit.getCodeLength());
                library.setIsSet(submit.getIsSet());
                library.setAccessCount(0);

                dataLibraryMapper.insert(library);
            }
        } else {
            LambdaQueryWrapper<DataLibrary> queryWrapper = new LambdaQueryWrapper<DataLibrary>()
                    .eq(DataLibrary::getUserId, submit.getUserId())
                    .eq(DataLibrary::getProblemId, submit.getProblemId())
                    .eq(DataLibrary::getLanguage, submit.getLanguage())
                    .eq(DataLibrary::getIsSet, Boolean.FALSE);

            if (dataLibraryMapper.exists(queryWrapper)) {
                log.info("存在记录，执行更新");
                DataLibrary library = dataLibraryMapper.selectOne(queryWrapper);
                library.setSubmitId(submit.getId());
                library.setSubmitTime(submit.getCreateTime());

                library.setCodeLength(submit.getCodeLength());
                library.setCode(submit.getCode());

                library.setCodeToken(tokensDetail.getTokens());
                library.setCodeTokenName(tokensDetail.getTokenNames());
                library.setCodeTokenTexts(tokensDetail.getTokenTexts());

                library.setAccessCount(0);// 重置访问次数

                dataLibraryMapper.updateById(library);
            } else {
                log.info("不存在记录，创建新记录");

                DataLibrary library = new DataLibrary();

                library.setUserId(submit.getUserId());
                library.setSetId(submit.getIsSet() ? submit.getSetId() : null);
                library.setProblemId(submit.getProblemId());
                library.setLanguage(submit.getLanguage());
                library.setSubmitId(submit.getId());
                library.setSubmitTime(submit.getCreateTime());
                library.setCode(submit.getCode());

                library.setCodeToken(tokensDetail.getTokens());
                library.setCodeTokenName(tokensDetail.getTokenNames());
                library.setCodeTokenTexts(tokensDetail.getTokenTexts());

                library.setCodeLength(submit.getCodeLength());
                library.setIsSet(submit.getIsSet());
                library.setAccessCount(0);

                dataLibraryMapper.insert(library);
            }
        }
    }
}
