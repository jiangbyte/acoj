package github.jiangbyte.io.oj.modules.judge.node.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.core.param.IdParam;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.satoken.StpKit;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodeAddParam;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodeEditParam;
import github.jiangbyte.io.oj.modules.judge.node.param.OjJudgeNodePageParam;
import github.jiangbyte.io.oj.modules.judge.node.result.OjJudgeLanguagesResult;
import github.jiangbyte.io.oj.modules.judge.node.service.OjJudgeNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 OJ 执行机 API：CRUD。
 *
 * Author: Charlie
 */
@Tag(name = "管理端 OJ 执行机 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminOjJudgeNodeController {

    private final OjJudgeNodeService ojJudgeNodeService;

    /** 创建。 */
    @Operation(summary = "创建。")
    @PostMapping("/v1/admin/oj/judge-nodes/create")
    @SaCheckPermission(value = "oj:judgenode:create", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_judge_node", action = "create")
    public ApiResponse<Void> create(@Valid @RequestBody OjJudgeNodeAddParam param) {
        ojJudgeNodeService.create(param);
        return ApiResponse.ok();
    }

    /** 更新。 */
    @Operation(summary = "更新。")
    @PostMapping("/v1/admin/oj/judge-nodes/update")
    @SaCheckPermission(value = "oj:judgenode:update", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_judge_node", action = "update")
    public ApiResponse<Void> update(@Valid @RequestBody OjJudgeNodeEditParam param) {
        ojJudgeNodeService.update(param);
        return ApiResponse.ok();
    }

    /** 批量删除。 */
    @Operation(summary = "批量删除。")
    @PostMapping("/v1/admin/oj/judge-nodes/delete")
    @SaCheckPermission(value = "oj:judgenode:delete", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "oj_judge_node", action = "delete")
    public ApiResponse<Void> delete(@Valid @RequestBody IdsParam param) {
        ojJudgeNodeService.delete(param);
        return ApiResponse.ok();
    }

    /** 查询详情。 */
    @Operation(summary = "查询详情。")
    @GetMapping("/v1/admin/oj/judge-nodes/detail")
    @SaCheckPermission(value = "oj:judgenode:detail", type = StpKit.TYPE_ADMIN)
    public ApiResponse<OjJudgeNode> detail(@Valid @ModelAttribute IdParam param) {
        return ApiResponse.ok(ojJudgeNodeService.detail(param.getId()));
    }

    /** 分页查询。 */
    @Operation(summary = "分页查询。")
    @GetMapping("/v1/admin/oj/judge-nodes/page")
    @SaCheckPermission(value = "oj:judgenode:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<OjJudgeNode>> page(@Valid @ModelAttribute OjJudgeNodePageParam param) {
        return ApiResponse.ok(ojJudgeNodeService.page(param));
    }

    /** 聚合 ENABLED 执行机支持的语言（去重）。 */
    @Operation(summary = "聚合执行机支持语言。")
    @GetMapping("/v1/admin/oj/judge-nodes/languages")
    @SaCheckPermission(value = "oj:judgenode:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<OjJudgeLanguagesResult> languages() {
        return ApiResponse.ok(ojJudgeNodeService.listAggregatedLanguages());
    }
}
