package io.charlie.ranking;

import io.charlie.app.core.modular.similarity.language.java.Java20Lexer;
import io.charlie.app.core.modular.similarity.language.java.Java20Parser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class ASTGenerator {
    public static ParseTree generateAST(String sourceCode) {
        CharStream input = CharStreams.fromString(sourceCode);
        Java20Lexer lexer = new Java20Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java20Parser parser = new Java20Parser(tokens);
        return parser.compilationUnit();
    }
    
    public static void printTree(ParseTree tree, String indent) {
        System.out.println(indent + tree.getClass().getSimpleName() + ": " + tree.getText());
        for (int i = 0; i < tree.getChildCount(); i++) {
            printTree(tree.getChild(i), indent + "  ");
        }
    }
    
    public static void main(String[] args) {
        String javaCode = "public class Hello { public static void main(String[] args) { System.out.println(\"Hello\"); } }";
        ParseTree ast = generateAST(javaCode);
        printTree(ast, "");
    }
}