package github.jiangbyte.io.oj.modules.judge.mq;

import com.rabbitmq.client.Channel;
import github.jiangbyte.io.oj.modules.judge.schedule.JudgeWorkerService;
import github.jiangbyte.io.oj.modules.problemdryrun.service.OjProblemDryRunService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 判题主队列消费者：手动 ack，编排提交判题 / 管理端试跑。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OjJudgeConsumer {

    private final ObjectMapper objectMapper;
    private final JudgeWorkerService judgeWorkerService;
    private final OjProblemDryRunService ojProblemDryRunService;
    private final OjJudgePublisher ojJudgePublisher;

    @RabbitListener(
            queues = "#{@ojJudgeWorkQueue.name}",
            containerFactory = OjJudgeMqConfig.LISTENER_FACTORY,
            ackMode = "MANUAL")
    public void onMessage(
            Message message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        OjJudgeMessage payload = null;
        try {
            payload = parse(message);
            if (payload == null || !StringUtils.hasText(payload.jobId())) {
                log.warn("oj judge poison message: empty job id");
                if (payload != null) {
                    safeDlq(payload);
                }
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (payload.isDryRun()) {
                ojProblemDryRunService.processQueued(payload);
            } else {
                judgeWorkerService.process(payload);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("oj judge consume failed jobType={} jobId={}",
                    payload == null ? null : payload.jobType(),
                    payload == null ? null : payload.jobId(), ex);
            if (payload != null && StringUtils.hasText(payload.jobId())) {
                try {
                    ojJudgePublisher.publishDlq(payload);
                } catch (Exception dlqEx) {
                    log.warn("publish dlq failed: {}", dlqEx.toString());
                }
            }
            channel.basicAck(deliveryTag, false);
        }
    }

    private OjJudgeMessage parse(Message message) {
        try {
            JsonNode root = objectMapper.readTree(message.getBody());
            String jobType = text(root, "job_type", "jobType");
            if (!StringUtils.hasText(jobType)) {
                jobType = OjJudgeMessage.TYPE_SUBMISSION;
            }
            String submissionId = text(root, "submission_id", "submissionId");
            String dryRunId = text(root, "dry_run_id", "dryRunId");
            Boolean stopOnFirstError = null;
            if (root.has("stop_on_first_error") && !root.get("stop_on_first_error").isNull()) {
                stopOnFirstError = root.get("stop_on_first_error").asBoolean();
            } else if (root.has("stopOnFirstError") && !root.get("stopOnFirstError").isNull()) {
                stopOnFirstError = root.get("stopOnFirstError").asBoolean();
            }
            String requestId = text(root, "request_id", "requestId");
            long enqueueAt = root.path("enqueue_at").asLong(root.path("enqueueAt").asLong(System.currentTimeMillis()));
            String reason = text(root, "reason");
            return new OjJudgeMessage(
                    jobType, submissionId, dryRunId, stopOnFirstError, requestId, enqueueAt, reason);
        } catch (Exception ex) {
            log.warn("parse oj judge message failed body={}",
                    new String(message.getBody(), StandardCharsets.UTF_8), ex);
            return null;
        }
    }

    private static String text(JsonNode root, String... names) {
        for (String name : names) {
            JsonNode n = root.get(name);
            if (n != null && !n.isNull() && StringUtils.hasText(n.asText())) {
                return n.asText();
            }
        }
        return null;
    }

    private void safeDlq(OjJudgeMessage payload) {
        try {
            ojJudgePublisher.publishDlq(payload);
        } catch (Exception ex) {
            log.warn("publish dlq failed: {}", ex.toString());
        }
    }
}
