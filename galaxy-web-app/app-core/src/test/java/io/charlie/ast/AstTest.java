package io.charlie.ast;

import io.charlie.app.core.modular.similarity.language.cpp.CPP14Lexer;
import io.charlie.app.core.modular.similarity.language.cpp.CPP14Parser;
import io.charlie.app.core.modular.similarity.language.cpp.CPP14ParserBaseVisitor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/09/2025
 * @description 从语法分析树生成抽象语法树（AST）
 */
public class AstTest {
    // ********************* 主测试方法 ********************* //
    public static void main(String[] args) {
        String code = "#include <iostream>  int main() {return 0;}";
        System.out.println("输入的代码：" + code);

        // 1. 创建输入流
        CharStream input = CharStreams.fromString(code);

        // 2. 用正确的输入流创建 Lexer（关键修正！）
        CPP14Lexer lexer = new CPP14Lexer(input);

        // 3. 用上面创建好的 Lexer 创建 TokenStream
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 4. 填充 TokenStream（现在可以了，因为lexer从开始就是正确的）
        tokens.fill(); // 显式填充，或者下一步的 getTokens() 也会隐式填充

        // 5. 查看 tokens（此时应该不为空了）
        for (Token token : tokens.getTokens()) {
            System.out.println(token.getType());
        }

        // 6. 用填充好的 TokenStream 创建 Parser
        CPP14Parser parser = new CPP14Parser(tokens);

        // 7. 开始解析
        ParseTree tree = parser.translationUnit();
        System.out.println("Parse Tree: " + tree.toStringTree(parser));
    }
}