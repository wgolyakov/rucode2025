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
		var interLeft = listOf<MultiSector>()
		var interRight = listOf<MultiSector>()
		fun isNotEmpty() = !range.isEmpty()
		override fun toString() = range.toString()
	}

	class SectorInters(val sector: MultiSector, val interLeft: Set<MultiSector>, val interRight: List<MultiSector>) {
		override fun toString() = sector.toString()
	}

	class LastAdd(val first: MultiSector, val last: MultiSector) {
		constructor(sectors: List<MultiSector>): this(sectors.first(), sectors.last())
		constructor(sector: MultiSector): this(sector, sector)
		override fun toString() = "$first - $last"
	}

	class FenwickTree(val size: Int) : Cloneable {
		private val tree = IntArray(size + 1)

		/**
		 * @param index The 1-based upper bound of the range.
		 * @return The sum of elements in the range [1, index].
		 */
		private fun prefixSum(index: Int): Int {
			var sum = 0
			var i = index
			while (i > 0) {
				sum += tree[i]
				i -= i and -i
			}
			return sum
		}

		/**
		 * @param range The 0-based range.
		 * @return The sum of elements in the range.
		 */
		fun rangeSum(range: IntRange): Int {
			return prefixSum(range.last + 1) - prefixSum(range.first)
		}

		/**
		 * Adds a value to the element at the specified index and updates subsequent elements in the tree.
		 * @param index The 1-based index to update.
		 * @param value The value to add (can be positive or negative).
		 */
		fun add(index: Int, value: Int) {
			var i = index
			while (i <= size) {
				tree[i] += value
				i += i and -i
			}
		}

		public override fun clone(): FenwickTree{
			val ft = FenwickTree(size)
			tree.copyInto(ft.tree)
			return ft
		}
	}

	class LenCounter(val goodSectors: List<MultiSector>,
					 private val allSubSectors: Array<Sector>,
					 private val subSectorsCount: IntArray = IntArray(allSubSectors.size),
					 private val fenwickTree: FenwickTree = FenwickTree(allSubSectors.size)
	) : Cloneable {
		fun lenRest(s: MultiSector) = s.len - fenwickTree.rangeSum(s.range)

		fun maxPlusSector(unusedSectors: Collection<MultiSector>): MultiSector {
			return unusedSectors.maxWith(compareBy({ lenRest(it) }, { it.b }))
		}

		fun plusUsed(s: MultiSector): Int {
			val len = lenRest(s)
			for (sNum in s.range) {
				if (subSectorsCount[sNum] == 0) fenwickTree.add(sNum + 1, allSubSectors[sNum].len)
				subSectorsCount[sNum]++
			}
			return len
		}

		fun minusUsed(s: MultiSector): Int {
			for (sNum in s.range) {
				subSectorsCount[sNum]--
				if (subSectorsCount[sNum] == 0) fenwickTree.add(sNum + 1, -allSubSectors[sNum].len)
			}
			return lenRest(s)
		}

		public override fun clone() = LenCounter(goodSectors, allSubSectors, subSectorsCount.clone(), fenwickTree.clone())
	}

	class Chain : Cloneable {
		var len = 0
		var limit = 0
		var lens = mutableListOf<Int>()
		var remove = mutableListOf<MultiSector>()
		var add = mutableListOf<MultiSector>()
		fun size() = remove.size
		override fun toString() = (remove.firstOrNull()?.toString() ?: "") + "(${size()})"

		fun apply(lenCounter: LenCounter) {
			for (s in add) lenCounter.plusUsed(s)
			for (s in remove) lenCounter.minusUsed(s)
		}

		public override fun clone(): Chain {
			val c = Chain()
			c.len = len
			c.limit = limit
			c.lens.addAll(lens)
			c.remove.addAll(remove)
			c.add.addAll(add)
			return c
		}
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
					if (m.b == 0) m.a = a
					m.b = b
				}
			}
		}
		val goodSectors = mSectors.filter { it.isNotEmpty() }.sortedBy { it.a }
		for ((i, s) in goodSectors.withIndex()) {
			s.n = i
			s.len = s.b - s.a
		}
		return goodSectors to allSubSectors.toTypedArray()
	}

	fun continueChain(chain: Chain, j0: Int, lastInter: MultiSector, usedWithInter: List<SectorInters>,
					  lenCounter: LenCounter, currLen: Int, limit: Int) {
		var dLen = chain.len
		var j = j0
		var s1 = lastInter
		while (j < usedWithInter.size) {
			val s = usedWithInter[j]
			if (s1 !in s.interLeft) break
			dLen -= lenCounter.minusUsed(s.sector)
			val s2 = lenCounter.maxPlusSector(s.interRight)
			if (currLen + dLen + lenCounter.lenRest(s2) < limit) {
				chain.limit = limit - currLen
				break
			}
			dLen += lenCounter.lenRest(s2)
			lenCounter.plusUsed(s2)
			chain.remove.add(s.sector)
			chain.add.add(s2)
			chain.len = dLen
			chain.lens.add(chain.len)
			j++
			s1 = s2
		}
	}

	fun createChains(chainArr: Array<MutableList<Chain>>, j: Int, usedWithInter: List<SectorInters>,
					 lenCounter: LenCounter, currLen: Int, limit: Int) {
		var dLen = 0
		val s = usedWithInter[j]
		val chains = chainArr[s.sector.n]
		chains.clear()
		dLen -= lenCounter.minusUsed(s.sector)
		for (s1 in s.interLeft) {
			dLen += lenCounter.plusUsed(s1)
			for (s2 in s.interRight) {
				dLen += lenCounter.plusUsed(s2)
				val chain = Chain()
				chain.remove.add(s.sector)
				chain.add.add(s1)
				chain.add.add(s2)
				chain.len = dLen
				chain.lens.add(chain.len)
				val chain1 = chain.clone()
				chains.add(chain1)
				continueChain(chain, j + 1, s2, usedWithInter, lenCounter.clone(), currLen, limit)
				if (chain.size() > chain1.size()) chains.add(chain) else chain1.limit = chain.limit
				dLen -= lenCounter.minusUsed(s2)
			}
			dLen -= lenCounter.minusUsed(s1)
		}
		lenCounter.plusUsed(s.sector)
	}

	fun updateChains(chainArr: Array<MutableList<Chain>>, j: Int, jBefore: Int, usedWithInter: List<SectorInters>,
					 lenCounter: LenCounter, currLen: Int, limit: Int) {
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
				val lenCounterClone = lenCounter.clone()
				chain.apply(lenCounterClone)
				continueChain(chain, jBefore, lastInter, usedWithInter, lenCounterClone, currLen, limit)
			}
		} else {
			chains.removeIf { it.size() > 1 }
			for (chain in chains.toList()) {
				val lastInter = chain.add.last()
				val lenCounterClone = lenCounter.clone()
				chain.apply(lenCounterClone)
				val chain2 = chain.clone()
				continueChain(chain2, j + chain.size(), lastInter, usedWithInter, lenCounterClone, currLen, limit)
				if (chain2.size() > chain.size()) chains.add(chain2) else chain.limit = chain2.limit
			}
		}
	}

	fun enlargeChains(chainArr: Array<MutableList<Chain>>, j: Int, usedWithInter: List<SectorInters>,
					  lenCounter: LenCounter, currLen: Int, limit: Int) {
		val s = usedWithInter[j].sector
		val chains = chainArr[s.n]
		val limitedChains = chains.filter { it.limit + currLen > limit }
		for (chain in limitedChains) {
			chain.limit = 0
			val lastInter = chain.add.last()
			val lenCounterClone = lenCounter.clone()
			chain.apply(lenCounterClone)
			val chain2 = if (chain.size() > 1) chain else chain.clone()
			continueChain(chain2, j + chain.size(), lastInter, usedWithInter, lenCounterClone, currLen, limit)
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
					 lenCounter: LenCounter, kLen: Int, maxLen: Int): Chain? {
		val usedWithInter = filterIntersectors(usedArr, chainArr, lenCounter.goodSectors)
		if (usedWithInter.isEmpty()) return null
		var jBefore = usedWithInter.indexOfLast { it.sector.a < lastAdd.first.a }
		if (jBefore == -1) jBefore = 0
		var jAfter = usedWithInter.indexOfFirst { it.sector.b > lastAdd.last.b }
		if (jAfter == -1) jAfter = usedWithInter.lastIndex
		for (j in 0 until jBefore) {
			updateChains(chainArr, j, jBefore, usedWithInter, lenCounter, kLen, maxLen)
		}
		for (j in jBefore .. jAfter) {
			createChains(chainArr, j, usedWithInter, lenCounter, kLen, maxLen)
		}
		for (j in usedWithInter.indices) {
			enlargeChains(chainArr, j, usedWithInter, lenCounter, kLen, maxLen)
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
	val lenCounter = LenCounter(goodSectors, allSubSectors)
	val chainArr = Array(n) { mutableListOf<Chain>() }
	var lastAdd = LastAdd(goodSectors.first())
	for (i in 1 .. k) {
		// 1 way: Add one new sector with maximum length increase
		val sMax = lenCounter.maxPlusSector(unusedSectors)
		val sMaxLen = lenCounter.lenRest(sMax)

		// 2 way: Replace several sequential sectors with their sequential intersectors plus one
		val maxChain = findMaxChain(usedArr, chainArr, lastAdd, lenCounter, kLen, kLen + sMaxLen)

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
			maxChain.apply(lenCounter)
			lastAdd = LastAdd(maxChain.add)
		} else {
			kLen += sMaxLen
			usedArr[sMax.n] = true
			unusedSectors.remove(sMax)
			lenCounter.plusUsed(sMax)
			lastAdd = LastAdd(sMax)
		}
		if (kLen >= maxLimit) break
	}

	println(kLen.toDouble() / pi)
}
