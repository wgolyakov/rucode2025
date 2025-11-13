fun main() {
	fun addSpaces(withoutSpaces: StringBuilder, s: String): StringBuilder {
		val withSpaces = StringBuilder()
		var j = 0
		for (c in s) {
			val e = if (c == ' ') c else withoutSpaces[j++]
			withSpaces.append(e)
		}
		return withSpaces
	}

	fun inc(ind: Int, r: StringBuilder, indexes: Set<Int>) {
		var i = ind
		while (i !in indexes) i--
		val d = r[i].digitToInt() + 1
		r[i] = d.digitToChar()
	}

	val q = readln()
	val s = readln()
	val withoutSpaces = s.filter { it != ' ' }
	val replacedByX = StringBuilder(withoutSpaces)
	for ((i, part) in withoutSpaces.windowed(q.length,1).withIndex()) {
		if (part == q) for (j in 0 until q.length) replacedByX[i + j] = 'X'
	}
	val xCount = replacedByX.count { it == 'X' }
	if (xCount == 0) {
		println(s)
		return
	}
	val xWithSpaces = addSpaces(replacedByX, s)
	val indexes = replacedByX.withIndex().filter { it.value == 'X' }.map { it.index }.toSet()
	val xNumbers = xWithSpaces.split(' ')
	val notZeros = mutableSetOf<Int>()
	var j = 0
	for (num in xNumbers) {
		if (num[0] == 'X' && num.length > 1) notZeros.add(j)
		j += num.length
	}
	val r = StringBuilder(replacedByX)
	for (i in indexes) r[i] = if (i in notZeros) '1' else '0'
	var ind: Int
	do {
		ind = r.indexOf(q)
		if (ind != -1) inc(ind + q.length - 1, r, indexes)
	} while (ind != -1)
	val result = addSpaces(r, s)
	println(result)
}
