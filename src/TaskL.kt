import kotlin.collections.maxBy
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.sqrt

fun main() {
	data class Sector(val a: Double, val b: Double, val n: Int = -1) {
		val len = b - a
		operator fun contains(x: Double) = x in a..b
	}

	class MultiSector {
		val subSectors = mutableSetOf<Int>()
		var len = 0.0
		val intersectors = mutableSetOf<MultiSector>()
		fun isNotEmpty() = subSectors.isNotEmpty()
		operator fun contains(s: MultiSector) = subSectors.containsAll(s.subSectors)
	}

	val e = 0.000000001

	fun convert(sectors: Array<Sector>): Pair<Array<MultiSector>, Array<Sector>> {
		val allSubSectors = mutableListOf<Sector>()
		val mSectors = Array(sectors.size) { MultiSector() }
		val points = mutableSetOf(0.0, PI)
		for (s in sectors) {
			if (s.a in 0.0..PI) points.add(s.a)
			if (s.b in 0.0..PI) points.add(s.b)
		}
		points.remove(-0.0)
		for ((a, b) in points.sorted().windowed(2)) {
			if (b - a < e) {
				if (b < PI) points.remove(b) else points.remove(a)
			}
		}
		for ((a, b) in points.sorted().windowed(2)) {
			val c = (a + b) / 2
			var n = -1
			for ((i, sector) in sectors.withIndex()) {
				if (c in sector) {
					if (n == -1) {
						n = allSubSectors.size
						allSubSectors.add(Sector(a, b, n))
					}
					val m = mSectors[i]
					m.subSectors.add(n)
					m.len += b - a
				}
			}
		}
		return mSectors to allSubSectors.toTypedArray()
	}

	fun calcByMaxLen(sectorsSet: MutableSet<MultiSector>, allSubSectors: Array<Sector>, k: Int, subSectorsCount: IntArray = IntArray(allSubSectors.size)): Double {
		fun plusLen(s: MultiSector) = s.subSectors.filter { subSectorsCount[it] == 0 }.sumOf { allSubSectors[it].len }

		var kLen = 0.0
		var count = 0
		while (count < k) {
			val s = sectorsSet.maxBy { plusLen(it) }
			kLen += plusLen(s)
			for (sNum in s.subSectors) subSectorsCount[sNum]++
			sectorsSet.remove(s)
			count++
		}
		return kLen
	}

	fun calcByMaxLen2(sectorsSet: MutableSet<MultiSector>, allSubSectors: Array<Sector>,
					  mSectorsBySubNum: Array<MutableList<MultiSector>>, k: Int, subSectorsCount: IntArray): Double {
		val usedSectors = mutableListOf<MultiSector>()
		var kLen = 0.0
		var count = 0
		while (count < k && sectorsSet.isNotEmpty()) {
			val s = sectorsSet.maxBy { it.len }
			if (s.len < e) break
			kLen += s.len
			for (sNum in s.subSectors) {
				if (subSectorsCount[sNum] == 0) {
					val dl = allSubSectors[sNum].len
					for (ms in mSectorsBySubNum[sNum]) {
						ms.len -= dl
						if (ms.len < e) {
							sectorsSet.remove(ms)
						}
					}
				}
				subSectorsCount[sNum]++
			}
			usedSectors.add(s)
			count++
		}
		for (s in usedSectors) {
			for (sNum in s.subSectors) {
				subSectorsCount[sNum]--
				if (subSectorsCount[sNum] == 0) {
					val dl = allSubSectors[sNum].len
					for (ms in mSectorsBySubNum[sNum]) ms.len += dl
				}
			}
		}
		return kLen
	}

	fun calcByMinLen(sectorsSet: MutableSet<MultiSector>, allSubSectors: Array<Sector>, k: Int): Double {
		val subSectorsCount = IntArray(allSubSectors.size)
		for (s in sectorsSet) {
			for (sNum in s.subSectors) subSectorsCount[sNum]++
		}

		fun minusLen(s: MultiSector) = s.subSectors.filter { subSectorsCount[it] == 1 }.sumOf { allSubSectors[it].len }

		var kLen = allSubSectors.sumOf { it.len }
		while (sectorsSet.size > k) {
			//val s = sectorsSet.minBy { minusLen(it) }
			val s = sectorsSet.minWith(compareBy({ minusLen(it) }, { it.len }))
			kLen -= minusLen(s)
			for (sNum in s.subSectors) subSectorsCount[sNum]--
			sectorsSet.remove(s)
		}
		return kLen
	}

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
	val (mSectors, allSubSectors) = convert(sectors)
	val goodSectors = mSectors.filter { it.isNotEmpty() }
	val maxLimit = allSubSectors.sumOf { it.len }
	if (goodSectors.size <= k) {
		println(maxLimit / PI)
		return
	}
	val minLimit1 = calcByMaxLen(goodSectors.toMutableSet(), allSubSectors, k)
	val minLimit2 = calcByMinLen(goodSectors.toMutableSet(), allSubSectors, k)
	val minLimit = max(minLimit1, minLimit2)
	if (minLimit > maxLimit - e) {
		println(minLimit / PI)
		return
	}

	val mSectorsBySubNum = Array(allSubSectors.size) { mutableListOf<MultiSector>() }
	for (ms in goodSectors) {
		for (subNum in ms.subSectors) mSectorsBySubNum[subNum].add(ms)
	}

	for (ms in goodSectors) {
		ms.intersectors.addAll(ms.subSectors.map { mSectorsBySubNum[it] }.flatten().toSet().filter { it.len < ms.len && it !in ms })
	}

	val subSectorsCount = IntArray(allSubSectors.size)

	fun calcRecurs(currSectors: Set<MultiSector>, len: Double, level: Int, minLimit: Double, maxLimit: Double): Double {
		if (level >= k || currSectors.isEmpty() || len > maxLimit - e) return len
		if (currSectors.size + level <= k)  {
			return len + allSubSectors.filter { subSectorsCount[it.n] == 0 }.sumOf { it.len }
		}
		val predict = len + calcByMaxLen2(currSectors.toMutableSet(), allSubSectors, mSectorsBySubNum, k - level, subSectorsCount)
		if (predict < minLimit - e) return minLimit
		val maxPlus = currSectors.maxBy { it.len }
		if (maxPlus.len <= e) return len
		val maxMultiSectors = listOf(maxPlus) + maxPlus.intersectors.filter { it in currSectors }
		var maxLen = minLimit
		for (s in maxMultiSectors) {
			val sLen = s.len
			val emptySectors = mutableSetOf<MultiSector>()
			for (sNum in s.subSectors) {
				if (subSectorsCount[sNum] == 0) {
					val dl = allSubSectors[sNum].len
					for (ms in mSectorsBySubNum[sNum]) {
						ms.len -= dl
						if (ms.len < e) emptySectors.add(ms)
					}
				}
				subSectorsCount[sNum]++
			}
			val res = calcRecurs(currSectors - emptySectors, len + sLen, level + 1, maxLen, maxLimit)
			if (res > maxLimit - e) return maxLimit
			if (res > maxLen + e) maxLen = res
			for (sNum in s.subSectors) {
				subSectorsCount[sNum]--
				if (subSectorsCount[sNum] == 0) {
					val dl = allSubSectors[sNum].len
					for (ms in mSectorsBySubNum[sNum]) ms.len += dl
				}
			}
		}
		return maxLen
	}

	val kLen = calcRecurs(goodSectors.toSet(), 0.0, 0, minLimit1, maxLimit)
	println(kLen / PI)
}
