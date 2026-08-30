package github.jiangbyte.io.auth;

/** Author: Charlie **/

import github.jiangbyte.io.auth.modules.login.service.AuthService;
import github.jiangbyte.io.auth.modules.login.result.CaptchaResult;
import github.jiangbyte.io.auth.modules.login.controller.AdminAuthController;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthCaptchaControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminAuthController controller;

    @Test
    void captchaEndpointReturnsPayload() {
        CaptchaResult captcha = new CaptchaResult();
        captcha.setCaptchaId("cid-1");
        captcha.setImageBase64("data:image/svg+xml;base64,abc");
        when(authService.captcha(eq("svg"))).thenReturn(captcha);

        ApiResponse<CaptchaResult> response = controller.captcha("svg");

        assertNotNull(response.getData());
        assertEquals("cid-1", response.getData().getCaptchaId());
        verify(authService).captcha("svg");
    }
}
