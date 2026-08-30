package github.jiangbyte.io.common.notify.mail;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.notify.NotifyConfigSource;
import github.jiangbyte.io.common.notify.cloud.AliyunRpcClient;
import github.jiangbyte.io.common.notify.cloud.TencentApiClient;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * 通知侧邮件发送门面：按运行时配置选择 SMTP 等实现并发送。
 *
 * Author: Charlie
 */
public class MailSenderFacade {

    private static final Logger log = LoggerFactory.getLogger(MailSenderFacade.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NotifyConfigSource config;

    public MailSenderFacade(NotifyConfigSource config) {
        this.config = config;
    }

    /** 按配置发送邮件。 */
    public void send(String to, String subject, String body) {
        String engine = config.get("DEFAULT_EMAIL_ENGINE", "LOCAL").trim().toUpperCase(Locale.ROOT);
        switch (engine) {
            case "LOCAL" -> sendLocal(to, subject, body);
            case "ALIYUN" -> sendAliyun(to, subject, body);
            case "TENCENT" -> sendTencent(to, subject, body);
            default -> throw new BizException("Unsupported email engine: " + engine);
        }
    }

    /** 按模板发送邮件。 */
    public void sendTemplated(String scene, String to, Map<String, ?> vars) {
        String key = "MAIL_TEMPLATE_" + scene;
        Map<String, Object> tmpl = readJsonObject(config.get(key, ""), key);
        String subject = render(stringVal(tmpl.get("subject"), ""), vars);
        String body = render(stringVal(tmpl.get("body"), ""), vars);
        if (!StringUtils.hasText(subject) && !StringUtils.hasText(body)) {
            throw new BizException("Mail template missing: " + key);
        }
        send(to, subject, body);
    }

    private void sendLocal(String to, String subject, String body) {
        String host = requireTrimmed("MAIL_LOCAL_HOST", "邮件引擎未配置: MAIL_LOCAL_HOST / Mail engine not configured: MAIL_LOCAL_HOST");
        int port = config.getInt("MAIL_LOCAL_PORT", 465);
        String fromEmail = requireTrimmed(
                "MAIL_LOCAL_FROM_EMAIL",
                "邮件引擎未配置: MAIL_LOCAL_FROM_EMAIL / Mail engine not configured: MAIL_LOCAL_FROM_EMAIL");
        String fromName = trim(config.get("MAIL_LOCAL_FROM_NAME", ""));
        boolean useSsl = config.getBoolean("MAIL_LOCAL_USE_SSL", false);
        boolean useStartTls = config.getBoolean("MAIL_LOCAL_USE_STARTTLS", true);
        String username = trim(config.get("MAIL_LOCAL_USERNAME", ""));
        String password = config.get("MAIL_LOCAL_PASSWORD", "");
        boolean authRequired = config.getBoolean("MAIL_LOCAL_AUTH_REQUIRED", StringUtils.hasText(username));

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", String.valueOf(authRequired));
        if (useSsl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));
        } else if (useStartTls) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        Session session;
        if (authRequired && StringUtils.hasText(username)) {
            String finalPassword = password == null ? "" : password;
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, finalPassword);
                }
            });
        } else {
            session = Session.getInstance(props);
        }

        try {
            MimeMessage message = new MimeMessage(session);
            if (StringUtils.hasText(fromName)) {
                message.setFrom(new InternetAddress(fromEmail, fromName));
            } else {
                message.setFrom(new InternetAddress(fromEmail));
            }
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject == null ? "" : subject, "UTF-8");
            message.setText(body == null ? "" : body, "UTF-8");
            Transport.send(message);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Local SMTP mail failed", ex);
            throw new BizException("Failed to send email");
        }
    }

    private void sendAliyun(String to, String subject, String body) {
        String accessKeyId = require("MAIL_ALIYUN_ACCESS_KEY_ID");
        String accessKeySecret = require("MAIL_ALIYUN_ACCESS_KEY_SECRET");
        String accountName = require("MAIL_ALIYUN_ACCOUNT_NAME");
        String fromAlias = trim(config.get("MAIL_LOCAL_FROM_NAME", ""));
        Map<String, String> params = new HashMap<>();
        params.put("AccountName", accountName);
        params.put("AddressType", "1");
        params.put("ReplyToAddress", "false");
        params.put("ToAddress", to);
        params.put("Subject", subject == null ? "" : subject);
        params.put("TextBody", body == null ? "" : body);
        if (StringUtils.hasText(fromAlias)) {
            params.put("FromAlias", fromAlias);
        }
        try {
            AliyunRpcClient.get(
                    "dm.aliyuncs.com",
                    accessKeyId,
                    accessKeySecret,
                    "SingleSendMail",
                    "2015-11-23",
                    params);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Aliyun mail failed", ex);
            throw new BizException("Failed to send email via Aliyun");
        }
    }

    private void sendTencent(String to, String subject, String body) {
        String secretId = require("MAIL_TENCENT_SECRET_ID");
        String secretKey = require("MAIL_TENCENT_SECRET_KEY");
        String fromEmail = require("MAIL_TENCENT_FROM_EMAIL");
        String region = config.get("MAIL_TENCENT_REGION", "ap-guangzhou").trim();
        Map<String, Object> payload = new HashMap<>();
        payload.put("FromEmailAddress", fromEmail);
        payload.put("Destination", java.util.List.of(to));
        payload.put("Subject", subject == null ? "" : subject);
        payload.put("Simple", Map.of("Text", body == null ? "" : body));
        try {
            TencentApiClient.post(
                    "ses",
                    "ses.tencentcloudapi.com",
                    "SendEmail",
                    "2020-10-02",
                    region,
                    secretId,
                    secretKey,
                    payload);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Tencent mail failed", ex);
            throw new BizException("Failed to send email via Tencent");
        }
    }

    private String require(String key) {
        return requireTrimmed(key, "邮件引擎未配置: " + key + " / Mail engine not configured: " + key);
    }

    private String requireTrimmed(String key, String message) {
        String value = trim(config.get(key, ""));
        if (!StringUtils.hasText(value)) {
            throw new BizException(message);
        }
        return value;
    }

    private static String render(String text, Map<String, ?> vars) {
        String out = text == null ? "" : text;
        if (vars == null) {
            return out;
        }
        for (Map.Entry<String, ?> entry : vars.entrySet()) {
            out = out.replace("{{" + entry.getKey() + "}}", entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return out;
    }

    private static Map<String, Object> readJsonObject(String json, String key) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new BizException("Invalid JSON config: " + key);
        }
    }

    private static String stringVal(Object value, String def) {
        if (value == null) {
            return def;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? def : text;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
