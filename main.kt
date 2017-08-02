fun main(args: Array<String>) {
    val input: String = "7 + 11 + 8 * 0 + 3"
    val code: List<String> = input.split(" ")
    val tokens = code.map {
        x -> if (x.contains(Regex("\\d"))) Token(Token.Type.NUMBER, x) else Token(Token.Type.ARITHOP, x)
    }
    val Parser = ParserCombinator(tokens)
    val ast = Parser.parse
    println(ast)
}
