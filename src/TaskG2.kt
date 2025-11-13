import java.math.BigInteger

fun main() {
	fun isGood(s: String, q: String): Boolean {
		val withoutSpaces = s.replace(" ", "")
		if (q in withoutSpaces) return false
		val numbers = s.split(' ')
		return numbers.all { it.length == 1 || it[0] != '0' }
	}

	fun str(xWithSpaces: StringBuilder, n: BigInteger): String {
		val strNum = n.toString()
		val result = StringBuilder()
		var j = strNum.lastIndex
		for (c in xWithSpaces.reversed()) {
			if (c == 'X') {
				if (j >= 0) {
					result.insert(0, strNum[j--])
				} else {
					result.insert(0, '0')
				}
			} else {
				result.insert(0, c)
			}
		}
		return result.toString()
	}

	val q = readln()
	val s = readln()
	val withoutSpaces = s.replace(" ", "")
	val replacedByX = StringBuilder(withoutSpaces)
	for ((i, part) in withoutSpaces.windowed(q.length,1).withIndex()) {
		if (part == q) for (j in 0 until q.length) replacedByX[i + j] = 'X'
	}
	val size = replacedByX.count { it == 'X' }
	if (size == 0) {
		println(s)
		return
	}
	val xWithSpaces = StringBuilder()
	var j = 0
	for (c in s) {
		if (c == ' ') {
			xWithSpaces.append(c)
		} else {
			xWithSpaces.append(replacedByX[j++])
		}
	}
	val xNumbers = xWithSpaces.split(' ')
	val strNum = StringBuilder("0".repeat(size))
	j = 0
	for (num in xNumbers) {
		if (num[0] == 'X' && num.length > 1) strNum[j] = '1'
		j += num.count { it == 'X' }
	}
	var n = strNum.toString().toBigInteger()
	while (!isGood(str(xWithSpaces, n), q)) n++
	println(str(xWithSpaces, n))
}
