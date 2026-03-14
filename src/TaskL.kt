import kotlin.math.*

class TaskL(val n: Int, val k: Int) {
	companion object {
		private fun round(x: Double) = (x * 100_000_000).roundToInt()

		fun run(): Double {
			val (n, k) = readln().split(' ').map { it.toInt() }
			val sectors = Array(n) {
				val (x, y, r) = readln().split(' ').map { it.toInt() }
				val c = sqrt((x * x + y * y).toDouble())
				val betta = asin(r / c)
				val alfa = if (y >= 0) {
					acos(x / c)
				} else {
					if (x >= 0) -acos(x / c) else -acos(x / c) + PI * 2
				}
				Sector(alfa - betta, alfa + betta)
			}
			return TaskL(n, k).run(sectors)
		}

		fun runTest(sectors: Array<Pair<Double, Double>>, k: Int): Double {
			val secArr = sectors.map { (a, b) -> Sector(a, b) }.toTypedArray()
			return TaskL(sectors.size, k).run(secArr)
		}
	}

	private val pi = round(PI)

	private data class Sector(val a: Int, val b: Int, var n: Int = -1) {
		constructor(a: Double, b: Double) : this(round(a), round(b))
		val len = b - a
	}

	private fun cutSectors(sectors: Array<Sector>): List<Sector> {
		val result = mutableListOf<Sector>()
		for (s in sectors) {
			if (s.b < 0 || s.a > pi) continue
			val a = if (s.a < 0) 0 else s.a
			val b = if (s.b > pi) pi else s.b
			if (a == b) continue
			result.add(Sector(a, b))
		}
		return result
	}

	private fun removeContainedSectors(sectors: List<Sector>): List<Sector> {
		val sorted = sectors.sortedWith(compareBy({ it.a }, { -it.b }))
		var bMax = -1
		return sorted.filter { if (it.b <= bMax) false else { bMax = it.b; true } }
	}

	// Assuming s1.a < s2.a and s1.b < s2.b
	private fun plusLen(s1: Sector, s2: Sector): Int {
		return if (s2.a < s1.b) s2.b - s1.b else s2.len
	}

	private fun run(sectors: Array<Sector>): Double {
		val cutSectors = cutSectors(sectors)
		val mSectors = removeContainedSectors(cutSectors)

		if (mSectors.isEmpty()) return 0.0
		val maxLimit = mSectors.windowed(2)
			.fold(mSectors.first().len) { len, (s1, s2) -> len + plusLen(s1, s2) }
		if (mSectors.size <= k) return maxLimit.toDouble() / pi

		// Dynamic programming
		val n = mSectors.size
		val lens = IntArray(n)
		for (j in mSectors.indices) lens[j] = mSectors[j].len
		for (i in 2..k) {
			for (j in n - 1 downTo i - 1) {
				val s = mSectors[j]
				val pMax = (i - 2 until j).maxBy { lens[it] + plusLen(mSectors[it], s) }
				val len = lens[pMax] + plusLen(mSectors[pMax], s)
				if (len >= maxLimit) return len.toDouble() / pi
				lens[j] = len
			}
		}
		val kLen = lens.sliceArray(k - 1 until n).max()
		return kLen.toDouble() / pi
	}
}

fun main() {
	println(TaskL.run())
}
