fun main() {
	var n = 0
	for (i in 29 downTo 0) {
		val t = 1 shl i
		println("? $t")
		System.out.flush()
		var s = readLine() ?: return
		while (s.isEmpty()) s = readLine() ?: return
		val d = s.toInt()
		if (d <= 0) n = n or t
	}
	println("! $n")
	System.out.flush()
}
