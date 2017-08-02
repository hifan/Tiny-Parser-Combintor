data class Result<T>(var value: T,
                     var rest: Int) {
    override fun toString(): String {
        return "$value\n"
    }
}

data class Memo<T>(val tokenSize: Int, var map: MutableMap<Int, Array<Result<T>?>>) {
    fun get(funcName: Int, input: Int) = map[funcName]?.get(input)

    fun add(funcName: Int, input: Int , result: Result<T>) {
        if (map.containsKey(funcName)) {
            map[funcName]?.set(input, result)
        }
        else {
            var resList = arrayOfNulls<Result<T>>(tokenSize)
            resList[input] = result
            map.put(funcName, resList)
        }
    }

    fun isExist(funcName: Int, input: Int): Boolean {
        return map.containsKey(funcName) && map[funcName]?.get(input) == null
    }

    override fun toString(): String {
        return "$map"
    }
}

interface Parser<A> {
    operator fun invoke(input: Int): Result<A>

    operator fun <B> plus(right: Parser<B>): Parser<B> = sequence(this, right)

    operator fun div(right: Parser<A>) = disjunctive(this, right)

    operator fun get(op: Char): Parser<A> = repeat(this, op)


    fun <A,B,C> sequence(left: Parser<A>, right: Parser<B>): Parser<C> =
            parse { input ->
                val (leftValue, restInput) = left(input)
                val (rightValue, rest) = right(restInput)
                val value = astOp<A, B, C>(leftValue, rightValue)
                return@parse Result(value, rest)
            }

    fun <T> disjunctive(left: Parser<T>, right: Parser<T>): Parser<T> =
            parse { input ->
                try {
                    return@parse left(input)
                } catch (e: Exception) {
                    return@parse right(input)
                }
            }

    fun <T> repeat(p: Parser<T>, op: Char): Parser<T> {
        return when (op) {
            '*' -> parse { input ->
                try {
                    return@parse (p + repeat(p, op))(input)
                } catch (e: Exception) {
                    return@parse p(input)
                }
            }
            '+' -> parse { input ->
                try {
                    return@parse (p + repeat(p, op))(input)
                } catch (e: Exception) {
                    return@parse p(input)
                }
            }
            else -> throw Exception("Error repeat operation")
        }
    }

    fun <T> parse(f: (Int) -> Result<T>): Parser<T> =
            object : Parser<T> { override fun invoke(input: Int): Result<T> = f(input) }


    fun <A, B, C> astOp(left: A, right: B): C {
        @Suppress("UNCHECKED_CAST")
        return when {
            left is String && right is String -> "$left [$right]" as C
            else -> throw Exception("Unhandled type")
        }
    }

    fun astOp(token: Token) =
            when (token.type) {
                Token.Type.NUMBER -> token.data
                Token.Type.ARITHOP -> token.data
                else -> throw Exception("Error ast 1")
            }


//    fun empty(): Parser<AST> = parse { input: Int -> return@parse Result(AST(), input) }


}

abstract class BasicCombinator<T>(val tokens: List<Token>): Parser<T> {
//    var memo: Memo<Any> = Memo(tokens.size, hashMapOf())

    fun <T> term(value: T) =
            parse { input: Int ->
                val token = tokens.subList(input, tokens.size)[0]
                val tokenType = token.type
                when (value) {
                    token -> return@parse Result(astOp(token), input+1)
                    tokenType -> return@parse Result(astOp(token), input+1)
                    else -> throw Exception("Error term: $token")
                }
            }
}

class ParserCombinator(tokens: List<Token>): BasicCombinator<String>(tokens) {
    override fun invoke(input: Int) = parse

    val addtk = term(Token(Token.Type.ARITHOP, "+"))
    val mulittk = term(Token(Token.Type.ARITHOP, "*"))
    val num = term(Token.Type.NUMBER)
    val mulit = ( num + ( mulittk + num )['+'] ) / num
    val add = ( mulit + ( addtk + mulit )['+'] ) / mulit
    val parse = add(0)
}


