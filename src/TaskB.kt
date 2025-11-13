fun main() {
	val a = readln()
	if (a.contains(':')) {
		val (h, m, s) = a.split(':').map { it.toInt() }
		println(s + m * 60 + h * 60 * 60)
	} else {
		val t = a.toInt()
		val h = t / (60 * 60)
		val m = (t % (60 * 60)) / 60
		val s = (t % (60 * 60)) % 60
		println(String.format("%02d:%02d:%02d", h, m, s))
	}
}
