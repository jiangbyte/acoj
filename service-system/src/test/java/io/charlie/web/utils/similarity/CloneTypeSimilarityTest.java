package io.charlie.web.utils.similarity;

import io.charlie.web.utils.similarity.factory.LanguageStrategy;
import io.charlie.web.utils.similarity.strategy.CLanguageStrategy;
import io.charlie.web.utils.similarity.strategy.CppLanguageStrategy;
import io.charlie.web.utils.similarity.strategy.GoLanguageStrategy;
import io.charlie.web.utils.similarity.strategy.JavaLanguageStrategy;
import io.charlie.web.utils.similarity.strategy.PythonLanguageStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Type-1（注释、空白）和 Type-2（再改名、改同类型字面量）的骨架必须完全一致，相似度 100%。
 */
class CloneTypeSimilarityTest {

    private static final int MIN_MATCH = 8;

    @Test
    void pythonType1AndType2ScoreOne() {
        LanguageStrategy strategy = new PythonLanguageStrategy();
        String base = """
                def add(a, b):
                    return a + b
                print(add(1, 2))
                """;
        String type1 = """
                # header

                def add(a, b):  # sum
                    return a + b

                print(add(1, 2))
                """;
        String type2 = """
                def plus(x, y):
                    return x + y
                print(plus(3, 4))
                """;
        String userLeft = """
                # A+B
                x,y = map(int, input().split())
                print(x + y)
                """;
        String userRight = """
                a, b = map(int, input().split())
                print(a+b)
                """;
        assertClone(strategy, "python type-1", base, type1);
        assertClone(strategy, "python type-2", base, type2);
        assertClone(strategy, "python user pair", userLeft, userRight);
    }

    @Test
    void cType1AndType2ScoreOne() {
        LanguageStrategy strategy = new CLanguageStrategy();
        String base = """
                int sum(int numbers[], int length) {
                    int total = 0;
                    for (int i = 0; i < length; i++) {
                        total += numbers[i];
                    }
                    return total;
                }
                """;
        String type1 = """
                /* sum */
                int sum(int numbers[], int length) {
                    int total = 0; // acc

                    for (int i = 0; i < length; i++) {
                            total += numbers[i];
                    }

                    return total;
                }
                """;
        String type2 = """
                int accumulate(int vals[], int n) {
                    int result = 9;
                    for (int idx = 0; idx < n; idx++) {
                        result += vals[idx];
                    }
                    return result;
                }
                """;
        assertClone(strategy, "c type-1", base, type1);
        assertClone(strategy, "c type-2", base, type2);
    }

    @Test
    void cppType1AndType2ScoreOne() {
        LanguageStrategy strategy = new CppLanguageStrategy();
        String base = """
                #include <iostream>
                using namespace std;
                int sumArray(int numbers[], int length) {
                    int total = 0;
                    for (int i = 0; i < length; i++) {
                        total += numbers[i];
                    }
                    return total;
                }
                int main() {
                    int arr[5] = {1, 2, 3, 4, 5};
                    cout << sumArray(arr, 5) << endl;
                    return 0;
                }
                """;
        String type1 = """
                #include <iostream>
                using namespace std;

                // Sum all array elements
                int sumArray(int numbers[], int length) {
                    int total = 0;   // accumulator

                    for (int i = 0; i < length; i++) {
                            total += numbers[i];
                    }

                    return total;
                }

                int main() {
                    /* test data */
                    int arr[5] = {1, 2, 3, 4, 5};

                    cout << sumArray(arr, 5) << endl;
                    return 0;
                }
                """;
        String type2 = """
                #include <iostream>
                using namespace std;
                int accumulateList(int vals[], int n) {
                    int result = 0;
                    for (int idx = 0; idx < n; idx++) {
                        result += vals[idx];
                    }
                    return result;
                }
                int main() {
                    int data[5] = {9, 8, 7, 6, 5};
                    cout << accumulateList(data, 9) << endl;
                    return 0;
                }
                """;
        assertClone(strategy, "cpp type-1", base, type1);
        assertClone(strategy, "cpp type-2", base, type2);
    }

    @Test
    void javaType1AndType2ScoreOne() {
        LanguageStrategy strategy = new JavaLanguageStrategy();
        String base = """
                public class Main {
                    static int sum(int[] numbers, int length) {
                        int total = 0;
                        for (int i = 0; i < length; i++) {
                            total += numbers[i];
                        }
                        return total;
                    }
                }
                """;
        String type1 = """
                public class Main {
                    // sum
                    static int sum(int[] numbers, int length) {
                        int total = 0; /* acc */

                        for (int i = 0; i < length; i++) {
                                total += numbers[i];
                        }

                        return total;
                    }
                }
                """;
        String type2 = """
                public class Program {
                    static int accumulate(int[] vals, int n) {
                        int result = 9;
                        for (int idx = 0; idx < n; idx++) {
                            result += vals[idx];
                        }
                        return result;
                    }
                }
                """;
        assertClone(strategy, "java type-1", base, type1);
        assertClone(strategy, "java type-2", base, type2);
    }

    @Test
    void goType1AndType2ScoreOne() {
        LanguageStrategy strategy = new GoLanguageStrategy();
        String base = """
                package main
                func sum(numbers []int, length int) int {
                    total := 0
                    for i := 0; i < length; i++ {
                        total += numbers[i]
                    }
                    return total
                }
                """;
        String type1 = """
                package main

                // sum
                func sum(numbers []int, length int) int {
                    total := 0 // acc

                    for i := 0; i < length; i++ {
                            total += numbers[i]
                    }

                    return total
                }
                """;
        String type2 = """
                package main
                func accumulate(vals []int, n int) int {
                    result := 9
                    for idx := 0; idx < n; idx++ {
                        result += vals[idx]
                    }
                    return result
                }
                """;
        assertClone(strategy, "go type-1", base, type1);
        assertClone(strategy, "go type-2", base, type2);
    }

    private void assertClone(LanguageStrategy strategy, String label, String left, String right) {
        List<Integer> leftTokens = strategy.getTokenInfo(left);
        List<Integer> rightTokens = strategy.getTokenInfo(right);
        List<String> leftNames = strategy.getTokenNames(leftTokens);
        List<String> rightNames = strategy.getTokenNames(rightTokens);
        double score = similarity(leftTokens, rightTokens, MIN_MATCH);
        assertEquals(rightTokens, leftTokens, () -> label + " tokens differ, score=" + score
                + "\nleft=" + leftNames + "\nright=" + rightNames);
        assertEquals(1.0, score, 0.0001, label + " similarity");
        assertTrue(leftTokens.size() > 0, label + " produced no tokens");
    }

    /**
     * 与 service-similarity 的 GST 计分一致：2 * 匹配数 / 两侧长度。
     */
    static double similarity(List<Integer> token1, List<Integer> token2, int minMatchLength) {
        int matches = greedyStringTiling(token1, token2, minMatchLength);
        int total = token1.size() + token2.size();
        if (matches == 0 || total == 0) {
            return 0.0;
        }
        return Math.round((matches * 2.0 / total) * 10000.0) / 10000.0;
    }

    private static int greedyStringTiling(List<Integer> token1, List<Integer> token2, int minMatchLength) {
        if (token1.isEmpty() || token2.isEmpty()) {
            return 0;
        }
        boolean[] matched1 = new boolean[token1.size()];
        boolean[] matched2 = new boolean[token2.size()];
        int total = 0;
        while (true) {
            int maxMatch = minMatchLength;
            List<int[]> maxTiles = new ArrayList<>();
            for (int i = 0; i < token1.size(); i++) {
                if (matched1[i] || token1.size() - i < maxMatch) {
                    continue;
                }
                for (int j = 0; j < token2.size(); j++) {
                    if (matched2[j] || token2.size() - j < maxMatch) {
                        continue;
                    }
                    int k = 0;
                    while (i + k < token1.size() && j + k < token2.size()
                            && !matched1[i + k] && !matched2[j + k]
                            && token1.get(i + k).equals(token2.get(j + k))) {
                        k++;
                    }
                    if (k > maxMatch) {
                        maxMatch = k;
                        maxTiles.clear();
                        maxTiles.add(new int[]{i, j, k});
                    } else if (k == maxMatch && k > minMatchLength) {
                        maxTiles.add(new int[]{i, j, k});
                    }
                }
            }
            if (maxMatch <= minMatchLength) {
                break;
            }
            for (int[] tile : maxTiles) {
                for (int k = 0; k < tile[2]; k++) {
                    matched1[tile[0] + k] = true;
                    matched2[tile[1] + k] = true;
                }
                total += tile[2];
            }
        }
        return total;
    }
}
