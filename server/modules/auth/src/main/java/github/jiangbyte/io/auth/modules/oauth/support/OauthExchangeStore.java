/** Author: Charlie */

package github.jiangbyte.io.auth.modules.oauth.support;

import cn.hutool.core.util.IdUtil;
import github.jiangbyte.io.auth.modules.login.result.LoginResult;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * OAuth 登录一次性兑换码：避免把 token 放进前端回调 URL。
 */
@Component
@RequiredArgsConstructor
public class OauthExchangeStore {

    private static final Duration TTL = Duration.ofMinutes(2);

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    public String save(LoginResult login) {
        if (login == null || !StringUtils.hasText(login.getToken())) {
            throw new BizException("登录结果无效");
        }
        String code = IdUtil.simpleUUID();
        Payload payload = new Payload();
        payload.setToken(login.getToken());
        payload.setAccountId(login.getAccountId());
        payload.setAccountType(login.getAccountType() == null ? null : login.getAccountType().name());
        payload.setPasswordExpired(login.getPasswordExpired());
        payload.setForceBindEmail(login.getForceBindEmail());
        payload.setForceBindPhone(login.getForceBindPhone());
        payload.setPasswordExpiryWarningDays(login.getPasswordExpiryWarningDays());
        payload.setExpiresIn(login.getExpiresIn());
        try {
            redissonClient.getBucket(key(code)).set(objectMapper.writeValueAsString(payload), TTL);
        } catch (JacksonException ex) {
            throw new BizException(500, "保存 OAuth 兑换码失败");
        }
        return code;
    }

    public LoginResult consume(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException("兑换码无效或已过期");
        }
        RBucket<String> bucket = redissonClient.getBucket(key(code.trim()));
        String json = bucket.getAndDelete();
        if (!StringUtils.hasText(json)) {
            throw new BizException("兑换码无效或已过期");
        }
        try {
            Payload payload = objectMapper.readValue(json, Payload.class);
            LoginResult result = new LoginResult();
            result.setToken(payload.getToken());
            result.setAccountId(payload.getAccountId());
            if (StringUtils.hasText(payload.getAccountType())) {
                result.setAccountType(AccountType.valueOf(payload.getAccountType()));
            }
            result.setPasswordExpired(payload.getPasswordExpired());
            result.setForceBindEmail(payload.getForceBindEmail());
            result.setForceBindPhone(payload.getForceBindPhone());
            result.setPasswordExpiryWarningDays(payload.getPasswordExpiryWarningDays());
            result.setExpiresIn(payload.getExpiresIn());
            return result;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("兑换码无效或已过期");
        }
    }

    private static String key(String code) {
        return "oauth:exchange:" + code;
    }

    @Data
    public static class Payload {
        private String token;
        private String accountId;
        private String accountType;
        private Boolean passwordExpired;
        private Boolean forceBindEmail;
        private Boolean forceBindPhone;
        private Integer passwordExpiryWarningDays;
        private Long expiresIn;
    }
}
