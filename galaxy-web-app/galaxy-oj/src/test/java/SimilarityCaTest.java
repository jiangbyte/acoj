import io.charlie.web.oj.GalaxyOJ;
import io.charlie.web.oj.modular.task.similarity.basic.utils.CodeSimilarityCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = GalaxyOJ.class)
public class SimilarityCaTest {

    @Autowired
    private CodeSimilarityCalculator codeSimilarityCalculator;

    @Test
    public void runAllTests() {
        System.out.println("=== 开始所有相似度测试 ===\n");

        testJavaIdentical();
        testJavaDifferent();
        testCppIdentical();
        testCppDifferent();
        testPythonIdentical();
        testPythonDifferent();
        testCIdentical();
        testCDifferent();
        testGolangIdentical();
        testGolangDifferent();

        System.out.println("\n=== 所有测试完成 ===");
    }

    @Test
    public void runAllTestsWithExceptionHandling() {
        System.out.println("=== 开始所有相似度测试 ===\n");

        testWithHandling("Java相同代码", this::testJavaIdentical);
        testWithHandling("Java不同风格", this::testJavaDifferent);
        testWithHandling("C++相同代码", this::testCppIdentical);
        testWithHandling("C++不同风格", this::testCppDifferent);
        testWithHandling("Python相同代码", this::testPythonIdentical);
        testWithHandling("Python不同范式", this::testPythonDifferent);
        testWithHandling("C相同代码", this::testCIdentical);
        testWithHandling("C不同方法", this::testCDifferent);
        testWithHandling("Golang相同代码", this::testGolangIdentical);
        testWithHandling("Golang不同风格", this::testGolangDifferent);

        System.out.println("\n=== 所有测试完成 ===");
    }

    private void testWithHandling(String testName, Runnable test) {
        try {
            System.out.print(testName + ": ");
            test.run();
        } catch (Exception e) {
            System.out.println("错误 - " + e.getMessage());
            // 可以选择记录日志但不中断其他测试
        }
    }

    private void testJavaIdentical() {
        String language = "java";
        String code1 = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "}";
        String code2 = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "}";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("Java相同代码相似度: " + similarity);
    }

    private void testJavaDifferent() {
        String language = "java";
        String code1 = "public class Calculator {\n" +
                "    public static int sumArray(int[] numbers) {\n" +
                "        int total = 0;\n" +
                "        for (int i = 0; i < numbers.length; i++) {\n" +
                "            total += numbers[i];\n" +
                "        }\n" +
                "        return total;\n" +
                "    }\n" +
                "}";
        String code2 = "import java.util.Arrays;\n" +
                "public class Calculator {\n" +
                "    public static int calculateSum(int[] numbers) {\n" +
                "        return Arrays.stream(numbers).sum();\n" +
                "    }\n" +
                "}";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("Java不同风格相似度: " + similarity);
    }

    private void testCppIdentical() {
        String language = "cpp";
        String code1 = "int main() {\n" +
                "    printf(\"Hello World!\");\n" +
                "    return 0;\n" +
                "}";
        String code2 = "int main() {\n" +
                "    printf(\"Hello World!\");\n" +
                "    return 0;\n" +
                "}";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("C++相同代码相似度: " + similarity);
    }

    private void testCppDifferent() {
        String language = "cpp";
        String code1 = "#include <iostream>\n" +
                "int main() {\n" +
                "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                "    int sum = 0;\n" +
                "    for (int i = 0; i < 5; i++) {\n" +
                "        sum += arr[i];\n" +
                "    }\n" +
                "    return 0;\n" +
                "}";
        String code2 = "#include <iostream>\n" +
                "#include <vector>\n" +
                "#include <numeric>\n" +
                "int main() {\n" +
                "    std::vector<int> numbers = {1, 2, 3, 4, 5};\n" +
                "    auto result = std::accumulate(numbers.begin(), numbers.end(), 0);\n" +
                "    return 0;\n" +
                "}";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("C++不同风格相似度: " + similarity);
    }

    private void testPythonIdentical() {
        String language = "python";
        String code1 = "print(\"Hello World!\")";
        String code2 = "print(\"Hello World!\")";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("Python相同代码相似度: " + similarity);
    }

    private void testPythonDifferent() {
        String language = "python";
        String code1 = "def calculate_sum(numbers):\n" +
                "    total = 0\n" +
                "    for i in range(len(numbers)):\n" +
                "        total += numbers[i]\n" +
                "    return total";
        String code2 = "from functools import reduce\n" +
                "def compute_total(values):\n" +
                "    return reduce(lambda x, y: x + y, values)";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("Python不同范式相似度: " + similarity);
    }

    private void testCIdentical() {
        String language = "c";
        String code1 = "int main() {\n" +
                "    printf(\"Hello World!\");\n" +
                "    return 0;\n" +
                "}";
        String code2 = "int main() {\n" +
                "    printf(\"Hello World!\");\n" +
                "    return 0;\n" +
                "}";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("C相同代码相似度: " + similarity);
    }

    private void testCDifferent() {
        String language = "c";
        String code1 = "#include <stdio.h>\n" +
                "int main() {\n" +
                "    int arr[5] = {1, 2, 3, 4, 5};\n" +
                "    int sum = 0;\n" +
                "    for (int i = 0; i < 5; i++) {\n" +
                "        sum += arr[i];\n" +
                "    }\n" +
                "    return 0;\n" +
                "}";
        String code2 = "#include <stdio.h>\n" +
                "typedef struct {\n" +
                "    int values[5];\n" +
                "} NumberArray;\n" +
                "int calculate_total(NumberArray *arr) {\n" +
                "    int total = 0;\n" +
                "    for (int i = 0; i < 5; i++) {\n" +
                "        total += arr->values[i];\n" +
                "    }\n" +
                "    return total;\n" +
                "}";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("C不同方法相似度: " + similarity);
    }

    private void testGolangIdentical() {
        String language = "go";
        String code1 = "package main\n" +
                "import \"fmt\"\n" +
                "func main() {\n" +
                "    fmt.Println(\"Hello World!\") \n" +
                "}";
        String code2 = "package main\n" +
                "import \"fmt\"\n" +
                "func main() {\n" +
                "    fmt.Println(\"Hello World!\") \n" +
                "}";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("Golang相同代码相似度: " + similarity);
    }

    private void testGolangDifferent() {
        String language = "go";
        String code1 = "package main\n" +
                "func main() {\n" +
                "    numbers := []int{1, 2, 3, 4, 5}\n" +
                "    sum := 0\n" +
                "    for i := 0; i < len(numbers); i++ {\n" +
                "        sum += numbers[i]\n" +
                "    }\n" +
                "}";
        String code2 = "package main\n" +
                "func calculateTotal(nums []int) int {\n" +
                "    total := 0\n" +
                "    for _, value := range nums {\n" +
                "        total += value\n" +
                "    }\n" +
                "    return total\n" +
                "}";
        double similarity = codeSimilarityCalculator.calculateSimilarity(language, code1, code2, 3);
        System.out.println("Golang不同风格相似度: " + similarity);
    }
}