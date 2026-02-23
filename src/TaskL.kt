import kotlin.math.*

// Main idea. Add next sector to maximum union in two alternative ways:
// 1) Add one new sector with maximum length increase
// 2) Replace several sequential sectors with their sequential intersectors plus one
fun main() {
	fun round(x: Double) = (x * 10_000_000).roundToInt()
	val pi = round(PI)

	data class Sector(val a: Int, val b: Int, val n: Int = -1) {
		constructor(a: Double, b: Double) : this(round(a), round(b))
		val len = b - a
		operator fun contains(x: Int) = x in a .. b
		operator fun contains(s: Sector) = a <= s.a && s.b <= b
	}

	class MultiSector {
		var n = -1
		var range = IntRange.EMPTY
		var a = 0
		var b = 0
		var len = 0
		var lenRest = 0
		var interLeft = listOf<MultiSector>()
		var interRight = listOf<MultiSector>()
		fun isNotEmpty() = !range.isEmpty()
		override fun toString() = range.toString()
	}

	class SectorInters(val sector: MultiSector, val interLeft: Set<MultiSector>, val interRight: List<MultiSector>) {
		override fun toString() = sector.toString()
	}

	class Chain {
		var len = 0
		var limit = 0
		var lens = mutableListOf<Int>()
		var remove = mutableListOf<MultiSector>()
		var add = mutableListOf<MultiSector>()
		fun size() = remove.size
		override fun toString() = (remove.firstOrNull()?.toString() ?: "") + "(${size()})"

		fun apply(subSectorsCount: IntArray) {
			for (s in remove) {
				for (sNum in s.range) subSectorsCount[sNum]--
			}
			for (s in add) {
				for (sNum in s.range) subSectorsCount[sNum]++
			}
		}

		fun clone(): Chain {
			val c = Chain()
			c.len = len
			c.limit = limit
			c.lens.addAll(lens)
			c.remove.addAll(remove)
			c.add.addAll(add)
			return c
		}
	}

	class LastAdd(val first: MultiSector, val last: MultiSector) {
		constructor(sectors: List<MultiSector>): this(sectors.first(), sectors.last())
		constructor(sector: MultiSector): this(sector, sector)
		override fun toString() = "$first - $last"
	}

	fun cutSectors(sectors: Array<Sector>): List<Sector> {
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

	fun removeContainedSectors(sectors: List<Sector>): List<Sector> {
		val distinct = mutableListOf<Sector>()
		for ((i, s) in sectors.withIndex()) {
			var duplicate = false
			for (s2 in sectors.subList(i + 1, sectors.size)) {
				if (s.a == s2.a && s.b == s2.b) {
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
		val points = mutableSetOf<Int>()
		for (s in sectors) {
			points.add(s.a)
			points.add(s.b)
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
					if (m.b == 0) m.a = a
					m.b = b
				}
			}
		}
		val goodSectors = mSectors.filter { it.isNotEmpty() }.sortedBy { it.a }
		for ((i, s) in goodSectors.withIndex()) s.n = i
		return goodSectors to allSubSectors.toTypedArray()
	}

	fun maxPlusSector(unusedSectors: Collection<MultiSector>, allSubSectors: Array<Sector>,
					  subSectorsCount: IntArray): MultiSector {
		for (s in unusedSectors) {
			s.lenRest = s.len - s.range.filter { subSectorsCount[it] != 0 }.sumOf { allSubSectors[it].len }
		}
		return unusedSectors.maxWith(compareBy({ it.lenRest }, { it.b }))
	}

	fun plusLen(s: MultiSector, allSubSectors: Array<Sector>, subSectorsCount: IntArray): Int {
		var len = 0
		for (sNum in s.range) {
			if (subSectorsCount[sNum] == 0) len += allSubSectors[sNum].len
			subSectorsCount[sNum]++
		}
		return len
	}

	fun minusLen(s: MultiSector, allSubSectors: Array<Sector>, subSectorsCount: IntArray): Int {
		var len = 0
		for (sNum in s.range) {
			subSectorsCount[sNum]--
			if (subSectorsCount[sNum] == 0) len += allSubSectors[sNum].len
		}
		return len
	}

	fun plus(s: MultiSector, subSectorsCount: IntArray) {
		for (sNum in s.range) subSectorsCount[sNum]++
	}

	fun continueChain(chain: Chain, j0: Int, lastInter: MultiSector, usedWithInter: List<SectorInters>,
					  allSubSectors: Array<Sector>, subSectorsCount: IntArray, currLen: Int, limit: Int) {
		var dLen = chain.len
		var j = j0
		var s1 = lastInter
		while (j < usedWithInter.size) {
			val s = usedWithInter[j]
			if (s1 !in s.interLeft) break
			dLen -= minusLen(s.sector, allSubSectors, subSectorsCount)
			val s2 = maxPlusSector(s.interRight, allSubSectors, subSectorsCount)
			if (currLen + dLen + s2.lenRest < limit) {
				chain.limit = limit - currLen
				break
			}
			dLen += s2.lenRest
			plus(s2, subSectorsCount)
			chain.remove.add(s.sector)
			chain.add.add(s2)
			chain.len = dLen
			chain.lens.add(chain.len)
			j++
			s1 = s2
		}
	}

	fun createChains(chainArr: Array<MutableList<Chain>>, j: Int, usedWithInter: List<SectorInters>,
					 allSubSectors: Array<Sector>, subSectorsCount: IntArray, currLen: Int, limit: Int) {
		var dLen = 0
		val s = usedWithInter[j]
		val chains = chainArr[s.sector.n]
		chains.clear()
		dLen -= minusLen(s.sector, allSubSectors, subSectorsCount)
		for (s1 in s.interLeft) {
			dLen += plusLen(s1, allSubSectors, subSectorsCount)
			for (s2 in s.interRight) {
				dLen += plusLen(s2, allSubSectors, subSectorsCount)
				val chain = Chain()
				chain.remove.add(s.sector)
				chain.add.add(s1)
				chain.add.add(s2)
				chain.len = dLen
				chain.lens.add(chain.len)
				val chain1 = chain.clone()
				chains.add(chain1)
				continueChain(chain, j + 1, s2, usedWithInter,
					allSubSectors, subSectorsCount.clone(), currLen, limit)
				if (chain.size() > chain1.size()) chains.add(chain) else chain1.limit = chain.limit
				dLen -= minusLen(s2, allSubSectors, subSectorsCount)
			}
			dLen -= minusLen(s1, allSubSectors, subSectorsCount)
		}
		plus(s.sector, subSectorsCount)
	}

	fun updateChains(chainArr: Array<MutableList<Chain>>, j: Int, jBefore: Int, usedWithInter: List<SectorInters>,
					 allSubSectors: Array<Sector>, subSectorsCount: IntArray, currLen: Int, limit: Int) {
		val s = usedWithInter[j].sector
		val chains = chainArr[s.n]
		val goodSize = jBefore - j
		if (goodSize > 1) {
			val affectedChains = chains.filter { it.size() > goodSize }
			for (chain in affectedChains) {
				chain.remove = chain.remove.subList(0, goodSize).toMutableList()
				chain.lens = chain.lens.subList(0, goodSize).toMutableList()
				chain.len = chain.lens.last()
				chain.limit = 0
				chain.add = chain.add.subList(0, goodSize + 1).toMutableList()
				val lastInter = chain.add.last()
				val subSectorsCountClone = subSectorsCount.clone()
				chain.apply(subSectorsCountClone)
				continueChain(chain, jBefore, lastInter, usedWithInter,
					allSubSectors, subSectorsCountClone, currLen, limit)
			}
		} else {
			chains.removeIf { it.size() > 1 }
			for (chain in chains.toList()) {
				val lastInter = chain.add.last()
				val subSectorsCountClone = subSectorsCount.clone()
				chain.apply(subSectorsCountClone)
				val chain2 = chain.clone()
				continueChain(chain2, j + chain.size(), lastInter,
					usedWithInter, allSubSectors, subSectorsCountClone, currLen, limit)
				if (chain2.size() > chain.size()) chains.add(chain2) else chain.limit = chain2.limit
			}
		}
	}

	fun enlargeChains(chainArr: Array<MutableList<Chain>>, j: Int, usedWithInter: List<SectorInters>,
					  allSubSectors: Array<Sector>, subSectorsCount: IntArray, currLen: Int, limit: Int) {
		val s = usedWithInter[j].sector
		val chains = chainArr[s.n]
		val limitedChains = chains.filter { it.limit + currLen > limit }
		for (chain in limitedChains) {
			chain.limit = 0
			val lastInter = chain.add.last()
			val subSectorsCountClone = subSectorsCount.clone()
			chain.apply(subSectorsCountClone)
			val chain2 = if (chain.size() > 1) chain else chain.clone()
			continueChain(chain2, j + chain.size(), lastInter,
				usedWithInter, allSubSectors, subSectorsCountClone, currLen, limit)
			if (chain2.size() > chain.size()) chains.add(chain2) else chain.limit = chain2.limit
		}
	}

	// Sorted used sectors with unused intersectors
	fun filterIntersectors(usedArr: BooleanArray, chainArr: Array<MutableList<Chain>>,
						   goodSectors: List<MultiSector>): List<SectorInters> {
		val usedWithInter = mutableListOf<SectorInters>()
		for (s in goodSectors) {
			if (!usedArr[s.n]) continue
			val interLeft = s.interLeft.filter { !usedArr[it.n] }.toSet()
			if (interLeft.isEmpty())  {
				chainArr[s.n].clear()
				continue
			}
			val interRight = s.interRight.filter { !usedArr[it.n] }
			if (interRight.isEmpty()) {
				chainArr[s.n].clear()
				continue
			}
			usedWithInter.add(SectorInters(s, interLeft, interRight))
		}
		return usedWithInter
	}

	fun findMaxChain(usedArr: BooleanArray, chainArr: Array<MutableList<Chain>>, lastAdd: LastAdd,
					 allSubSectors: Array<Sector>, subSectorsCount: IntArray, goodSectors: List<MultiSector>,
					 kLen: Int, maxLen: Int): Chain? {
		val usedWithInter = filterIntersectors(usedArr, chainArr, goodSectors)
		if (usedWithInter.isEmpty()) return null
		var jBefore = usedWithInter.indexOfLast { it.sector.a < lastAdd.first.a }
		if (jBefore == -1) jBefore = 0
		var jAfter = usedWithInter.indexOfFirst { it.sector.b > lastAdd.last.b }
		if (jAfter == -1) jAfter = usedWithInter.lastIndex
		for (j in 0 until jBefore) {
			updateChains(chainArr, j, jBefore, usedWithInter, allSubSectors, subSectorsCount, kLen, maxLen)
		}
		for (j in jBefore .. jAfter) {
			createChains(chainArr, j, usedWithInter, allSubSectors, subSectorsCount, kLen, maxLen)
		}
		for (j in usedWithInter.indices) {
			enlargeChains(chainArr, j, usedWithInter, allSubSectors, subSectorsCount, kLen, maxLen)
		}
		return chainArr.maxBy { chains -> chains.maxOfOrNull { it.len } ?: 0 }.maxByOrNull { it.len }
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
		println(maxLimit.toDouble() / pi)
		return
	}

	for (s1 in goodSectors) {
		val interLeft = mutableListOf<MultiSector>()
		val interRight = mutableListOf<MultiSector>()
		for (s2 in goodSectors) {
			if (s2.a < s1.a && s1.a < s2.b) {
				interLeft.add(s2)
			} else if (s1.a < s2.a && s2.a < s1.b) {
				interRight.add(s2)
			}
		}
		s1.interLeft = interLeft
		s1.interRight = interRight
	}

	var kLen = 0
	val unusedSectors = goodSectors.toMutableSet()
	val usedArr = BooleanArray(n)
	val subSectorsCount = IntArray(allSubSectors.size)
	val chainArr = Array(n) { mutableListOf<Chain>() }
	var lastAdd = LastAdd(goodSectors.first())
	for (i in 1 .. k) {
		// 1 way: Add one new sector with maximum length increase
		val sMax = maxPlusSector(unusedSectors, allSubSectors, subSectorsCount)
		val sMaxLen = sMax.lenRest

		// 2 way: Replace several sequential sectors with their sequential intersectors plus one
		val maxChain = findMaxChain(usedArr, chainArr, lastAdd, allSubSectors, subSectorsCount,
			goodSectors, kLen, kLen + sMaxLen)

		if (maxChain != null && maxChain.len > sMaxLen) {
			kLen += maxChain.len
			for (s in maxChain.remove) {
				usedArr[s.n] = false
				chainArr[s.n].clear()
			}
			for (s in maxChain.add) {
				usedArr[s.n] = true
				unusedSectors.remove(s)
			}
			unusedSectors.addAll(maxChain.remove)
			maxChain.apply(subSectorsCount)
			lastAdd = LastAdd(maxChain.add)
		} else {
			kLen += sMaxLen
			usedArr[sMax.n] = true
			unusedSectors.remove(sMax)
			plus(sMax, subSectorsCount)
			lastAdd = LastAdd(sMax)
		}
		if (kLen >= maxLimit) break
	}

	println(kLen.toDouble() / pi)
}
