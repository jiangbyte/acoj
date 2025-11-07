//package io.charlie.web.oj.modular.task.similarity.strategy;
//
//import io.charlie.web.oj.modular.task.similarity.factory.LanguageStrategy;
//import io.charlie.web.oj.modular.task.similarity.language.c.CLexer;
//import io.charlie.web.oj.modular.task.similarity.language.c.CParser;
//import lombok.extern.slf4j.Slf4j;
//import org.antlr.v4.runtime.*;
//import org.antlr.v4.runtime.tree.ParseTree;
//import org.antlr.v4.runtime.tree.TerminalNode;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author ZhangJiangHu
// * @version v1.0
// * @date 11/08/2025
// * @description c 策略
// */
//@Slf4j
//@Component("cLanguageStrategy")
//public class CLanguageStrategy implements LanguageStrategy {
//    private final CLexer lexer;
//    private final CParser parser;
//    private final Vocabulary vocabulary;
//
//    public CLanguageStrategy() {
//        this.lexer = new CLexer(CharStreams.fromString(""));
//        this.parser = new CParser(new CommonTokenStream(lexer));
//        this.vocabulary = CLexer.VOCABULARY;
//    }
//
//    @Override
//    public List<Integer> getTokenInfo(String code) {
//        if (code == null || code.trim().isEmpty()) {
//            return List.of();
//        }
//
//        CharStream input = CharStreams.fromString(code);
//        lexer.setInputStream(input);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        parser.setTokenStream(tokens);
//
//        CParser.CompilationUnitContext tree = parser.compilationUnit();
//
//        List<Integer> integers = new ArrayList<>();
//        traverse(tree, integers);
//
//        return integers;
//    }
//
//    private void traverse(ParseTree tree, List<Integer> tokens) {
//        if (tree instanceof TerminalNode) {
//            Token token = ((TerminalNode) tree).getSymbol();
//            if (token.getType() != Token.EOF) {
//                tokens.add(token.getType());
//            }
//        } else {
//            for (int i = 0; i < tree.getChildCount(); i++) {
//                traverse(tree.getChild(i), tokens);
//            }
//        }
//    }
//
//    @Override
//    public List<String> getTokenNames(List<Integer> tokenValues) {
//        List<String> names = new ArrayList<>();
//        for (Integer tokenValue : tokenValues) {
//            String name = this.vocabulary.getSymbolicName(tokenValue);
//            names.add(name != null ? name : "UNKNOWN_" + tokenValue);
//        }
//        return names;
//    }
//
//    @Override
//    public List<String> getTokenTexts(List<Integer> tokenValues, String code) {
//        CharStream input = CharStreams.fromString(code);
//        lexer.setInputStream(input);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        tokens.fill();
//
//        List<String> texts = new ArrayList<>();
//        for (Token token : tokens.getTokens()) {
//            if (token.getType() == Token.EOF) continue;
//            if (tokenValues.contains(token.getType())) {
//                texts.add(token.getText());
//            }
//        }
//
//        return texts;
//    }
//}



package io.charlie.web.oj.utils.similarity.strategy;

import io.charlie.web.oj.utils.similarity.factory.LanguageStrategy;
import io.charlie.web.oj.utils.similarity.language.c.CLexer;
import io.charlie.web.oj.utils.similarity.language.c.CParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component("cLanguageStrategy")
public class CLanguageStrategy implements LanguageStrategy {
    private final Vocabulary vocabulary;
    private final ConcurrentMap<String, List<Integer>> tokenCache;

    public CLanguageStrategy() {
        this.vocabulary = CLexer.VOCABULARY;
        this.tokenCache = new ConcurrentHashMap<>();
    }

    @Override
    public List<Integer> getTokenInfo(String code) {
        if (code == null || code.trim().isEmpty()) {
            return List.of();
        }

        // 缓存优化
        String cacheKey = code.hashCode() + "_c";
        List<Integer> cached = tokenCache.get(cacheKey);
        if (cached != null) {
            return new ArrayList<>(cached);
        }

        try {
            CharStream input = CharStreams.fromString(code);
            CLexer lexer = new CLexer(input);
            setupErrorListener(lexer);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CParser parser = new CParser(tokens);
            setupErrorListener(parser);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);

            CParser.CompilationUnitContext tree;
            try {
                tree = parser.compilationUnit();
            } catch (Exception e) {
                parser.reset();
                parser.getInterpreter().setPredictionMode(PredictionMode.LL);
                tree = parser.compilationUnit();
            }

            List<Integer> tokensList = new ArrayList<>();
            traverse(tree, tokensList);

            if (tokenCache.size() < 1000) {
                tokenCache.put(cacheKey, new ArrayList<>(tokensList));
            }

            return tokensList;

        } catch (Exception e) {
            log.warn("解析C代码失败，尝试词法分析: {}", e.getMessage());
            return fallbackToLexerOnly(code);
        }
    }

    private List<Integer> fallbackToLexerOnly(String code) {
        try {
            CLexer lexer = new CLexer(CharStreams.fromString(code));
            setupErrorListener(lexer);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            List<Integer> tokenTypes = new ArrayList<>();
            for (Token token : tokens.getTokens()) {
                if (token.getType() != Token.EOF) {
                    tokenTypes.add(token.getType());
                }
            }
            return tokenTypes;
        } catch (Exception e) {
            log.error("词法分析也失败: {}", e.getMessage());
            return List.of();
        }
    }

    private void traverse(ParseTree tree, List<Integer> tokens) {
        if (tree instanceof TerminalNode) {
            Token token = ((TerminalNode) tree).getSymbol();
            if (token.getType() != Token.EOF) {
                tokens.add(token.getType());
            }
        } else {
            for (int i = 0; i < tree.getChildCount(); i++) {
                traverse(tree.getChild(i), tokens);
            }
        }
    }

    @Override
    public List<String> getTokenNames(List<Integer> tokenValues) {
        List<String> names = new ArrayList<>(tokenValues.size());
        for (Integer tokenValue : tokenValues) {
            String name = this.vocabulary.getSymbolicName(tokenValue);
            names.add(name != null ? name : "UNKNOWN_" + tokenValue);
        }
        return names;
    }

    @Override
    public List<String> getTokenTexts(List<Integer> tokenValues, String code) {
        if (code == null || code.trim().isEmpty()) {
            return List.of();
        }

        try {
            CLexer lexer = new CLexer(CharStreams.fromString(code));
            setupErrorListener(lexer);
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
        } catch (Exception e) {
            log.error("获取token文本失败: {}", e.getMessage());
            return List.of();
        }
    }

    private void setupErrorListener(Recognizer<?, ?> recognizer) {
        recognizer.removeErrorListeners();
        recognizer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg, RecognitionException e) {
                log.debug("语法错误 at {}:{} - {}", line, charPositionInLine, msg);
            }
        });
    }
}