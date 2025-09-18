package io.charlie.ast;

import io.charlie.app.core.CoreApplication;
import io.charlie.app.core.modular.similarity.utils.CodeSimilarityCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description 代码相似度测试
 */
@SpringBootTest(classes = CoreApplication.class)
public class CodeSimilarityTest {
    @Autowired
    private CodeSimilarityCalculator codeSimilarityCalculator;

    @Test
    public void test() {
        String code1 = """
                public class Example {
                    private String name;
                    private int value;

                    public Example(String name, int value) {
                        this.name = name;
                        this.value = value;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public int getValue() {
                        return value;
                    }

                    public void setValue(int value) {
                        this.value = value;
                    }

                    public void printInfo() {
                        System.out.println("Name: " + name + ", Value: " + value);
                    }
                }
                """;

        String code2 = """
                public class Example {
                    private String name;
                    private int value;

                    public Example(String name, int value) {
                    }
                }
                """;

        CodeSimilarityCalculator.SimilarityDetail detail = codeSimilarityCalculator.getSimilarityDetail("java", code1, code2, 8);
        System.out.println("相似度: " + detail.getSimilarity());
        System.out.println("代码1 Tokens: " + detail.getTokenNames1());
        System.out.println("代码1 Text: " + detail.getTokenTexts1());
        System.out.println("代码2 Tokens: " + detail.getTokenNames2());
        System.out.println("代码2 Text: " + detail.getTokenTexts2());
    }
}
