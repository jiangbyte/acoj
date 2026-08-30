package github.jiangbyte.io.auth.modules.login.support;

/**
 * Author: Charlie
 **/

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptchaImageRendererTest {

    @Test
    void svgContainsNoiseLinesAndRotatedGlyphs() {
        SecureRandom random = new SecureRandom();
        random.setSeed(1L);
        String b64 = CaptchaImageRenderer.svgBase64("A2B3", random);
        String svg = new String(Base64.getDecoder().decode(b64));
        assertTrue(svg.contains("<line "));
        assertTrue(svg.contains("transform=\"rotate("));
        assertTrue(svg.contains(">A</text>"));
        assertTrue(svg.contains("viewBox=\"0 0 140 44\""));
    }

    @Test
    void pngHasValidSignature() {
        SecureRandom random = new SecureRandom();
        random.setSeed(2L);
        byte[] png = Base64.getDecoder().decode(CaptchaImageRenderer.pngBase64("A2B3", random));
        assertTrue(png.length > 8);
        assertTrue(png[0] == (byte) 0x89 && png[1] == 'P' && png[2] == 'N' && png[3] == 'G');
    }
}
