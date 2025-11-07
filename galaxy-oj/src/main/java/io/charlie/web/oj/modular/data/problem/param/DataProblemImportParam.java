package io.charlie.web.oj.modular.data.problem.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-09-20
 * @description 题目
 */
@Data
@Schema(name = "DataProblem", description = "题目 ID参数")
public class DataProblemImportParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "上传的文件")
    private MultipartFile file;

    private BigDecimal threshold;
    private Boolean useAi;
    private Boolean isPublic;
    private Boolean isVisible;
    private String categoryId;
    private Integer difficulty;

    private List<String> tagIds;
    private  List<String> allowedLanguages;
}