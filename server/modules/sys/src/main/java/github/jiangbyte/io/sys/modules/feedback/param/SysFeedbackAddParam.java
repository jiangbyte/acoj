package github.jiangbyte.io.sys.modules.feedback.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 提交反馈的请求参数（标题、分类、内容与附件对象名）。
 *
 * Author: Charlie
 */
@Schema(description = "提交反馈的请求参数（标题、分类、内容与附件对象名）。")
@Data
public class SysFeedbackAddParam {
    @Schema(description = "标题")

    private String title;
    @NotBlank
    @Schema(description = "内容")
    private String content;
    @Schema(description = "分类")
    private String category = "GENERAL";
    @Schema(description = "联系方式")
    private String contact;
    @Schema(description = "用户上传附件 object_name 列表")
    private List<String> attachObjectNames = new ArrayList<>();
}
