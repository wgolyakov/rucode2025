fun main() {
	val n = readln().toInt()
	var nines = 0
	for (i in 1..n) {
		val pi = readln()
		nines += 2
		for (j in pi.indices.reversed()) {
			if (pi[j] == '0') {
				nines++
			} else {
				if (pi[j] == '9') nines--
				break
			}
		}
	}
	println(nines)
}
