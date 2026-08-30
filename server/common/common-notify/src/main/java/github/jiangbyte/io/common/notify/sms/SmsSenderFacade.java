package github.jiangbyte.io.common.notify.sms;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.notify.NotifyConfigSource;
import github.jiangbyte.io.common.notify.cloud.AliyunRpcClient;
import github.jiangbyte.io.common.notify.cloud.TencentApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/**
 * 短信发送门面：按厂商配置调用阿里云/腾讯云等短信通道。
 *
 * Author: Charlie
 */
public class SmsSenderFacade {

    private static final Logger log = LoggerFactory.getLogger(SmsSenderFacade.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NotifyConfigSource config;

    public SmsSenderFacade(NotifyConfigSource config) {
        this.config = config;
    }

    /** 按模板发送短信。 */
    public void sendTemplated(String scene, String phone, Map<String, ?> vars) {
        String key = "SMS_TEMPLATE_" + scene;
        Map<String, Object> tmpl = readJsonObject(config.get(key, ""), key);
        String code = trim(stringVal(tmpl.get("code"), ""));
        if (!StringUtils.hasText(code)) {
            throw new BizException("SMS template code missing: " + key);
        }
        send(phone, code, vars == null ? Map.of() : vars);
    }

    /** 按配置发送短信。 */
    public void send(String phone, String templateCode, Map<String, ?> params) {
        String engine = config.get("DEFAULT_SMS_ENGINE", "ALIYUN").trim().toUpperCase(Locale.ROOT);
        switch (engine) {
            case "ALIYUN" -> sendAliyun(phone, templateCode, params);
            case "TENCENT" -> sendTencent(phone, templateCode, params);
            default -> throw new BizException("Unsupported SMS engine: " + engine);
        }
    }

    private void sendAliyun(String phone, String templateCode, Map<String, ?> params) {
        String accessKeyId = require("SMS_ALIYUN_ACCESS_KEY_ID");
        String accessKeySecret = require("SMS_ALIYUN_ACCESS_KEY_SECRET");
        String signName = require("SMS_ALIYUN_SIGN_NAME");
        try {
            Map<String, String> business = new HashMap<>();
            business.put("PhoneNumbers", phone);
            business.put("SignName", signName);
            business.put("TemplateCode", templateCode);
            business.put("TemplateParam", MAPPER.writeValueAsString(params == null ? Map.of() : params));
            AliyunRpcClient.get(
                    "dysmsapi.aliyuncs.com",
                    accessKeyId,
                    accessKeySecret,
                    "SendSms",
                    "2017-05-25",
                    business);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Aliyun SMS failed", ex);
            throw new BizException("Failed to send SMS via Aliyun");
        }
    }

    private void sendTencent(String phone, String templateCode, Map<String, ?> params) {
        String secretId = require("SMS_TENCENT_SECRET_ID");
        String secretKey = require("SMS_TENCENT_SECRET_KEY");
        String sdkAppId = require("SMS_TENCENT_SDK_APP_ID");
        String signName = require("SMS_TENCENT_SIGN_NAME");
        String region = config.get("SMS_TENCENT_REGION", "ap-guangzhou").trim();
        List<String> templateParamSet = orderedTemplateParams(params);
        String phoneNumber = phone != null && phone.startsWith("+") ? phone : "+86" + phone;
        Map<String, Object> payload = new HashMap<>();
        payload.put("SmsSdkAppId", sdkAppId);
        payload.put("SignName", signName);
        payload.put("TemplateId", templateCode);
        payload.put("TemplateParamSet", templateParamSet);
        payload.put("PhoneNumberSet", List.of(phoneNumber));
        try {
            TencentApiClient.post(
                    "sms",
                    "sms.tencentcloudapi.com",
                    "SendSms",
                    "2021-01-11",
                    region,
                    secretId,
                    secretKey,
                    payload);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Tencent SMS failed", ex);
            throw new BizException("Failed to send SMS via Tencent");
        }
    }

    private static List<String> orderedTemplateParams(Map<String, ?> params) {
        if (params == null || params.isEmpty()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        if (params.containsKey("code")) {
            values.add(String.valueOf(params.get("code")));
            for (String key : new TreeSet<>(params.keySet())) {
                if (!"code".equals(key)) {
                    values.add(String.valueOf(params.get(key)));
                }
            }
            return values;
        }
        for (String key : new TreeSet<>(params.keySet())) {
            values.add(String.valueOf(params.get(key)));
        }
        return values;
    }

    private String require(String key) {
        String value = trim(config.get(key, ""));
        if (!StringUtils.hasText(value)) {
            throw new BizException("短信引擎未配置: " + key + " / SMS engine not configured: " + key);
        }
        return value;
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
