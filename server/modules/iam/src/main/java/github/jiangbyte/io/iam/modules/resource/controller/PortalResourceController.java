package github.jiangbyte.io.iam.modules.resource.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.iam.modules.resource.entity.SysResource;
import github.jiangbyte.io.iam.modules.resource.service.ResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 门户端资源 API：返回当前登录账号可见菜单资源。
 *
 * Author: Charlie
 */
@Tag(name = "门户端资源 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalResourceController {

    private final ResourceService resourceService;

    /** 当前门户账号可见菜单资源。 */
    @Operation(summary = "当前门户账号可见菜单资源。")
    @GetMapping("/v1/portal/sys/resources/current")
    public ApiResponse<List<SysResource>> current() {
        return ApiResponse.ok(resourceService.listPublicPortalResources());
    }
}
