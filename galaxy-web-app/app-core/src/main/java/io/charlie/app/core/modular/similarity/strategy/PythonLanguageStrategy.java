package io.charlie.app.core.modular.similarity.strategy;

import io.charlie.app.core.modular.similarity.factory.LanguageStrategy;
import io.charlie.app.core.modular.similarity.language.python.Python3Lexer;
import io.charlie.app.core.modular.similarity.language.python.Python3Parser;
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
 * @description python 策略
 */
@Slf4j
@Component("pythonLanguageStrategy")
public class PythonLanguageStrategy implements LanguageStrategy {
    private final Python3Lexer lexer;
    private final Python3Parser parser;
    private final Vocabulary vocabulary;

    public PythonLanguageStrategy() {
        this.lexer = new Python3Lexer(CharStreams.fromString(""));
        this.parser = new Python3Parser(new CommonTokenStream(lexer));
        this.vocabulary = Python3Lexer.VOCABULARY;
    }

    @Override
    public List<Integer> getTokenInfo(String code) {
        if (code == null || code.trim().isEmpty()) {
            return List.of();
        }

        CharStream input = CharStreams.fromString(code);
        lexer.setInputStream(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser.setTokenStream(tokens);

        // 从文件输入开始解析
        Python3Parser.File_inputContext tree = parser.file_input();

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
