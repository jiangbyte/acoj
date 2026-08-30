package github.jiangbyte.io.sys.modules.dict.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.sys.modules.dict.service.DictService;
import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门户端数据字典 API：按类型查询字典项。
 *
 * Author: Charlie
 */
@Tag(name = "门户端数据字典 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalDictController {

    private final DictService dictService;

    /** 树形查询。 */
    @Operation(summary = "树形查询。")
    @GetMapping("/v1/portal/sys/dicts/tree")
    public ApiResponse<List<Tree<String>>> tree(@RequestParam(required = false) String category) {
        return ApiResponse.ok(dictService.tree(category));
    }
}
