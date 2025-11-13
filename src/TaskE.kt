import kotlin.math.max

fun main() {
	val (n, m) = readln().split(' ').map { it.toLong() }
	val mMax = (n - 1) * n / 2
	if (m > mMax) {
		println(-1)
		return
	}
	val dMin = max(n - m, 1)
	var dMax = n - 1
	var s = 0L
	for (i in 1 until n) {
		s += i
		if (m <= s) break
		dMax -= 1
	}
	println("$dMin $dMax")
}
