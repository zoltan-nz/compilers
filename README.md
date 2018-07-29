# SWEN-430 Compilers

## Presentations

1. [Introduction](presentations/01-introduction.pdf)
2. [Compiler Architecture](presentations/02-while-language.pdf)
3. [Parsing 1](presentations/03-parsing-1.pdf)
4. [Parsing 2](presentations/04-parsing-2.pdf)

## Parsing

1. [Grammars and Parse Trees](parsing-presentations/20-parsing-1-of-4.pdf)
2. [Scanner and Parsing](parsing-presentations/21-parsing-2-of-4.pdf)
3. [Building a Parse Tree](parsing-presentations/22-parsing-3-of-4.pdf)
4. [Using Patterns](parsing-presentations/23-parsing-4-of-4.pdf)

## Whiley

* [Whiley Compiler](https://github.com/Whiley/WhileyCompiler)

## While

* [While Language Specification](while/while-language-specification.pdf)
* [Compiler Source Code](while/compiler)

## Downloads

* [while.tar](downloads/while.tar)
* [tests.tar](downloads/tests.tar)

## Notes

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