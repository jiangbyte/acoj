package io.charlie.web.utils.similarity;

import io.charlie.web.utils.similarity.strategy.PythonLanguageStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * 注释、空行、改名不应改变 Python 相似度 token。
 */
class PythonTokenNormalizeTest {

    private final PythonLanguageStrategy strategy = new PythonLanguageStrategy();

    @Test
    void leadingCommentAndRenameKeepTheSameTokens() {
        String withComment = """
                # A+B
                x,y = map(int, input().split())
                print(x + y)
                """;
        String renamed = """
                a, b = map(int, input().split())
                print(a+b)
                """;

        assertSameTokens(withComment, renamed);
    }

    @Test
    void blankLineAndTrailingCommentKeepTheSameTokens() {
        String plain = """
                x,y = map(int, input().split())
                print(x + y)
                """;
        String noisy = """
                x,y = map(int, input().split())

                print(x + y)  # done
                """;

        assertSameTokens(plain, noisy);
    }

    @Test
    void differentOperatorStaysDifferent() {
        String plus = "print(x + y)\n";
        String minus = "print(x - y)\n";

        assertNotEquals(strategy.getTokenInfo(plus), strategy.getTokenInfo(minus));
    }

    private void assertSameTokens(String left, String right) {
        List<Integer> leftTokens = strategy.getTokenInfo(left);
        List<Integer> rightTokens = strategy.getTokenInfo(right);
        assertEquals(rightTokens, leftTokens,
                () -> "left=" + strategy.getTokenNames(leftTokens) + " right=" + strategy.getTokenNames(rightTokens));
        assertEquals(leftTokens.size(), strategy.getTokenTexts(leftTokens, left).size());
        assertEquals(rightTokens.size(), strategy.getTokenTexts(rightTokens, right).size());
    }
}
