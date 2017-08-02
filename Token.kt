class Token(var type: Token.Type, var data: String) {
    enum class Type constructor(val pattern: String) {
        NUMBER("-?[0-9]+"), FLOATNUM("-?[1-9][0-9]+\\.[0-9]+"),
        ARITHOP("[*/+-]"), BOOLOP("&&|\\|\\|")
    }

    override fun toString(): String {
        if (data !== "")
            return "($type.name $data)"
        else
            return "($type.name)"
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val token = o as Token?
        return type == token!!.type && data == token.data
    }
}