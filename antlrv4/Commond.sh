# 移除之前生成的
rm -rf /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/c/**
rm -rf /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/java/**
rm -rf /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/cpp/**
rm -rf /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/golang/**
rm -rf /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/python/**

java -jar /mnt/e/MyProjects/astro-code-oj/antlrv4/antlr-4.13.2-complete.jar \
    /mnt/e/MyProjects/astro-code-oj/antlrv4/c/C.g4 \
    -visitor \
    -listener \
    -Dlanguage=Java \
    -o /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/c \
    -package io.charlie.app.core.modular.similarity.language.c

java -jar /mnt/e/MyProjects/astro-code-oj/antlrv4/antlr-4.13.2-complete.jar  \
    /mnt/e/MyProjects/astro-code-oj/antlrv4/cpp/CPP14Lexer.g4  \
    /mnt/e/MyProjects/astro-code-oj/antlrv4/cpp/CPP14Parser.g4  \
    -visitor  \
    -listener  \
    -Dlanguage=Java  \
    -o /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/cpp \
    -package io.charlie.app.core.modular.similarity.language.cpp



java -jar /mnt/e/MyProjects/astro-code-oj/antlrv4/antlr-4.13.2-complete.jar  \
    /mnt/e/MyProjects/astro-code-oj/antlrv4/golang/GoLexer.g4  \
    /mnt/e/MyProjects/astro-code-oj/antlrv4/golang/GoParser.g4  \
    -visitor  \
    -listener  \
    -Dlanguage=Java  \
    -o /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/golang \
    -package io.charlie.app.core.modular.similarity.language.golang


java -jar /mnt/e/MyProjects/astro-code-oj/antlrv4/antlr-4.13.2-complete.jar  \
    /mnt/e/MyProjects/astro-code-oj/antlrv4/java20/Java20Lexer.g4  \
    /mnt/e/MyProjects/astro-code-oj/antlrv4/java20/Java20Parser.g4  \
    -visitor  \
    -listener  \
    -Dlanguage=Java  \
    -o /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/java \
    -package io.charlie.app.core.modular.similarity.language.java

java -jar /mnt/e/MyProjects/astro-code-oj/antlrv4/antlr-4.13.2-complete.jar  \
    /mnt/e/MyProjects/astro-code-oj/antlrv4/python/Python3Lexer.g4  \
    /mnt/e/MyProjects/astro-code-oj/antlrv4/python/Python3Parser.g4  \
    -visitor  \
    -listener  \
    -Dlanguage=Java  \
    -o /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/python \
    -package io.charlie.app.core.modular.similarity.language.python

rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/c/*.tokens
rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/c/*.interp
rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/cpp/*.tokens
rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/cpp/*.interp
rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/golang/*.tokens
rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/golang/*.interp
rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/java/*.tokens
rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/java/*.interp
rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/python/*.tokens
rm -f /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/app-core/src/main/java/io/charlie/app/core/modular/similarity/language/python/*.interp
