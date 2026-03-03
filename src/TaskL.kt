import kotlin.math.*

// if k < n/2 then add sectors to union in two alternative ways:
// 1) Add one sector with maximum length increase
// 2) Replace several sequential sectors with their sequential intersectors plus one
//
// if k >= n/2 then remove sectors from union in two alternative ways:
// 1) Remove one sector with minimum length decrease
// 2) Replace several sequential sectors with their sequential intersectors minus one
fun main() {
	fun round(x: Double) = (x * 10_000_000).roundToInt()
	val pi = round(PI)

	data class Sector(val a: Int, val b: Int, var n: Int = -1) {
		constructor(a: Double, b: Double) : this(round(a), round(b))
		val len = b - a
	}

	class MultiSector(val n: Int) {
		var range = IntRange.EMPTY
		var a = 0
		var b = 0
		var len = 0
		var interLeft = listOf<MultiSector>()
		var interRight = listOf<MultiSector>()
		override fun toString() = range.toString()
	}

	class SectorInters(val sector: MultiSector, val interLeft: Set<MultiSector>, val interRight: List<MultiSector>) {
		val a = interLeft.first().a
		val b = interRight.last().b
		override fun toString() = sector.toString()
	}

	class LastChange(val a: Int, val b: Int) {
		constructor(sectors: List<MultiSector>): this(sectors.first().a, sectors.last().b)
		constructor(sector: MultiSector): this(sector.a, sector.b)
		constructor(lc: LastChange, sector: MultiSector): this(min(lc.a, sector.a), max(lc.b, sector.b))
		override fun toString() = "$a - $b"
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

		public override fun clone(): FenwickTree {
			val ft = FenwickTree(size)
			tree.copyInto(ft.tree)
			return ft
		}
	}

	abstract class LenCounter(val mSectors: List<MultiSector>,
							  protected val allSubSectors: List<Sector>,
							  protected val subSectorsCount: IntArray,
							  protected val fenwickTree: FenwickTree) : Cloneable {
		abstract fun lenRest(s: MultiSector): Int
		abstract fun nextSector(sectors: Collection<MultiSector>): MultiSector
		abstract fun plus(s: MultiSector): Int
		abstract fun minus(s: MultiSector): Int
		public override fun clone() = super.clone() as LenCounter
	}

	class LenCounterUp(mSectors: List<MultiSector>,
					   allSubSectors: List<Sector>,
					   subSectorsCount: IntArray,
					   fenwickTree: FenwickTree
	) : LenCounter(mSectors, allSubSectors, subSectorsCount, fenwickTree) {

		constructor(mSectors: List<MultiSector>, allSubSectors: List<Sector>) :
				this(mSectors, allSubSectors, IntArray(allSubSectors.size), FenwickTree(allSubSectors.size))

		override fun lenRest(s: MultiSector) = s.len - fenwickTree.rangeSum(s.range)

		override fun nextSector(sectors: Collection<MultiSector>): MultiSector {
			return sectors.maxWith(compareBy({ lenRest(it) }, { it.b }))
		}

		override fun plus(s: MultiSector): Int {
			val len = lenRest(s)
			for (sNum in s.range) {
				if (subSectorsCount[sNum] == 0) fenwickTree.add(sNum + 1, allSubSectors[sNum].len)
				subSectorsCount[sNum]++
			}
			return len
		}

		override fun minus(s: MultiSector): Int {
			for (sNum in s.range) {
				subSectorsCount[sNum]--
				if (subSectorsCount[sNum] == 0) fenwickTree.add(sNum + 1, -allSubSectors[sNum].len)
			}
			return lenRest(s)
		}

		override fun clone() = LenCounterUp(mSectors, allSubSectors, subSectorsCount.clone(), fenwickTree.clone())
	}

	class LenCounterDown(mSectors: List<MultiSector>,
						 allSubSectors: List<Sector>,
						 subSectorsCount: IntArray,
						 fenwickTree: FenwickTree
	) : LenCounter(mSectors, allSubSectors, subSectorsCount, fenwickTree) {

		constructor(mSectors: List<MultiSector>, allSubSectors: List<Sector>) :
				this(mSectors, allSubSectors, IntArray(allSubSectors.size), FenwickTree(allSubSectors.size)) {
			for (s in mSectors) {
				for (sNum in s.range) subSectorsCount[sNum]++
			}
			for (sNum in subSectorsCount.indices) {
				if (subSectorsCount[sNum] == 1) fenwickTree.add(sNum + 1, allSubSectors[sNum].len)
			}
		}

		override fun lenRest(s: MultiSector) = fenwickTree.rangeSum(s.range)

		override fun nextSector(sectors: Collection<MultiSector>): MultiSector {
			return sectors.minWith(compareBy({ lenRest(it) }, { -it.b }))
		}

		override fun plus(s: MultiSector): Int {
			val len = lenRest(s)
			for (sNum in s.range) {
				if (subSectorsCount[sNum] == 1) fenwickTree.add(sNum + 1, -allSubSectors[sNum].len)
				subSectorsCount[sNum]--
				if (subSectorsCount[sNum] == 1) fenwickTree.add(sNum + 1, allSubSectors[sNum].len)
			}
			return len
		}

		override fun minus(s: MultiSector): Int {
			for (sNum in s.range) {
				if (subSectorsCount[sNum] == 1) fenwickTree.add(sNum + 1, -allSubSectors[sNum].len)
				subSectorsCount[sNum]++
				if (subSectorsCount[sNum] == 1) fenwickTree.add(sNum + 1, allSubSectors[sNum].len)
			}
			return lenRest(s)
		}

		override fun clone() = LenCounterDown(mSectors, allSubSectors, subSectorsCount.clone(), fenwickTree.clone())
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
			for (s in add) lenCounter.plus(s)
			for (s in remove) lenCounter.minus(s)
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
		val sorted = sectors.sortedWith(compareBy({ it.a }, { -it.b }))
		var bMax = -1
		return sorted.filter { if (it.b <= bMax) false else { bMax = it.b; true } }
	}

	fun convert(sectors: List<Sector>): Pair<List<MultiSector>, List<Sector>> {
		val allSubSectors = mutableListOf<Sector>()
		val mSectors = List(sectors.size) { MultiSector(it) }
		val points = mutableSetOf<Int>()
		for (s in sectors) {
			points.add(s.a)
			points.add(s.b)
		}
		for (i in sectors.indices) sectors[i].n = i
		val currSectors = mutableListOf<Sector>()
		var i = 0
		for ((a, b) in points.sorted().windowed(2)) {
			if (currSectors.isNotEmpty() && currSectors.first().b == a) currSectors.removeFirst()
			if (i < sectors.size && sectors[i].a == a) currSectors.add(sectors[i++])
			if (currSectors.isNotEmpty()) {
				val n = allSubSectors.size
				allSubSectors.add(Sector(a, b, n))
				for (sector in currSectors) {
					val m = mSectors[sector.n]
					m.range = if (m.range.isEmpty()) n..n else m.range.first..n
				}
			}
		}
		for (s in mSectors) {
			s.a = allSubSectors[s.range.first].a
			s.b = allSubSectors[s.range.last].b
			s.len = s.b - s.a
		}
		return mSectors to allSubSectors
	}

	fun continueChain(chain: Chain, j0: Int, lastInter: MultiSector, usedWithInter: List<SectorInters>,
					  lenCounter: LenCounter, limit: Int, up: Boolean) {
		var dLen = chain.len
		var currLimit = limit
		var j = j0
		var s1 = lastInter
		while (j < usedWithInter.size) {
			val s = usedWithInter[j]
			if (s1 !in s.interLeft) break
			if (up) {
				if (dLen > currLimit) currLimit = dLen
			} else {
				if (dLen < currLimit) currLimit = dLen
			}
			dLen -= lenCounter.minus(s.sector)
			val s2 = lenCounter.nextSector(s.interRight)
			dLen += lenCounter.plus(s2)
			if (dLen < 0) break
			if (up) {
				if (dLen < currLimit) {
					chain.limit = currLimit
					break
				}
			} else {
				if (dLen > currLimit) {
					chain.limit = currLimit
					break
				}
			}
			chain.remove.add(s.sector)
			chain.add.add(s2)
			chain.len = dLen
			chain.lens.add(chain.len)
			j++
			s1 = s2
		}
	}

	fun createChains(chainArr: Array<MutableList<Chain>>, j: Int, usedWithInter: List<SectorInters>,
					 lenCounter: LenCounter, limit: Int, up: Boolean) {
		var dLen = 0
		val s = usedWithInter[j]
		val chains = chainArr[s.sector.n]
		chains.clear()
		dLen -= lenCounter.minus(s.sector)
		for (s1 in s.interLeft) {
			dLen += lenCounter.plus(s1)
			for (s2 in s.interRight) {
				dLen += lenCounter.plus(s2)
				if (dLen >= 0) {
					val chain = Chain()
					chain.remove.add(s.sector)
					chain.add.add(s1)
					chain.add.add(s2)
					chain.len = dLen
					chain.lens.add(chain.len)
					chain.limit = if (up) 0 else Int.MAX_VALUE
					val chain1 = chain.clone()
					chains.add(chain1)
					continueChain(chain, j + 1, s2, usedWithInter, lenCounter.clone(), limit, up)
					if (chain.size() > chain1.size()) chains.add(chain) else chain1.limit = chain.limit
				}
				dLen -= lenCounter.minus(s2)
			}
			dLen -= lenCounter.minus(s1)
		}
		lenCounter.plus(s.sector)
	}

	fun updateChains(chainArr: Array<MutableList<Chain>>, j: Int, jBefore: Int, usedWithInter: List<SectorInters>,
					 lenCounter: LenCounter, limit: Int, up: Boolean) {
		val s = usedWithInter[j].sector
		val chains = chainArr[s.n]
		val goodSize = jBefore - j
		if (goodSize > 1) {
			val affectedChains = chains.filter { it.size() > goodSize }
			for (chain in affectedChains) {
				chain.remove = chain.remove.subList(0, goodSize).toMutableList()
				chain.lens = chain.lens.subList(0, goodSize).toMutableList()
				chain.len = chain.lens.last()
				chain.limit = if (up) 0 else Int.MAX_VALUE
				chain.add = chain.add.subList(0, goodSize + 1).toMutableList()
				val lastInter = chain.add.last()
				val lenCounterClone = lenCounter.clone()
				chain.apply(lenCounterClone)
				continueChain(chain, jBefore, lastInter, usedWithInter, lenCounterClone, limit, up)
			}
		} else {
			chains.removeIf { it.size() > 1 }
			for (chain in chains.toList()) {
				val lastInter = chain.add.last()
				val lenCounterClone = lenCounter.clone()
				chain.apply(lenCounterClone)
				val chain2 = chain.clone()
				continueChain(chain2, j + chain.size(), lastInter, usedWithInter, lenCounterClone, limit, up)
				if (chain2.size() > chain.size()) chains.add(chain2) else chain.limit = chain2.limit
			}
		}
	}

	fun enlargeChains(chainArr: Array<MutableList<Chain>>, j: Int, usedWithInter: List<SectorInters>,
					  lenCounter: LenCounter, limit: Int, up: Boolean) {
		val s = usedWithInter[j].sector
		val chains = chainArr[s.n]
		val limitedChains = if (up) {
			chains.filter { it.limit > limit }
		} else {
			chains.filter { it.limit < limit }
		}
		for (chain in limitedChains) {
			chain.limit = if (up) 0 else Int.MAX_VALUE
			val lastInter = chain.add.last()
			val lenCounterClone = lenCounter.clone()
			chain.apply(lenCounterClone)
			val chain2 = if (chain.size() > 1) chain else chain.clone()
			continueChain(chain2, j + chain.size(), lastInter, usedWithInter, lenCounterClone, limit, up)
			if (chain2.size() > chain.size()) chains.add(chain2) else chain.limit = chain2.limit
		}
	}

	fun filterIntersectors(filterArr: BooleanArray, chainArr: Array<MutableList<Chain>>,
						   mSectors: List<MultiSector>): List<SectorInters> {
		val usedWithInter = mutableListOf<SectorInters>()
		for (s in mSectors) {
			if (!filterArr[s.n]) continue
			val interLeft = s.interLeft.filter { !filterArr[it.n] }.toSet()
			if (interLeft.isEmpty())  {
				chainArr[s.n].clear()
				continue
			}
			val interRight = s.interRight.filter { !filterArr[it.n] }
			if (interRight.isEmpty()) {
				chainArr[s.n].clear()
				continue
			}
			usedWithInter.add(SectorInters(s, interLeft, interRight))
		}
		return usedWithInter
	}

	fun findMaxChain(usedArr: BooleanArray, chainArr: Array<MutableList<Chain>>, lastChange: LastChange,
					 lenCounter: LenCounter, limit: Int): Chain? {
		// Sorted used sectors with unused intersectors
		val usedWithInter = filterIntersectors(usedArr, chainArr, lenCounter.mSectors)
		if (usedWithInter.isEmpty()) return null
		var jBefore = usedWithInter.indexOfFirst { it.b > lastChange.a && it.a < lastChange.b }
		if (jBefore == -1) jBefore = 0
		var jAfter = usedWithInter.indexOfLast { it.a < lastChange.b && it.b > lastChange.a }
		if (jAfter == -1) jAfter = usedWithInter.lastIndex
		for (j in 0 until jBefore) {
			updateChains(chainArr, j, jBefore, usedWithInter, lenCounter, limit, true)
		}
		for (j in jBefore .. jAfter) {
			createChains(chainArr, j, usedWithInter, lenCounter, limit, true)
		}
		for (j in usedWithInter.indices) {
			enlargeChains(chainArr, j, usedWithInter, lenCounter, limit, true)
		}
		return chainArr.maxBy { chains -> chains.maxOfOrNull { it.len } ?: 0 }.maxByOrNull { it.len }
	}

	fun findMinChain(unusedArr: BooleanArray, chainArr: Array<MutableList<Chain>>, lastChange: LastChange,
					 lenCounter: LenCounter, limit: Int): Chain? {
		if (limit == 0) return null
		// Sorted unused sectors with used intersectors
		val unusedWithInter = filterIntersectors(unusedArr, chainArr, lenCounter.mSectors)
		if (unusedWithInter.isEmpty()) return null
		var jBefore = unusedWithInter.indexOfFirst { it.b > lastChange.a && it.a < lastChange.b }
		if (jBefore == -1) jBefore = 0
		var jAfter = unusedWithInter.indexOfLast { it.a < lastChange.b && it.b > lastChange.a }
		if (jAfter == -1) jAfter = unusedWithInter.lastIndex
		for (j in 0 until jBefore) {
			updateChains(chainArr, j, jBefore, unusedWithInter, lenCounter, limit, false)
		}
		for (j in jBefore .. jAfter) {
			createChains(chainArr, j, unusedWithInter, lenCounter, limit, false)
		}
		for (j in unusedWithInter.indices) {
			enlargeChains(chainArr, j, unusedWithInter, lenCounter, limit, false)
		}
		return chainArr.minBy { chains -> chains.minOfOrNull { it.len } ?: Int.MAX_VALUE }.minByOrNull { it.len }
	}

	fun calcUp(k: Int, mSectors: List<MultiSector>, allSubSectors: List<Sector>, maxLimit: Int): Int {
		val n = mSectors.size
		var kLen = 0
		val unusedSectors = mSectors.toMutableSet()
		val usedArr = BooleanArray(n)
		val lenCounter = LenCounterUp(mSectors, allSubSectors)
		val chainArr = Array(n) { mutableListOf<Chain>() }
		var lastChange = LastChange(0, 0)
		for (i in 1 .. k) {
			// 1 way: Add one sector with maximum length increase
			val sMax = lenCounter.nextSector(unusedSectors)
			val sMaxLen = lenCounter.lenRest(sMax)

			// 2 way: Replace several sequential sectors with their sequential intersectors plus one
			val maxChain = findMaxChain(usedArr, chainArr, lastChange, lenCounter, sMaxLen)

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
				lastChange = LastChange(maxChain.add)
			} else {
				kLen += sMaxLen
				usedArr[sMax.n] = true
				unusedSectors.remove(sMax)
				lenCounter.plus(sMax)
				lastChange = LastChange(sMax)
			}
			if (kLen >= maxLimit) break
		}
		return kLen
	}

	fun calcDown(k: Int, mSectors: List<MultiSector>, allSubSectors: List<Sector>, maxLimit: Int): Int {
		val n = mSectors.size
		var kLen = maxLimit
		val unusedArr = BooleanArray(n)
		val usedSectors = mSectors.toMutableSet()
		val lenCounter = LenCounterDown(mSectors, allSubSectors)
		val chainArr = Array(n) { mutableListOf<Chain>() }
		var lastChange = LastChange(0, 0)
		for (i in n - 1 downTo k) {
			// 1 way: Remove one sector with minimum length decrease
			val sMin = lenCounter.nextSector(usedSectors)
			val sMinLen = lenCounter.lenRest(sMin)

			// 2 way: Replace several sequential sectors with their sequential intersectors minus one
			val minChain = findMinChain(unusedArr, chainArr, lastChange, lenCounter, sMinLen)

			if (minChain != null && minChain.len < sMinLen) {
				kLen -= minChain.len
				for (s in minChain.remove) {
					unusedArr[s.n] = false
					chainArr[s.n].clear()
				}
				for (s in minChain.add) {
					unusedArr[s.n] = true
					usedSectors.remove(s)
				}
				usedSectors.addAll(minChain.remove)
				minChain.apply(lenCounter)
				lastChange = LastChange(minChain.add)
			} else {
				kLen -= sMinLen
				unusedArr[sMin.n] = true
				usedSectors.remove(sMin)
				lenCounter.plus(sMin)
				lastChange = if (minChain == null) LastChange(lastChange, sMin) else LastChange(sMin)
			}
			if (kLen <= 0) break
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

	val cutSectors = cutSectors(sectors)
	val largeSectors = removeContainedSectors(cutSectors)
	val (mSectors, allSubSectors) = convert(largeSectors)

	val maxLimit = allSubSectors.sumOf { it.len }
	if (mSectors.size <= k) {
		println(maxLimit.toDouble() / pi)
		return
	}

	for (s1 in mSectors) {
		val interLeft = mutableListOf<MultiSector>()
		val interRight = mutableListOf<MultiSector>()
		for (s2 in mSectors) {
			if (s2.a < s1.a && s1.a < s2.b) {
				interLeft.add(s2)
			} else if (s1.a < s2.a && s2.a < s1.b) {
				interRight.add(s2)
			}
		}
		s1.interLeft = interLeft
		s1.interRight = interRight
	}

	val kLen = if (k < mSectors.size / 2) {
		calcUp(k, mSectors, allSubSectors, maxLimit)
	} else {
		calcDown(k, mSectors, allSubSectors, maxLimit)
	}

	println(kLen.toDouble() / pi)
}
