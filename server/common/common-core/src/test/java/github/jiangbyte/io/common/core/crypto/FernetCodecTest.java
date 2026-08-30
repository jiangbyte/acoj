package github.jiangbyte.io.common.core.crypto;

/**
 * Author: Charlie
 **/

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FernetCodecTest {

    @Test
    void roundTripAndPythonToken() {
        String key = "XV1rJ-UPAbWeYjprihKNS3ZCCHdBuVbIc0WXmYc70ck=";
        FernetCodec codec = new FernetCodec(key);
        String token = codec.encrypt("admin");
        assertTrue(FernetCodec.looksLikeToken(token));
        assertEquals("admin", codec.tryDecrypt(token));

        // 由 hei-fastapi / cryptography.fernet 生成的 token
        String pyToken = "gAAAAABqd2UjFPQnf2Ubdi-R-sRcKvxSzYWKfVPTx2hYthjsnEmqJKFkl--chSO7APE99mrhVHoknNBj0OsEslytv3qaZ444ZA==";
        assertEquals("superadmin", codec.tryDecrypt(pyToken));
    }
}
