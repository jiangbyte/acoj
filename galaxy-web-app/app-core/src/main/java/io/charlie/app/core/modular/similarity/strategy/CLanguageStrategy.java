package io.charlie.app.core.modular.similarity.strategy;

import io.charlie.app.core.modular.similarity.factory.LanguageStrategy;
import io.charlie.app.core.modular.similarity.language.c.CLexer;
import io.charlie.app.core.modular.similarity.language.c.CParser;
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
 * @description c 策略
 */
@Slf4j
@Component("cLanguageStrategy")
public class CLanguageStrategy implements LanguageStrategy {
    private final CLexer lexer;
    private final CParser parser;
    private final Vocabulary vocabulary;

    public CLanguageStrategy() {
        this.lexer = new CLexer(CharStreams.fromString(""));
        this.parser = new CParser(new CommonTokenStream(lexer));
        this.vocabulary = CLexer.VOCABULARY;
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

        CParser.CompilationUnitContext tree = parser.compilationUnit();

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
