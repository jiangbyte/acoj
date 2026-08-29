package io.charlie.web.modular.task.library.handle;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.charlie.web.modular.data.library.entity.DataLibrary;
import io.charlie.web.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.modular.task.library.dto.Library;
import io.charlie.web.modular.task.library.mq.LibraryQueueProperties;
import io.charlie.web.utils.similarity.utils.CodeTokenUtil;
import io.charlie.web.utils.similarity.utils.TokenDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 * @description 样本库消息处理（主库 upsert，按业务 UK）
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

    @DS("master")
    @RabbitListener(queues = "${oj.mq.library.common.queue}", concurrency = "1")
    public void receiveJudge(Library submit) {
        try {
            processLibraryRecord(submit);
            log.debug("样本库处理完成, userId: {}, problemId: {}", submit.getUserId(), submit.getProblemId());
        } catch (Exception e) {
            log.error("处理样本库失败, userId: {}, problemId: {}, submitId: {}",
                    submit.getUserId(), submit.getProblemId(), submit.getSubmitId(), e);
            throw new RuntimeException("处理样本库失败", e);
        }
    }

    /**
     * 依赖唯一索引 uk_user_module_problem_lang。
     * 先按业务 UK update；0 行再 insert；撞 UK 再 update。全程主库。
     */
    private void processLibraryRecord(Library submit) {
        TokenDetail tokensDetail = codeTokenUtil.getCodeTokensDetail(
                submit.getLanguage().toLowerCase(), submit.getCode()
        );
        Date now = new Date();
        int codeLength = submit.getCode().length();

        int updated = updateByBusinessKey(submit, tokensDetail, codeLength, now);
        if (updated > 0) {
            return;
        }

        DataLibrary library = new DataLibrary();
        library.setUserId(submit.getUserId());
        library.setModuleType(submit.getModuleType());
        library.setModuleId(submit.getModuleId());
        library.setProblemId(submit.getProblemId());
        library.setLanguage(submit.getLanguage());
        library.setSubmitId(submit.getSubmitId());
        library.setSubmitTime(now);
        library.setCode(submit.getCode());
        library.setCodeLength(codeLength);
        library.setCodeToken(tokensDetail.getTokens());
        library.setCodeTokenName(tokensDetail.getTokenNames());
        library.setCodeTokenTexts(tokensDetail.getTokenTexts());
        library.setAccessCount(0);

        try {
            dataLibraryMapper.insert(library);
        } catch (Exception e) {
            if (isDuplicateKeyException(e)) {
                updateByBusinessKey(submit, tokensDetail, codeLength, now);
            } else {
                throw e;
            }
        }
    }

    /**
     * 按业务 UK 直接 update，避免 select-then-update 空窗。
     * 使用 entity + wrapper，保证 JSON 字段 TypeHandler 生效。
     */
    private int updateByBusinessKey(Library submit, TokenDetail tokensDetail, int codeLength, Date now) {
        DataLibrary patch = new DataLibrary();
        patch.setSubmitId(submit.getSubmitId());
        patch.setSubmitTime(now);
        patch.setCode(submit.getCode());
        patch.setCodeLength(codeLength);
        patch.setCodeToken(tokensDetail.getTokens());
        patch.setCodeTokenName(tokensDetail.getTokenNames());
        patch.setCodeTokenTexts(tokensDetail.getTokenTexts());
        patch.setAccessCount(0);

        return dataLibraryMapper.update(patch, new LambdaUpdateWrapper<DataLibrary>()
                .eq(DataLibrary::getUserId, submit.getUserId())
                .eq(DataLibrary::getModuleType, submit.getModuleType())
                .eq(DataLibrary::getModuleId, submit.getModuleId())
                .eq(DataLibrary::getProblemId, submit.getProblemId())
                .eq(DataLibrary::getLanguage, submit.getLanguage())
        );
    }

    private boolean isDuplicateKeyException(Exception e) {
        Throwable cur = e;
        while (cur != null) {
            String message = cur.getMessage();
            if (message != null && (message.contains("Duplicate entry") || message.contains("1062"))) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }
}
