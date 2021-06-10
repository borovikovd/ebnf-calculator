import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import kotlin.math.pow
import kotlin.reflect.KClass

// EBNF Grammar
// Expression := Term { ("+" | "-") Term }
// Term       := Factor { ( "*" | "/" ) Factor }
// Factor     := Primary [ "^" Factor ]
// Primary    := Number | "(" Expression ")" | ("+" | "-") Primary
// Number     := Digit{Digit} [ "." ] {Digit}
// Digit      := "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"

// Lexical analysis
sealed interface Token
object LPAR : Token
object RPAR : Token
object MINUS : Token
object PLUS : Token
object MUL : Token
object DIV : Token
object POW : Token
object EOF : Token
object ERROR : Token
data class NUMBER(val value: Double) : Token

class Lexer(val reader: Reader) {
    var next = reader.read()

    fun nextToken(): Token {
        while (isWhiteSpace(next())) {
            consume()
        }
        if (eof()) {
            return EOF
        }
        if (isDigit(next())) {
            val sb = StringBuilder()
            while (isDigit(next())) {
                sb.append(next())
                consume()
            }
            if (next() == '.') {
                sb.append('.')
                consume()
            }
            while (isDigit(next())) {
                sb.append(next())
                consume()
            }
            return NUMBER(sb.toString().toDouble())
        }
        val next = when (next()) {
            '(' -> LPAR
            ')' -> RPAR
            '-' -> MINUS
            '+' -> PLUS
            '*' -> MUL
            '/' -> DIV
            '^' -> POW
            else -> ERROR
        }
        consume()
        return next
    }

    // helpers

    private fun eof(): Boolean {
        return next == -1
    }

    private fun next(): Char = next.toChar()

    private fun consume() {
        next = reader.read()
    }

    private fun isDigit(ch: Char) = ch in '0'..'9'

    private fun isWhiteSpace(ch: Char) = ch == ' ' || ch == '\t' || ch == '\n'
}

// Syntax analysis

// AST
sealed interface Expr
data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr
data class ParExpr(val expr: Expr) : Expr
data class Unary(val operator: Token, val expr: Expr) : Expr
data class Num(val literal: Token) : Expr

class SyntaxError(message: String) : Exception(message)

class Parser(val lexer: Lexer) {
    var next = lexer.nextToken()

    // Expression := Term { ("+" | "-") Term }
    fun expr(): Expr {
        var left: Expr = term()
        while (next() == PLUS || next() == MINUS) {
            val op = next()
            consume()
            val right = term()
            left = Binary(left, op, right)
        }
        return left
    }

    // Term := Factor { ( "*" | "/" ) Factor }
    fun term(): Expr {
        var left = factor()
        while (next() == MUL || next() == DIV) {
            val op = next()
            consume()
            val right = factor()
            left = Binary(left, op, right)
        }
        return left
    }

    // Factor := Primary [ "^" Factor ]
    fun factor(): Expr {
        var left = primary()
        if (next() == POW) {
            val op = next()
            consume()
            val right = factor()
            left = Binary(left, op, right)
        }
        return left
    }

    // Primary := Number | "(" Expression ")" | ("+" | "-") Primary
    fun primary(): Expr {
        if (next() == LPAR) {
            consume()
            val expr = expr()
            expect(RPAR::class)
            return ParExpr(expr)
        }
        if (next() == PLUS || next() == MINUS) {
            val op = next()
            consume()
            val expr = primary()
            return Unary(op, expr)
        }
        return number()
    }

    // Number := Digit{Digit} [ "." ] {Digit}
    fun number(): Expr {
        val literal = next()
        expect(NUMBER::class)
        return Num(literal)
    }

    // helpers

    private fun <T : Token> expect(t: KClass<T>) {
        if (next()::class == t) {
            consume()
        } else {
            throw SyntaxError("${t.simpleName} expected")
        }
    }

    private fun next(): Token = next

    private fun consume() {
        next = lexer.nextToken()
    }
}

// Interpreter

object Interpreter {
    fun eval(expr: Expr): Double {
        return when (expr) {
            is Num -> when (expr.literal) {
                is NUMBER -> expr.literal.value
                else -> throw RuntimeException("Unexpected literal - " + expr.literal)
            }
            is ParExpr -> eval(expr.expr)
            is Unary -> when (expr.operator) {
                is PLUS -> eval(expr.expr)
                is MINUS -> -eval(expr.expr)
                else -> throw RuntimeException("Unexpected operator - " + expr.operator)
            }
            is Binary -> {
                val left = eval(expr.left)
                val right = eval(expr.right)
                when (expr.operator) {
                    is PLUS -> left + right
                    is MINUS -> left - right
                    is MUL -> left * right
                    is DIV -> left / right
                    is POW -> left.pow(right)
                    else -> throw RuntimeException("Unexpected operator - " + expr.operator)
                }
            }
        }
    }
}

object Calculator {
    fun eval(input: String): Double {
        val lexer = Lexer(StringReader(input))
        val parser = Parser(lexer)
        val expr = parser.expr()
        return Interpreter.eval(expr)
    }
}

fun main() {
    val reader = BufferedReader(InputStreamReader(System.`in`))
    System.out.println("Type \"\\q\" to exit");
    while (true) {
        System.out.print("> ")
        val line = reader.readLine()
        if (line.equals("\\q")) {
            break;
        }
        try {
            val result = Calculator.eval(line)
            System.out.println("> $result")
        } catch (e: java.lang.Exception) {
            System.out.println("Error: ${e.message}")
        }
    }
}
