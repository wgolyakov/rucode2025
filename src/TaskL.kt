import kotlin.math.*

// Main idea. Add next sector to maximum union in two alternative ways:
// 1) Add one new sector with maximum length increase
// 2) Replace several sequential sectors with their sequential intersectors plus one
fun main() {
	val e = 0.000000001

	data class Sector(val a: Double, val b: Double, val n: Int = -1) {
		val len = b - a
		operator fun contains(x: Double) = x in a - e .. b + e
		operator fun contains(s: Sector) = a - e <= s.a && s.b <= b + e
	}

	class MultiSector {
		var range = IntRange.EMPTY
		var a = 0.0
		var b = 0.0
		var len = 0.0
		var interLeft = setOf<MultiSector>()
		var interRight = setOf<MultiSector>()
		fun isNotEmpty() = !range.isEmpty()
		override fun toString() = range.toString()
		fun len() = b - a
	}

	class SectorInters(val sector: MultiSector, var interLeft: Set<MultiSector>, var interRight: Set<MultiSector>)

	fun cutSectors(sectors: Array<Sector>): List<Sector> {
		val result = mutableListOf<Sector>()
		for (s in sectors) {
			if (s.b < 0.0 + e || s.a > PI - e) continue
			val a = if (s.a < 0.0 + e) 0.0 else s.a
			val b = if (s.b > PI - e) PI else s.b
			if (abs(b - a) < e) continue
			result.add(Sector(a, b))
		}
		return result
	}

	fun removeContainedSectors(sectors: List<Sector>): List<Sector> {
		val distinct = mutableListOf<Sector>()
		for ((i, s) in sectors.withIndex()) {
			var duplicate = false
			for (s2 in sectors.subList(i + 1, sectors.size)) {
				if (abs(s.a - s2.a) < e && abs(s.b - s2.b) < e) {
					duplicate = true
					break
				}
			}
			if (!duplicate) distinct.add(s)
		}
		val result = mutableListOf<Sector>()
		for (s in distinct) {
			if (distinct.all { s === it || s !in it }) result.add(s)
		}
		return result
	}

	fun convert(sectors: List<Sector>): Pair<List<MultiSector>, Array<Sector>> {
		val allSubSectors = mutableListOf<Sector>()
		val mSectors = Array(sectors.size) { MultiSector() }
		val points = mutableSetOf<Double>()
		for (s in sectors) {
			points.add(s.a)
			points.add(s.b)
		}
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
					m.range = if (m.range.isEmpty()) n .. n else m.range.first .. n
					m.len += b - a
					if (m.b == 0.0) m.a = a
					m.b = b
				}
			}
		}
		val goodSectors = mSectors.filter { it.isNotEmpty() }
		return goodSectors to allSubSectors.toTypedArray()
	}

	fun calcByMaxLen(sectors: List<MultiSector>, allSubSectors: Array<Sector>,
					 mSectorsBySubNum: Array<MutableList<MultiSector>>, k: Int): Double {
		val sectorsSet = sectors.toMutableSet()
		val subSectorsCount = IntArray(allSubSectors.size)
		val usedSectors = mutableListOf<MultiSector>()
		var kLen = 0.0
		var count = 0
		while (count < k && sectorsSet.isNotEmpty()) {
			val s = sectorsSet.maxBy { it.len }
			if (s.len < e) break
			kLen += s.len
			for (sNum in s.range) {
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
			for (sNum in s.range) {
				subSectorsCount[sNum]--
				if (subSectorsCount[sNum] == 0) {
					val dl = allSubSectors[sNum].len
					for (ms in mSectorsBySubNum[sNum]) ms.len += dl
				}
			}
		}
		return kLen
	}

	fun maxPlusSector(unusedSectors: Collection<MultiSector>, allSubSectors: Array<Sector>,
					  subSectorsCount: IntArray): MultiSector {
		for (s in unusedSectors) {
			s.len = s.range.filter { subSectorsCount[it] == 0 }.sumOf { allSubSectors[it].len }
		}
		return unusedSectors.maxBy { it.len }
	}

	fun minus(s: MultiSector, allSubSectors: Array<Sector>, subSectorsCount: IntArray): Double {
		var len = 0.0
		for (sNum in s.range) {
			subSectorsCount[sNum]--
			if (subSectorsCount[sNum] == 0) len += allSubSectors[sNum].len
		}
		return len
	}

	fun plus(s: MultiSector, allSubSectors: Array<Sector>, subSectorsCount: IntArray): Double {
		var len = 0.0
		for (sNum in s.range) {
			if (subSectorsCount[sNum] == 0) len += allSubSectors[sNum].len
			subSectorsCount[sNum]++
		}
		return len
	}

	fun length(s: MultiSector, allSubSectors: Array<Sector>, subSectorsCount: IntArray): Double {
		return s.range.filter { subSectorsCount[it] == 0 }.sumOf { allSubSectors[it].len }
	}

	fun checkIntersectorsRecurs(v: Int, lastInter: MultiSector?, usedWithInter: List<SectorInters>,
								usedSet: MutableSet<MultiSector>, allSubSectors: Array<Sector>,
								subSectorsCount: IntArray, currLen: Double,
								limit: Double): Pair<Double, Set<MultiSector>> {
		var maxLen = limit
		var maxSectors = setOf<MultiSector>()
		var useLen = currLen
		val s = usedWithInter[v]
		if (lastInter == null) {
			usedSet.remove(s.sector)
			useLen -= minus(s.sector, allSubSectors, subSectorsCount)
			for (s1 in s.interLeft) {
				usedSet.add(s1)
				useLen += plus(s1, allSubSectors, subSectorsCount)
				for (s2 in s.interRight) {
					usedSet.add(s2)
					useLen += plus(s2, allSubSectors, subSectorsCount)
					if (useLen > maxLen + e) {
						maxLen = useLen
						maxSectors = usedSet.toSet()
					}
					if (v + 1 < usedWithInter.size) {
						val (len, sec) = checkIntersectorsRecurs(v + 1, s2, usedWithInter, usedSet,
							allSubSectors, subSectorsCount, useLen, maxLen)
						if (len > maxLen + e) {
							maxLen = len
							maxSectors = sec
						}
					}
					useLen -= minus(s2, allSubSectors, subSectorsCount)
					usedSet.remove(s2)
				}
				useLen -= minus(s1, allSubSectors, subSectorsCount)
				usedSet.remove(s1)
			}
			usedSet.add(s.sector)
			useLen += plus(s.sector, allSubSectors, subSectorsCount)
		} else {
			if (lastInter in s.interLeft) {
				usedSet.remove(s.sector)
				useLen -= minus(s.sector, allSubSectors, subSectorsCount)
				val s2 = s.interRight.map { it to useLen + length(it, allSubSectors, subSectorsCount) }
					.filter { it.second + e >= maxLen }.maxByOrNull { it.second }?.first
				if (s2 != null) {
					usedSet.add(s2)
					useLen += plus(s2, allSubSectors, subSectorsCount)
					if (useLen > maxLen + e) {
						maxLen = useLen
						maxSectors = usedSet.toSet()
					}
					if (v + 1 < usedWithInter.size) {
						val (len, sec) = checkIntersectorsRecurs(v + 1, s2, usedWithInter, usedSet,
							allSubSectors, subSectorsCount, useLen, maxLen)
						if (len > maxLen + e) {
							maxLen = len
							maxSectors = sec
						}
					}
					useLen -= minus(s2, allSubSectors, subSectorsCount)
					usedSet.remove(s2)
				}
				useLen += plus(s.sector, allSubSectors, subSectorsCount)
				usedSet.add(s.sector)
			}
		}
		return maxLen to maxSectors
	}

	// Sorted used sectors with unused intersectors
	fun filterIntersectors(usedSet: Set<MultiSector>): List<SectorInters> {
		val usedWithInter = mutableListOf<SectorInters>()
		val usedSorted = usedSet.sortedBy { it.a }
		for (s in usedSorted) {
			val interLeft = s.interLeft - usedSet
			if (interLeft.isEmpty()) continue
			val interRight = s.interRight - usedSet
			if (interRight.isEmpty()) continue
			usedWithInter.add(SectorInters(s, interLeft, interRight))
		}
		return usedWithInter
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

	val cutSectors = cutSectors(sectors)
	val largeSectors = removeContainedSectors(cutSectors)
	val (goodSectors, allSubSectors) = convert(largeSectors)

	val maxLimit = allSubSectors.sumOf { it.len }
	if (goodSectors.size <= k) {
		println(maxLimit / PI)
		return
	}

	val mSectorsBySubNum = Array(allSubSectors.size) { mutableListOf<MultiSector>() }
	for (ms in goodSectors) {
		for (subNum in ms.range) mSectorsBySubNum[subNum].add(ms)
	}

	val minLimit = calcByMaxLen(goodSectors, allSubSectors, mSectorsBySubNum, k)
	if (minLimit > maxLimit - e) {
		println(minLimit / PI)
		return
	}

	for (s in goodSectors) {
		val inter = s.range.flatMap { mSectorsBySubNum[it] }.toSet() - s
		s.interLeft = inter.filter { it.a < s.a }.toSet()
		s.interRight = inter.filter { it.b > s.b }.toSet()
	}

	var kLen = 0.0
	var unusedSectors = goodSectors
	var usedSet = setOf<MultiSector>()
	val sRemoved = mutableSetOf<MultiSector>()
	for (i in 1 .. k) {
		val subSectorsCount = IntArray(allSubSectors.size)
		for (s in usedSet) {
			for (sNum in s.range) subSectorsCount[sNum]++
		}

		// 1 way: Add one new sector with maximum length increase
		val sMax = maxPlusSector(unusedSectors, allSubSectors, subSectorsCount)
		var maxLen = kLen + sMax.len
		var maxSectors = usedSet + sMax

		// 2 way: Replace several sequential sectors with their sequential intersectors plus one
		val usedWithInter = filterIntersectors(usedSet)
		val usedMutable = usedSet.toMutableSet()
		for (j in usedWithInter.indices) {
			if (usedWithInter[j].sector in sRemoved) continue
			val (len2, sec2) = checkIntersectorsRecurs(j, null, usedWithInter, usedMutable,
				allSubSectors, subSectorsCount, kLen, maxLen)
			if (len2 > maxLen + e) {
				maxLen = len2
				maxSectors = sec2
			}
		}
		sRemoved.addAll(usedSet - maxSectors)

		usedSet = maxSectors
		unusedSectors = goodSectors - usedSet
		kLen = maxLen
	}

	println(kLen / PI)
}
