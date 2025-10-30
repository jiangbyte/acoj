package io.charlie.web.oj.utils.similarity.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 相似度详细信息类
 */
@Setter
@Getter
public class TokenDetail {
    private List<Integer> tokens;

    private List<String> tokenNames;
    private List<String> tokenTexts;
}