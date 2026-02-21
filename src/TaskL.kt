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
	}

	class SectorInters(val sector: MultiSector, val interLeft: Set<MultiSector>, val interRight: Set<MultiSector>) {
		override fun toString() = sector.toString()
	}

	class Chain {
		var len = 0.0
		var limit = 0.0
		var lens = mutableListOf<Double>()
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

	fun maxPlusSector(unusedSectors: Collection<MultiSector>, allSubSectors: Array<Sector>,
					  subSectorsCount: IntArray): MultiSector? {
		for (s in unusedSectors) {
			s.len = s.range.filter { subSectorsCount[it] == 0 }.sumOf { allSubSectors[it].len }
		}
		return unusedSectors.maxByOrNull { it.len }
	}

	fun plusLen(s: MultiSector, allSubSectors: Array<Sector>, subSectorsCount: IntArray): Double {
		var len = 0.0
		for (sNum in s.range) {
			if (subSectorsCount[sNum] == 0) len += allSubSectors[sNum].len
			subSectorsCount[sNum]++
		}
		return len
	}

	fun minusLen(s: MultiSector, allSubSectors: Array<Sector>, subSectorsCount: IntArray): Double {
		var len = 0.0
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
					  allSubSectors: Array<Sector>, subSectorsCount: IntArray, currLen: Double, limit: Double) {
		var dLen = chain.len
		var j = j0
		var s1 = lastInter
		while (j < usedWithInter.size) {
			val s = usedWithInter[j]
			if (s1 !in s.interLeft) break
			dLen -= minusLen(s.sector, allSubSectors, subSectorsCount)
			val s2 = maxPlusSector(s.interRight, allSubSectors, subSectorsCount) ?: break
			if (currLen + dLen + s2.len + e < limit) {
				chain.limit = limit - currLen
				break
			}
			dLen += s2.len
			plus(s2, subSectorsCount)
			chain.remove.add(s.sector)
			chain.add.add(s2)
			chain.len = dLen
			chain.lens.add(chain.len)
			j++
			s1 = s2
		}
	}

	fun createChains(chainMap: MutableMap<MultiSector, MutableList<Chain>>, j: Int, usedWithInter: List<SectorInters>,
					 allSubSectors: Array<Sector>, subSectorsCount: IntArray, currLen: Double, limit: Double) {
		var dLen = 0.0
		val s = usedWithInter[j]
		val chains = mutableListOf<Chain>()
		chainMap[s.sector] = chains
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

	fun updateChains(chains: MutableList<Chain>, j: Int, jBefore: Int, usedWithInter: List<SectorInters>,
					 allSubSectors: Array<Sector>, subSectorsCount: IntArray, currLen: Double, limit: Double) {
		val goodSize = jBefore - j
		if (goodSize > 1) {
			val affectedChains = chains.filter { it.size() > goodSize }
			for (chain in affectedChains) {
				chain.remove = chain.remove.subList(0, goodSize).toMutableList()
				chain.lens = chain.lens.subList(0, goodSize).toMutableList()
				chain.len = chain.lens.last()
				chain.limit = 0.0
				chain.add = chain.add.subList(0, goodSize + 1).toMutableList()
				val lastInter = chain.add.last()
				val subSectorsCountClone = subSectorsCount.clone()
				chain.apply(subSectorsCountClone)
				continueChain(
					chain, jBefore, lastInter, usedWithInter,
					allSubSectors, subSectorsCountClone, currLen, limit
				)
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

	fun enlargeChains(chains: MutableList<Chain>, j: Int, usedWithInter: List<SectorInters>,
					  allSubSectors: Array<Sector>, subSectorsCount: IntArray, currLen: Double, limit: Double) {
		val limitedChains = chains.filter { it.limit + currLen > limit + e }
		for (chain in limitedChains) {
			chain.limit = 0.0
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
	fun filterIntersectors(usedSet: Set<MultiSector>,
						   chainMap: MutableMap<MultiSector, MutableList<Chain>>): List<SectorInters> {
		val usedWithInter = mutableListOf<SectorInters>()
		val usedSorted = usedSet.sortedBy { it.a }
		for (s in usedSorted) {
			val interLeft = s.interLeft - usedSet
			if (interLeft.isEmpty())  {
				chainMap.remove(s)
				continue
			}
			val interRight = s.interRight - usedSet
			if (interRight.isEmpty()) {
				chainMap.remove(s)
				continue
			}
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
		s1.interLeft = interLeft.toSet()
		s1.interRight = interRight.toSet()
	}

	var kLen = 0.0
	var unusedSectors = goodSectors
	var usedSet = setOf<MultiSector>()
	val subSectorsCount = IntArray(allSubSectors.size)
	val chainMap = mutableMapOf<MultiSector, MutableList<Chain>>()
	lateinit var lastAdd: LastAdd
	for (i in 1 .. k) {
		// 1 way: Add one new sector with maximum length increase
		val sMax = maxPlusSector(unusedSectors, allSubSectors, subSectorsCount)!!
		var maxLen = kLen + sMax.len
		var maxSectors = usedSet + sMax

		// 2 way: Replace several sequential sectors with their sequential intersectors plus one
		var maxChain: Chain? = null
		val usedWithInter = filterIntersectors(usedSet, chainMap)
		if (usedWithInter.isNotEmpty()) {
			var jBefore = usedWithInter.indexOfLast { it.sector.a + e < lastAdd.first.a }
			if (jBefore == -1) jBefore = 0
			var jAfter = usedWithInter.indexOfFirst { it.sector.b > lastAdd.last.b + e }
			if (jAfter == -1) jAfter = usedWithInter.lastIndex
			for (j in 0 until jBefore) {
				val s = usedWithInter[j].sector
				val chains = chainMap[s] ?: continue
				updateChains(chains, j, jBefore, usedWithInter, allSubSectors, subSectorsCount, kLen, maxLen)
			}
			for (j in jBefore .. jAfter) {
				createChains(chainMap, j, usedWithInter, allSubSectors, subSectorsCount, kLen, maxLen)
			}
			for ((j, entry) in chainMap.entries.sortedBy { it.key.a }.withIndex()) {
				enlargeChains(entry.value, j, usedWithInter, allSubSectors, subSectorsCount, kLen, maxLen)
			}
			maxChain = chainMap.maxBy { e -> e.value.maxOfOrNull { it.len } ?: 0.0 }.value.maxByOrNull { it.len }
		}
		if (maxChain != null && kLen + maxChain.len > maxLen + e) {
			maxLen = kLen + maxChain.len
			maxSectors = usedSet - maxChain.remove.toSet() + maxChain.add
			maxChain.apply(subSectorsCount)
			lastAdd = LastAdd(maxChain.add)
			for (s in maxChain.remove) chainMap.remove(s)
		} else {
			plus(sMax, subSectorsCount)
			lastAdd = LastAdd(sMax)
		}
		usedSet = maxSectors
		unusedSectors = goodSectors - usedSet
		kLen = maxLen
		if (kLen >= maxLimit - e) break
	}

	println(kLen / PI)
}
