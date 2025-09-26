package io.charlie.web.oj.modular.task.similarity.data;

import io.charlie.web.oj.modular.data.similarity.entity.TaskSimilarity;
import lombok.Data;

import java.util.List;

@Data
public class SimilarityResult {
    private List<TaskSimilarity> details;
    private TaskSimilarity maxDetail;

    public SimilarityResult(List<TaskSimilarity> details, TaskSimilarity maxDetail) {
        this.details = details;
        this.maxDetail = maxDetail;
    }
}