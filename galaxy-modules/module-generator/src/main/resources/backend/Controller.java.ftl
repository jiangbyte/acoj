package ${package.Controller};

import io.charlie.galaxy.result.Result;
import ${package.PageParam}.${entity}PageParam;
import ${package.AddParam}.${entity}AddParam;
import ${package.EditParam}.${entity}EditParam;
import ${package.IdParam}.${entity}IdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
import ${package.Service}.${table.serviceName};
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author ${author}
* @version v1.0
* @date ${date}
* @description ${table.comment!} 控制器
*/
@Tag(name = "${table.comment!}控制器")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Validated
public class ${entity}Controller {
    private final ${table.serviceName} ${table.entityPath}Service;

    @Operation(summary = "获取${table.comment?replace('表', '')}分页")
    //@SaCheckPermission("/${table.name?replace('_', '/')}/page")
    @GetMapping("/${table.name?replace('_', '/')}/page")
    public Result<?> page(@ParameterObject ${entity}PageParam ${table.entityPath}PageParam) {
        return Result.success(${table.entityPath}Service.page(${table.entityPath}PageParam));
    }

    @Operation(summary = "添加${table.comment?replace('表', '')}")
    //@SaCheckPermission("/${table.name?replace('_', '/')}/add")
    @PostMapping("/${table.name?replace('_', '/')}/add")
    public Result<?> add(@RequestBody @Valid ${entity}AddParam ${table.entityPath}AddParam) {
        ${table.entityPath}Service.add(${table.entityPath}AddParam);
        return Result.success();
    }

    @Operation(summary = "编辑${table.comment?replace('表', '')}")
    //@SaCheckPermission("/${table.name?replace('_', '/')}/edit")
    @PostMapping("/${table.name?replace('_', '/')}/edit")
    public Result<?> edit(@RequestBody @Valid ${entity}EditParam ${table.entityPath}EditParam) {
        ${table.entityPath}Service.edit(${table.entityPath}EditParam);
        return Result.success();
    }

    @Operation(summary = "删除${table.comment?replace('表', '')}")
    //@SaCheckPermission("/${table.name?replace('_', '/')}/delete")
    @PostMapping("/${table.name?replace('_', '/')}/delete")
    public Result<?> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<${entity}IdParam> ${table.entityPath}IdParam) {
        ${table.entityPath}Service.delete(${table.entityPath}IdParam);
        return Result.success();
    }

    @Operation(summary = "获取${table.comment?replace('表', '')}详情")
    //@SaCheckPermission("/${table.name?replace('_', '/')}/detail")
    @GetMapping("/${table.name?replace('_', '/')}/detail")
    public Result<?> detail(@ParameterObject @Valid ${entity}IdParam ${table.entityPath}IdParam) {
        return Result.success(${table.entityPath}Service.detail(${table.entityPath}IdParam));
    }
}