//package io.charlie.web.oj.modular.task.similarity.strategy;
//
//import io.charlie.web.oj.modular.task.similarity.factory.LanguageStrategy;
//import io.charlie.web.oj.modular.task.similarity.language.java.Java20Lexer;
//import io.charlie.web.oj.modular.task.similarity.language.java.Java20Parser;
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
// * @description java 策略
// * 源代码 →
// * 词法分析 (Lexical Analysis) → 生成 Token 流
// * 语法分析 (Syntax Analysis) → 生成 AST (抽象语法树)
// * 遍历 AST 提取 Token 序列 → 用于 GST (Greedy String Tiling) 相似度计算
// * 计算相似度 → 输出匹配结果
// */
//@Slf4j
//@Component("javaLanguageStrategy")
//public class JavaLanguageStrategy implements LanguageStrategy {
//    private final Java20Lexer lexer;
//    private final Java20Parser parser;
//    private final Vocabulary vocabulary;
//
//    public JavaLanguageStrategy() {
//        this.lexer = new Java20Lexer(CharStreams.fromString(""));
//        this.parser = new Java20Parser(new CommonTokenStream(lexer));
//        this.vocabulary = Java20Lexer.VOCABULARY;
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
//        CommonTokenStream tokens = new CommonTokenStream(lexer); // 词法分析生成 Token 流
//        parser.setTokenStream(tokens);
//
//        // 从编译单元开始解析
//        Java20Parser.CompilationUnitContext tree = parser.compilationUnit();
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


package io.charlie.web.oj.modular.task.similarity.basic.strategy;

import io.charlie.web.oj.modular.task.similarity.basic.factory.LanguageStrategy;
import io.charlie.web.oj.modular.task.similarity.basic.language.java.Java20Lexer;
import io.charlie.web.oj.modular.task.similarity.basic.language.java.Java20Parser;
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

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 11/08/2025
 * @description java 策略 - 性能优化版
 */
@Slf4j
@Component("javaLanguageStrategy")
public class JavaLanguageStrategy implements LanguageStrategy {
    private final Vocabulary vocabulary;
    private final ConcurrentMap<String, List<Integer>> tokenCache;
    private final ThreadLocal<Java20Lexer> lexerThreadLocal;
    private final ThreadLocal<Java20Parser> parserThreadLocal;

    public JavaLanguageStrategy() {
        this.vocabulary = Java20Lexer.VOCABULARY;
        this.tokenCache = new ConcurrentHashMap<>();

        // 使用ThreadLocal避免多线程竞争
        this.lexerThreadLocal = ThreadLocal.withInitial(() -> {
            Java20Lexer lexer = new Java20Lexer(CharStreams.fromString(""));
            setupErrorListener(lexer);
            return lexer;
        });

        this.parserThreadLocal = ThreadLocal.withInitial(() -> {
            Java20Parser parser = new Java20Parser(new CommonTokenStream(lexerThreadLocal.get()));
            setupErrorListener(parser);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL); // 快速解析模式
            return parser;
        });
    }

    @Override
    public List<Integer> getTokenInfo(String code) {
        if (code == null || code.trim().isEmpty()) {
            return List.of();
        }

        // 缓存优化
        String cacheKey = code.hashCode() + "_java";
        List<Integer> cached = tokenCache.get(cacheKey);
        if (cached != null) {
            return new ArrayList<>(cached); // 返回副本
        }

        try {
            Java20Lexer lexer = lexerThreadLocal.get();
            Java20Parser parser = parserThreadLocal.get();

            CharStream input = CharStreams.fromString(code);
            lexer.setInputStream(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser.setTokenStream(tokens);

            // 尝试快速解析
            Java20Parser.CompilationUnitContext tree;
            try {
                tree = parser.compilationUnit();
            } catch (Exception e) {
                // 快速解析失败，使用更精确的解析模式
                parser.reset();
                parser.getInterpreter().setPredictionMode(PredictionMode.LL);
                tree = parser.compilationUnit();
            }

            List<Integer> tokensList = new ArrayList<>();
            traverse(tree, tokensList);

            // 缓存结果（限制缓存大小）
            if (tokenCache.size() < 1000) {
                tokenCache.put(cacheKey, new ArrayList<>(tokensList));
            }

            return tokensList;

        } catch (Exception e) {
            log.warn("解析Java代码失败，尝试词法分析: {}", e.getMessage());
            // 降级处理：只进行词法分析
            return fallbackToLexerOnly(code);
        }
    }

    private List<Integer> fallbackToLexerOnly(String code) {
        try {
            Java20Lexer lexer = new Java20Lexer(CharStreams.fromString(code));
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
            Java20Lexer lexer = new Java20Lexer(CharStreams.fromString(code));
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
                // 静默处理，不输出到控制台
                log.debug("语法错误 at {}:{} - {}", line, charPositionInLine, msg);
            }
        });
    }

    // 清理资源的方法
    public void cleanup() {
        lexerThreadLocal.remove();
        parserThreadLocal.remove();
    }
}
