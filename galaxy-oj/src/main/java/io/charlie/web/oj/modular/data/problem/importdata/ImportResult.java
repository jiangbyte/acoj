
// ImportResult.java
package io.charlie.web.oj.modular.data.problem.importdata;

import lombok.Data;
import java.util.List;

import java.util.ArrayList;

@Data
public class ImportResult {
    private boolean success;
    private String message;
    private int successCount;
    private int totalCount;
    private List<String> errors;

    public ImportResult() {
        this.errors = new ArrayList<>();
    }

    public static ImportResult of(int successCount, int totalCount, List<String> errors) {
        ImportResult result = new ImportResult();
        result.success = successCount > 0;
        result.successCount = successCount;
        result.totalCount = totalCount;
        result.errors = errors != null ? errors : new ArrayList<>();

        if (successCount == totalCount) {
            result.message = String.format("成功导入所有%d个题目", totalCount);
        } else if (successCount > 0) {
            result.message = String.format("成功导入%d/%d个题目", successCount, totalCount);
        } else {
            result.message = "导入失败，所有题目均未成功导入";
        }

        return result;
    }

    public static ImportResult error(String message) {
        ImportResult result = new ImportResult();
        result.success = false;
        result.message = message;
        result.successCount = 0;
        result.totalCount = 0;
        return result;
    }
}