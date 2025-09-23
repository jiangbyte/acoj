package io.charlie.web.oj.modular.task.similarity.strategy;

import io.charlie.web.oj.modular.task.similarity.factory.LanguageStrategy;
import io.charlie.web.oj.modular.task.similarity.language.java.Java20Lexer;
import io.charlie.web.oj.modular.task.similarity.language.java.Java20Parser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description java 策略
 * 源代码 →
 * 词法分析 (Lexical Analysis) → 生成 Token 流
 * 语法分析 (Syntax Analysis) → 生成 AST (抽象语法树)
 * 遍历 AST 提取 Token 序列 → 用于 GST (Greedy String Tiling) 相似度计算
 * 计算相似度 → 输出匹配结果
 */
@Slf4j
@Component("javaLanguageStrategy")
public class JavaLanguageStrategy implements LanguageStrategy {
    private final Java20Lexer lexer;
    private final Java20Parser parser;
    private final Vocabulary vocabulary;

    public JavaLanguageStrategy() {
        this.lexer = new Java20Lexer(CharStreams.fromString(""));
        this.parser = new Java20Parser(new CommonTokenStream(lexer));
        this.vocabulary = Java20Lexer.VOCABULARY;
    }

    @Override
    public List<Integer> getTokenInfo(String code) {
        if (code == null || code.trim().isEmpty()) {
            return List.of();
        }

        CharStream input = CharStreams.fromString(code);
        lexer.setInputStream(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer); // 词法分析生成 Token 流
        parser.setTokenStream(tokens);

        // 从编译单元开始解析
        Java20Parser.CompilationUnitContext tree = parser.compilationUnit(); // 语法分析生成 AST

        // 收集AST中的所有Token
        // 获取语法分析后的AST Token序列
        List<Integer> astTokens = new ArrayList<>();
        traverseAST(tree, astTokens);

        return astTokens;
    }

    /**
     * 遍历AST收集Token
     */
    private void traverseAST(ParseTree tree, List<Integer> tokens) {
        if (tree instanceof TerminalNode) {
            Token token = ((TerminalNode) tree).getSymbol();
            if (token.getType() != Token.EOF) {
                tokens.add(token.getType());
            }
        } else {
            for (int i = 0; i < tree.getChildCount(); i++) {
                traverseAST(tree.getChild(i), tokens);
            }
        }
    }

    @Override
    public List<String> getTokenNames(List<Integer> tokenValues) {
        List<String> names = new ArrayList<>();
        for (Integer tokenValue : tokenValues) {
            String name = this.vocabulary.getSymbolicName(tokenValue);
            names.add(name != null ? name : "UNKNOWN_" + tokenValue);
        }
        return names;
    }

    @Override
    public List<String> getTokenTexts(List<Integer> tokenValues, String code) {
        CharStream input = CharStreams.fromString(code);
        lexer.setInputStream(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        List<String> texts = new ArrayList<>();
        for (Token token : tokens.getTokens()) {
            if (token.getType() == Token.EOF) continue;
            if (tokenValues.contains(token.getType())) {
                texts.add(token.getText());
            }
        }

        return texts;
    }
}
