# EBNF Calculator in Kotlin

This is a toy command line calculator which was created in order to learn Kotlin language and explore it's abilities.

The implementation has no dependencies - everything is done using standard library.

In this app I created a hand-written lexer to generate a token stream which is parsed into AST using recursive descent parser.

## EBNF Grammar

```bash
Expression := Term { ("+" | "-") Term }
Term       := Factor { ( "*" | "/" ) Factor }
Factor     := Primary [ "^" Factor ]
Primary    := Number | "(" Expression ")" | ("+" | "-") Primary
Number     := Digit{Digit} [ "." ] {Digit}
Digit      := "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
```

## Running

```bash
 ./gradlew run
```

## Usage

```bash
Type "\q" to exit
> 3+1/-2.     
> 2.5
> \q

```

## License
[MIT](https://choosealicense.com/licenses/mit/)