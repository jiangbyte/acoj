package github.jiangbyte.io.common.notify.cloud;

/**
 * Author: Charlie
 **/

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AliyunRpcClientTest {

    @Test
    void signRpcParamsMatchesStableSignature() throws Exception {
        Method method = AliyunRpcClient.class.getDeclaredMethod("signRpcParams", Map.class, String.class);
        method.setAccessible(true);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("AccessKeyId", "testid");
        params.put("Action", "SingleSendMail");
        params.put("Format", "JSON");
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureNonce", "nonce");
        params.put("SignatureVersion", "1.0");
        params.put("Timestamp", "2020-01-01T00:00:00Z");
        params.put("Version", "2015-11-23");
        String signature = (String) method.invoke(null, params, "testsecret");
        assertTrue(signature.length() > 10);
        // 相同输入 → 相同签名
        assertEquals(signature, method.invoke(null, params, "testsecret"));
    }
}
