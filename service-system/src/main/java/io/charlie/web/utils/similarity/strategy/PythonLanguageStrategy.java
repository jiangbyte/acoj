//package io.charlie.web.modular.task.similarity.strategy;
//
//import io.charlie.web.modular.task.similarity.factory.LanguageStrategy;
//import io.charlie.web.modular.task.similarity.language.python.Python3Lexer;
//import io.charlie.web.modular.task.similarity.language.python.Python3Parser;
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
// * @description python 策略
// */
//@Slf4j
//@Component("pythonLanguageStrategy")
//public class PythonLanguageStrategy implements LanguageStrategy {
//    private final Python3Lexer lexer;
//    private final Python3Parser parser;
//    private final Vocabulary vocabulary;
//
//    public PythonLanguageStrategy() {
//        this.lexer = new Python3Lexer(CharStreams.fromString(""));
//        this.parser = new Python3Parser(new CommonTokenStream(lexer));
//        this.vocabulary = Python3Lexer.VOCABULARY;
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
//        // 从文件输入开始解析
//        Python3Parser.File_inputContext tree = parser.file_input();
//
//        List<Integer> integers = new ArrayList<>();
//        traverse(tree, integers);
//
//        return integers;
//    }
//
//    /**
//     * 遍历AST收集Token
//     */
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


package io.charlie.web.utils.similarity.strategy;

import io.charlie.web.utils.similarity.factory.LanguageStrategy;
import io.charlie.web.utils.similarity.language.python.Python3Lexer;
import io.charlie.web.utils.similarity.language.python.Python3Parser;
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
@Component("pythonLanguageStrategy")
public class PythonLanguageStrategy implements LanguageStrategy {
    private final Vocabulary vocabulary;
    private final ConcurrentMap<String, ParsedTokens> tokenCache;

    public PythonLanguageStrategy() {
        this.vocabulary = Python3Lexer.VOCABULARY;
        this.tokenCache = new ConcurrentHashMap<>();
    }

    /**
     * 同一遍解析得到的类型和原文，保证相似度下标和着色用的文本对齐。
     */
    private record ParsedTokens(List<Integer> types, List<String> texts) {
        private static final ParsedTokens EMPTY = new ParsedTokens(List.of(), List.of());
    }

    @Override
    public List<Integer> getTokenInfo(String code) {
        return new ArrayList<>(load(code).types());
    }

    /**
     * 解析 Python 源码并整理成可比对的 token。
     * 注释正文词法阶段已经丢弃，但文件头注释仍会留下一个换行，这里把它去掉。
     */
    private ParsedTokens load(String code) {
        // 1. 空源码不解析，避免无意义的语法树
        if (code == null || code.trim().isEmpty()) {
            return ParsedTokens.EMPTY;
        }
        // 2. 同一份源码复用结果，类型和文本来自同一次解析
        String cacheKey = code.hashCode() + "_python";
        ParsedTokens cached = tokenCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        ParsedTokens parsed = parseTokens(code);
        if (tokenCache.size() < 1000) {
            tokenCache.put(cacheKey, parsed);
        }
        return parsed;
    }

    private ParsedTokens parseTokens(String code) {
        List<Integer> types = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        try {
            // 3. 优先语法树；快速模式失败再退回精确模式，两次都只收集默认通道上的终结符
            CharStream input = CharStreams.fromString(code);
            Python3Lexer lexer = new Python3Lexer(input);
            setupErrorListener(lexer);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Python3Parser parser = new Python3Parser(tokens);
            setupErrorListener(parser);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);

            Python3Parser.File_inputContext tree;
            try {
                tree = parser.file_input();
            } catch (Exception e) {
                parser.reset();
                parser.getInterpreter().setPredictionMode(PredictionMode.LL);
                tree = parser.file_input();
            }
            traverse(tree, types, texts);
        } catch (Exception e) {
            log.warn("解析Python代码失败，尝试词法分析: {}", e.getMessage());
            types.clear();
            texts.clear();
            if (!fallbackToLexerOnly(code, types, texts)) {
                return ParsedTokens.EMPTY;
            }
        }
        // 4. 去掉注释行、空行带进来的多余换行，变量改名和空格不进入 token 类型
        stripLayoutNewlines(types, texts);
        return new ParsedTokens(List.copyOf(types), List.copyOf(texts));
    }

    /**
     * 词法兜底。只保留默认通道，注释和空白不进相似度序列。
     */
    private boolean fallbackToLexerOnly(String code, List<Integer> types, List<String> texts) {
        try {
            Python3Lexer lexer = new Python3Lexer(CharStreams.fromString(code));
            setupErrorListener(lexer);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();
            for (Token token : tokens.getTokens()) {
                appendToken(token, types, texts);
            }
            return true;
        } catch (Exception e) {
            log.error("词法分析也失败: {}", e.getMessage());
            return false;
        }
    }

    private void traverse(ParseTree tree, List<Integer> types, List<String> texts) {
        if (tree instanceof TerminalNode terminal) {
            appendToken(terminal.getSymbol(), types, texts);
            return;
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            traverse(tree.getChild(i), types, texts);
        }
    }

    private void appendToken(Token token, List<Integer> types, List<String> texts) {
        if (token.getType() == Token.EOF || token.getChannel() != Token.DEFAULT_CHANNEL) {
            return;
        }
        types.add(token.getType());
        texts.add(token.getText() == null ? "" : token.getText());
    }

    /**
     * 去掉不改变程序结构的换行。
     * Python 把注释行后的换行留在默认通道里，文件头多一行注释就会让原本相同的代码掉到 98% 左右。
     */
    private void stripLayoutNewlines(List<Integer> types, List<String> texts) {
        // 1. 连续换行折成一个，空行不再单独占 token
        List<Integer> foldedTypes = new ArrayList<>(types.size());
        List<String> foldedTexts = new ArrayList<>(texts.size());
        for (int i = 0; i < types.size(); i++) {
            int type = types.get(i);
            if (type == Python3Lexer.NEWLINE && !foldedTypes.isEmpty()
                    && foldedTypes.get(foldedTypes.size() - 1) == Python3Lexer.NEWLINE) {
                continue;
            }
            foldedTypes.add(type);
            foldedTexts.add(texts.get(i));
        }
        // 2. 去掉首尾换行：文件头注释、末尾空行都不参与比对
        int from = 0;
        int to = foldedTypes.size();
        while (from < to && foldedTypes.get(from) == Python3Lexer.NEWLINE) {
            from++;
        }
        while (to > from && foldedTypes.get(to - 1) == Python3Lexer.NEWLINE) {
            to--;
        }
        types.clear();
        texts.clear();
        types.addAll(foldedTypes.subList(from, to));
        texts.addAll(foldedTexts.subList(from, to));
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
        // 与 getTokenInfo 同一次解析，避免原文序列和类型序列错位
        List<String> texts = new ArrayList<>(load(code).texts());
        if (tokenValues != null && tokenValues.size() != texts.size()) {
            log.debug("Python token 文本与类型数量不一致: types={}, texts={}", tokenValues.size(), texts.size());
        }
        return texts;
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