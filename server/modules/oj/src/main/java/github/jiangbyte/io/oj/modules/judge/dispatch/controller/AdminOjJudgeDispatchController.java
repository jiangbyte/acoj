package github.jiangbyte.io.oj.modules.judge.dispatch.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.oj.modules.judge.dispatch.entity.OjJudgeDispatch;
import github.jiangbyte.io.oj.modules.judge.dispatch.param.OjJudgeDispatchPageParam;
import github.jiangbyte.io.oj.modules.judge.dispatch.service.OjJudgeDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 OJ 派发审计 API：按提交分页查询。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 OJ 派发审计 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminOjJudgeDispatchController {

    private final OjJudgeDispatchService ojJudgeDispatchService;

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/oj/judge-dispatches/page")
    @SaCheckPermission(value = "oj:submission:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<OjJudgeDispatch>> page(@Valid @ModelAttribute OjJudgeDispatchPageParam param) {
        return ApiResponse.ok(ojJudgeDispatchService.page(param));
    }
}
