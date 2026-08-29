import io.charlie.web.utils.similarity.factory.LanguageStrategy;
import io.charlie.web.utils.similarity.strategy.CppLanguageStrategy;

import java.util.List;

public class CodeTokenUtilTest {
    public static void main(String[] args) {
        LanguageStrategy languageStrategy = new CppLanguageStrategy();

        String code = """
#include <iostream>

int main() {    
    int a, b;
    std::cin >> a >> b;
    std::cout << a + b << std::endl;
    return 0;
}
                
                """;

        List<Integer> tokenInfo = languageStrategy.getTokenInfo(code);
        System.out.println(tokenInfo);
        List<String> tokenNames = languageStrategy.getTokenNames(tokenInfo);
        System.out.println(tokenNames);
    }
}
