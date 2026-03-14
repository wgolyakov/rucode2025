import kotlin.math.*

// Very slow version. For tests with small n.
class TaskL2(val n: Int, val k: Int) {
	companion object {
		fun run(): Double {
			val (n, k) = readln().split(' ').map { it.toInt() }
			val sectors = List(n) {
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
			//println(sectors.joinToString("\n"))
			return TaskL2(n, k).run(sectors)
		}

		fun runTest(sectors: Array<Pair<Double, Double>>, k: Int): Double {
			val sectorsList = sectors.map { (a, b) -> Sector(a, b) }
			return TaskL2(sectors.size, k).run(sectorsList)
		}
	}

	private data class Sector(var a: Double, var b: Double) {
		fun len() = b - a

		fun filter() {
			if (b <= 0 || a >= PI) {
				a = 0.0
				b = 0.0
			} else {
				if (a < 0) a = 0.0
				if (b > PI) b = PI
			}
		}

		fun remove(s: Sector): List<Sector> {
			if (b <= s.a || a >= s.b) return listOf(this)
			if (a >= s.a && b <= s.b) return emptyList()
			if (a < s.a && b > s.b) return listOf(Sector(a, s.a), Sector(s.b, b))
			if (a < s.a) return listOf(Sector(a, s.a))
			if (b > s.b) return listOf(Sector(s.b, b))
			error("Wrong remove: $this - $s")
		}
	}

	private fun calcRecurs(currSectors: List<Sector>, len: Double, level: Int): Double {
		if (level >= k || currSectors.isEmpty()) return len
		var maxLen = 0.0
		for (s in currSectors) {
			val nextSectors = mutableListOf<Sector>()
			for (ns in currSectors) {
				if (ns !== s) nextSectors.addAll(ns.remove(s))
			}
			val res = calcRecurs(nextSectors, len + s.len(), level + 1)
			if (res > maxLen) {
				maxLen = res
			}
		}
		return maxLen
	}

	private fun run(sectors: List<Sector>): Double {
		for (s in sectors) s.filter()
		val kLen = calcRecurs(sectors, 0.0, 0)
		return kLen / PI
	}
}

fun main() {
	println(TaskL2.run())
}
