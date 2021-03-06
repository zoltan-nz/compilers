# SWEN-430 Compilers

> Connected repository with assignments and solutions: https://github.com/zoltan-nz/while-lang-compiler

## Presentations

1. [Introduction](presentations/01-introduction.pdf)
2. [Compiler Architecture](presentations/02-while-language.pdf)
3. [Parsing 1](presentations/03-parsing-1.pdf)
4. [Parsing 2](presentations/04-parsing-2.pdf)
5. [Parsing 3](presentations/05-parsing-3.pdf)
6. [Typing 1.](presentations/06-typing-1.pdf)
7. [Interpreter](presentations/07-interpreter.pdf)
8. [Typing 2.](presentations/08-typing-2.pdf)
9. [Typing 3.](presentations/09-typing-3.pdf)
10. [Unreachable Code](presentations/10-unreachable-code.pdf)
11. [Definite Assignment/Unassignment](presentations/11-definite-assignment.pdf)
12. [Definite Assignment 2.](presentations/12-definite-assignment.pdf) | [Java Bytecode](presentations/12b-java-bytecode.pdf)
13. [Java Bytecode Generation 1](presentations/13-bytecode-generation-1.pdf)
14. [Java Bytecode Generation 2](presentations/14-bytecode-generation-2.pdf)
15. [Java Bytecode Verification](presentations/15-bytecode-verification.pdf)
16. [Machine Code 1](presentations/16-machine-generation-1.pdf)
17. [Machine Code 2](presentations/17-machine-generation-2.pdf)
18. [Machine Code 3](presentations/18-machine-generation-3.pdf)
19. [Paper 1: Class Hierarchy Analysis](presentations/19-paper-1-class-hierarchy-analysis.pdf)
20. [Paper 2: Garbage Collection](presentations/20-garbage-collection.pdf)

## Parsing

1. [Grammars and Parse Trees](parsing-presentations/20-parsing-1-of-4.pdf)
2. [Scanner and Parsing](parsing-presentations/21-parsing-2-of-4.pdf)
3. [Building a Parse Tree](parsing-presentations/22-parsing-3-of-4.pdf)
4. [Using Patterns](parsing-presentations/23-parsing-4-of-4.pdf)

## Papers

1. [Optimization of Object-Oriented Programs Using Static Class Hierarchy Analysis](papers/optimization-of-object-oriented-programs.pdf)
2. [Practical Virtual Method Call Resolution for Java](papers/practical-virtual-method-call-resolution-for-java.pdf)
3. [Myths and Realities: The Performance Impact of Garbage Collection](papers/the-performance-impact-of-garbage-collection.pdf)

## Whiley

- [Whiley Compiler](https://github.com/Whiley/WhileyCompiler)

## While

- [While Language Specification](while/while-language-specification.pdf)
- [Compiler Source Code](while/compiler)
- [My While Language Compiler repository](https://github.com/zoltan-nz/while-lang-compiler)

## Downloads

- [while.tar](downloads/while.tar) - While compiler for Assignment 1
- [tests.tar](downloads/tests.tar) - Test harness for Assignment 1
- [newtests.tar](downloads/newtests.tar) - Test update for Assignment 1
- [assignment2.tgz](downloads/assignment2.tgz) - Tests for Assignment 2
- [whilelang.tar](downloads/whilelang.tar) - Updated While compiler for Assignment 2, 3 and 4
- [jasm-v0.1.7](downloads/jasm-v0.1.7.jar) - Old `jasm` compiler
- [assignment-3-test-harness.tgz](downloads/assignment-3-test-harness.tgz) - Test harness for Assignment 3
- [assignment4.tgz](downloads/assignment4.tgz) - C compiler and test harness for Assignment 4

## Assignments

- [Assignment 1](assignments/assignment-1.pdf)
- [Assignment 1 - Implementation](https://github.com/zoltan-nz/while-lang-compiler/blob/master/docs/assignment-1-notes.md)
- [Assignment 2](assignments/assignment-2.pdf)
- [Assignment 2 - Implementation](https://github.com/zoltan-nz/while-lang-compiler/blob/master/docs/assignment-2-notes.md)
- [Assignment 3](assignments/assignment-3.pdf)
- [Assignment 3 - Implementation](https://github.com/zoltan-nz/while-lang-compiler/blob/master/docs/assignment-3-notes.md)
- [Assignment 4](assignments/assignment-4.pdf)

## JASM

- [Whiley JASM](https://whiley.github.io/Jasm/)

## jx86

- [Java Library for Generating x86 Code](https://github.com/DavePearce/jx86)

## Notes

- [Export fat JAR files from IntelliJ IDEA](export-jar-file-from-intellij-idea.md)

### Regular Expression Notations

```
a             An ordinary character stands for itself.
ε             The empty string.
M|N           Alternation, choosing from M or N.
M·N           Concatenation, an M followed by an N.
MN            Another way to write concatenation.
M∗            Repetition (zero or more times).
M+            Repetition, one or more times.
M?            Optional, zero or one occurrence of M.
[a − zA − Z]  Character set alternation.
.             A period stands for any single character except newline.
"a.+*"        Quotation, a string in quotes stands for itself literally.
```

Regular expressions for some tokens:

```
if                                              IF
[a-z][a-z0-9]*                                  ID
[0-9]+                                          NUM
([0-9]+"."[0-9]*)|([0-9]*"."[0-9]+)             REAL
("--"[a-z]*"\n")|(" "|"\n"|"\t")+               no token, just white space (ex. comment starts: --)
.                                               error
```

### Grammar

- [Context-free grammar](https://en.wikipedia.org/wiki/Context-free_grammar)
- [BNF - Backus-Naur form](https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form)
