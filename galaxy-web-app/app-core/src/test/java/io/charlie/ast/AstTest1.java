package io.charlie.ast;

import io.charlie.app.core.modular.similarity.language.cpp.CPP14Lexer;
import io.charlie.app.core.modular.similarity.language.cpp.CPP14Parser;
import io.charlie.app.core.modular.similarity.language.cpp.CPP14ParserBaseVisitor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/09/2025
 * @description TODO
 */
public class AstTest1 {
    // ********************* AST节点接口 ********************* //
    public interface ASTNode {
        String getType();

        List<ASTNode> getChildren();
    }

    // 具体节点实现
    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class FunctionNode implements ASTNode {
        private String name;
        private List<ASTNode> parameters;
        private ASTNode body;

        @Override
        public String getType() {
            return "";
        }

        @Override
        public List<ASTNode> getChildren() {
            return List.of();
        }
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class VariableNode implements ASTNode {
        private String name;
        private String type;

        @Override
        public List<ASTNode> getChildren() {
            return List.of();
        }
    }

    public static class CppASTVisitor extends CPP14ParserBaseVisitor<ASTNode> {

    }


    // ********************* 主测试方法 ********************* //
    public static void main(String[] args) {
        // 初始化
        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromString(""));
        CPP14Parser parser = new CPP14Parser(new CommonTokenStream(lexer));
        Vocabulary vocabulary = CPP14Lexer.VOCABULARY;

        // 测试用的代码
        String code = """
                 #include <iostream>
                \s
                 int main() {
                     int a, b;
                     int sum;
                    \s
                     std::cout << "请输入第一个数字 a: ";
                     std::cin >> a;
                    \s
                     std::cout << "请输入第二个数字 b: ";
                     std::cin >> b;
                    \s
                     sum = a + b;
                     std::cout << "a + b = " << sum << std::endl;
                    \s
                     return 0;
                 }
                \s""";

        // 解析代码
        CharStream input = CharStreams.fromString(code);
        // 设置输入源
        lexer.setInputStream(input);
        // 创建Token流
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // 设置Token流
        parser.setTokenStream(tokens);
        // 获取语法分析树
//        CPP14Parser.TranslationUnitContext tree = parser.translationUnit();
        ParseTree tree = parser.translationUnit();
        // DEBUG 打印语法分析树
//        printTree(tree, "");

        // 使用Visitor构建AST
        CppASTVisitor visitor = new CppASTVisitor();
        // TODO
    }

    // 递归打印语法分析树
    public static void printTree(ParseTree tree, String indent) {
        System.out.println(indent + tree.getClass().getSimpleName() + ": " + tree.getText());
        for (int i = 0; i < tree.getChildCount(); i++) {
            printTree(tree.getChild(i), indent + "  ");
        }
    }
}
