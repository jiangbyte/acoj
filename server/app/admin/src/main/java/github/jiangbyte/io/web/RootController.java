package github.jiangbyte.io.web;

/** Author: Charlie **/

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Hidden
@RestController
public class RootController {

    @GetMapping("/")
    public ApiResponse<Map<String, String>> root() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("name", "hei-boot-admin");
        return ApiResponse.ok(data);
    }
}
