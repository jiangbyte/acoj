package io.charlie.web.oj.utils.similarity.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 相似度详细信息类
 */
@Setter
@Getter
public class SimilarityDetail {
    private double similarity;

    private String submitUser; // 提交用户
    private String libraryUser; // 样本集用户

    private List<String> submitTokenNames;
    private List<String> libraryTokenNames;

    private List<String> submitTokenTexts;
    private List<String> libraryTokenTexts;

    public SimilarityDetail() {

    }

    public SimilarityDetail(double similarity, List<String> submitTokenNames,
                            List<String> libraryTokenNames, List<String> submitTokenTexts,
                            List<String> libraryTokenTexts) {
        this.similarity = similarity;

        this.submitTokenNames = submitTokenNames;
        this.submitTokenTexts = submitTokenTexts;
        this.libraryTokenNames = libraryTokenNames;
        this.libraryTokenTexts = libraryTokenTexts;
    }
}