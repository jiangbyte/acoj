package github.jiangbyte.io.oj.modules.judge.node.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行机支持语言聚合结果（Admin / Portal 共用）。
 *
 * Author: Charlie
 */
@Schema(description = "执行机支持语言聚合结果")
@Data
public class OjJudgeLanguagesResult {

    @Schema(description = "去重排序后的语言 key 列表")
    private List<String> languages = new ArrayList<>();

    @Schema(description = "参与聚合的 ENABLED 执行机数量")
    private Integer nodeCount;
}
