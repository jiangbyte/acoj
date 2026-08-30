package github.jiangbyte.io.sys.modules.banner.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.sys.modules.banner.entity.SysBanner;
import github.jiangbyte.io.sys.modules.banner.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门户端 Banner API：查询有效 Banner。
 *
 * Author: Charlie
 */
@Tag(name = "门户端 Banner API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalBannerController {

    private final BannerService bannerService;

    /** 列表查询。 */
    @Operation(summary = "列表查询。")
    @GetMapping("/v1/portal/sys/banners/list")
    public ApiResponse<List<SysBanner>> list(
            @RequestParam String position,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type) {
        return ApiResponse.ok(bannerService.portalList(position, category, type));
    }

    /** Banner 互动上报。 */
    @Operation(summary = "Banner 互动上报。")
    @PostMapping("/v1/portal/sys/banners/interaction")
    public ApiResponse<Void> interaction(@Valid @RequestBody IdParam param) {
        bannerService.interaction(param.getId());
        return ApiResponse.ok();
    }
}
