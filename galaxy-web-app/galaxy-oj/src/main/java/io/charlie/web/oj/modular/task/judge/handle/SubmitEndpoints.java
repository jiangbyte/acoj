package io.charlie.web.oj.modular.task.judge.handle;

import cn.hutool.json.JSONUtil;
import io.charlie.web.oj.modular.data.submit.param.DataSubmitExeParam;
import io.charlie.web.oj.modular.data.submit.service.DataSubmitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 24/09/2025
 * @description websocket 提交端点
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SubmitEndpoints {
    private final DataSubmitService dataSubmitService;

//    @MessageMapping("/judge/status/{submitId}")
//    public void handleJudgeStatusMessage(String message,
//                                         @Header("Authorization") String token,
//                                         @DestinationVariable String submitId) {
//        log.info("收到客户端消息 - submitId: {}, token: {}, 内容: {}", submitId, token, message);
//        DataSubmitExeParam bean = JSONUtil.toBean(message, DataSubmitExeParam.class);
//        if (bean.getSetId() == null) {
//            log.info("题目提交");
////            System.out.println(JSONUtil.toJsonPrettyStr(bean));
//            dataSubmitService.handleProblemSubmit(bean);
//        } else {
//            log.info("题集提交");
////            System.out.println(JSONUtil.toJsonPrettyStr(bean));
//            dataSubmitService.handleSetSubmit(bean);
//        }
//    }
//
//        // 处理客户端订阅事件（可选）
//    @SubscribeMapping("/topic/judge/status/{submitId}")
//    public String handleSubscription(@DestinationVariable String submitId) {
//        log.info("客户端订阅了主题: /topic/judge/status/{}", submitId);
//        return "订阅成功，开始接收判题状态更新";
//    }
}
