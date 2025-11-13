fun main() {
	val n = readln().toInt()
	val p = readln().split(' ').map { it.toInt() }
	val t = readln().toInt()
	for (scenario in 1..t) {
		for ((i, pi) in p.withIndex().sortedBy { it.value }.reversed()) {
			if (pi == 0) break
			println("retest ${i + 1}")
			System.out.flush()
			var result = readLine() ?: return
			while (result.isEmpty()) result = readLine() ?: return
			if (result == "Wrong Answer") return
			if (result == "1") break
		}
		println("done")
		System.out.flush()
	}
}
