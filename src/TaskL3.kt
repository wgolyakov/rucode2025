import kotlin.math.*

// if k < n/2 then add sectors to union in two alternative ways:
// 1) Add one sector with maximum length increase
// 2) Replace several sequential sectors with their sequential intersectors plus one
//
// if k >= n/2 then remove sectors from union in two alternative ways:
// 1) Remove one sector with minimum length decrease
// 2) Replace several sequential sectors with their sequential intersectors minus one

// Slow: 24 sec. Correct result is not guaranteed.
class TaskL3(val n: Int, val k: Int) {
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
			return TaskL3(n, k).run(sectors)
		}

		fun runTest(sectors: Array<Pair<Double, Double>>, k: Int): Double {
			val secArr = sectors.map { (a, b) -> Sector(a, b) }.toTypedArray()
			return TaskL3(sectors.size, k).run(secArr)
		}
	}

	private val pi = round(PI)

	private data class Sector(val a: Int, val b: Int, var n: Int = -1) {
		constructor(a: Double, b: Double) : this(round(a), round(b))
		val len = b - a
	}

	private class MultiSector(val n: Int) {
		var range = IntRange.EMPTY
		var a = 0
		var b = 0
		var len = 0
		var interLeft = listOf<MultiSector>()
		var interRight = listOf<MultiSector>()
		override fun toString() = range.toString()
	}

	private class FenwickTree(val size: Int) : Cloneable {
		private val tree = IntArray(size + 1)

		/**
		 * @param data The initial 0-based array of values.
		 */
		constructor(data: IntArray) : this(data.size) {
			for (i in data.indices) {
				add(i + 1, data[i])
			}
		}

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

	private abstract class LenCounter(val mSectors: List<MultiSector>,
							  protected val allSubSectors: List<Sector>,
							  protected val subSectorsCount: IntArray,
							  protected val fenwickTree: FenwickTree) : Cloneable {
		abstract fun lenRest(s: MultiSector): Int
		abstract fun nextSector(sectors: Collection<MultiSector>): MultiSector
		abstract fun plus(s: MultiSector): Int
		abstract fun minus(s: MultiSector): Int
		public override fun clone() = super.clone() as LenCounter
	}

	private class LenCounterUp(mSectors: List<MultiSector>,
					   allSubSectors: List<Sector>,
					   subSectorsCount: IntArray,
					   fenwickTree: FenwickTree
	) : LenCounter(mSectors, allSubSectors, subSectorsCount, fenwickTree) {
		constructor(mSectors: List<MultiSector>, allSubSectors: List<Sector>) :
				this(mSectors, allSubSectors, IntArray(allSubSectors.size), FenwickTree(allSubSectors.size))

		override fun lenRest(s: MultiSector) = s.len - fenwickTree.rangeSum(s.range)

		override fun nextSector(sectors: Collection<MultiSector>): MultiSector {
			return sectors.maxWith(compareBy({ lenRest(it) }, { -it.a }))
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

	private class LenCounterDown(mSectors: List<MultiSector>,
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
			return sectors.minWith(compareBy({ lenRest(it) }, { it.a }))
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

	private class Chain(var len: Int) : Cloneable {
		var remove = mutableListOf<MultiSector>()
		var add = mutableListOf<MultiSector>()
		var lens = mutableListOf<Int>()
		fun size() = remove.size
		fun prevLen() = lens[lens.lastIndex - 1]
		fun prevLen2() = prevLen() + lens[lens.lastIndex - 2]
		override fun toString() = (remove.firstOrNull()?.toString() ?: "") + "(${size()})"

		fun apply(lenCounter: LenCounter) {
			for (s in add) lenCounter.plus(s)
			for (s in remove) lenCounter.minus(s)
		}

		public override fun clone(): Chain {
			val c = Chain(len)
			c.remove.addAll(remove)
			c.add.addAll(add)
			c.lens.addAll(lens)
			return c
		}
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

	private fun convert(sectors: List<Sector>): Pair<List<MultiSector>, List<Sector>> {
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

	private fun findMaxChainRecurs(chain: Chain, usedArr: BooleanArray, lenCounter: LenCounter): Chain {
		var maxChain = chain
		var maxLen = chain.len
		var dLen = chain.len
		val s1 = chain.add.last()
		for (s in s1.interRight) {
			if (!usedArr[s.n]) continue
			dLen -= lenCounter.minus(s)
			for (s2 in s.interRight) {
				if (usedArr[s2.n]) continue
				dLen += lenCounter.plus(s2)
				// Filter chains for speed. Filter chosen experimentally and result can be wrong for some rare cases.
				//if (dLen + 2 >= chain.prevLen()) {
				if (chain.len + dLen + 3 >= chain.prevLen2()) {
					chain.remove.add(s)
					chain.add.add(s2)
					chain.len = dLen
					chain.lens.add(dLen)
					val resChain = findMaxChainRecurs(chain, usedArr, lenCounter)
					if (maxLen < resChain.len) {
						maxChain = resChain.clone()
						maxLen = resChain.len
					}
					chain.remove.removeLast()
					chain.add.removeLast()
					chain.lens.removeLast()
					chain.len = chain.lens.last()
				}
				dLen -= lenCounter.minus(s2)
			}
			dLen += lenCounter.plus(s)
		}
		return maxChain
	}

	private fun findMinChainRecurs(chain: Chain, unusedArr: BooleanArray, lenCounter: LenCounter): Chain {
		var minChain = chain
		var minLen = chain.len
		var dLen = chain.len
		val s1 = chain.add.last()
		for (s in s1.interRight) {
			if (!unusedArr[s.n]) continue
			dLen -= lenCounter.minus(s)
			for (s2 in s.interRight) {
				if (unusedArr[s2.n]) continue
				dLen += lenCounter.plus(s2)
				// Filter chains for speed. Filter chosen experimentally and result can be wrong for some rare cases.
				//if (chain.len + dLen - 3 <= chain.prevLen2()) {
				if (dLen - 2 <= chain.prevLen()) {
					chain.remove.add(s)
					chain.add.add(s2)
					chain.len = dLen
					chain.lens.add(dLen)
					val resChain = findMinChainRecurs(chain, unusedArr, lenCounter)
					if (resChain.len < minLen) {
						minChain = resChain.clone()
						minLen = resChain.len
					}
					chain.remove.removeLast()
					chain.add.removeLast()
					chain.lens.removeLast()
					chain.len = chain.lens.last()
				}
				dLen -= lenCounter.minus(s2)
			}
			dLen += lenCounter.plus(s)
		}
		return minChain
	}

	private fun findMaxChain(usedArr: BooleanArray, lenCounter: LenCounter): Chain {
		var maxChain = Chain(0)
		var dLen = 0
		for (s in lenCounter.mSectors) {
			if (!usedArr[s.n]) continue
			dLen -= lenCounter.minus(s)
			for (s1 in s.interLeft) {
				if (!usedArr[s1.n]) {
					dLen += lenCounter.plus(s1)
					for (s2 in s.interRight) {
						if (!usedArr[s2.n]) {
							dLen += lenCounter.plus(s2)
							if (dLen >= 0) {
								val chain = Chain(dLen)
								chain.lens.add(0)
								chain.lens.add(0)
								chain.lens.add(dLen)
								chain.remove.add(s)
								chain.add.add(s1)
								chain.add.add(s2)
								val resChain = findMaxChainRecurs(chain, usedArr, lenCounter)
								if (resChain.len > maxChain.len) maxChain = resChain
							}
							dLen -= lenCounter.minus(s2)
						}
					}
					dLen -= lenCounter.minus(s1)
				}
			}
			dLen += lenCounter.plus(s)
		}
		return maxChain
	}

	private fun findMinChain(unusedArr: BooleanArray, lenCounter: LenCounter, limit: Int): Chain? {
		if (limit == 0) return null
		var minChain = Chain(Int.MAX_VALUE)
		var dLen = 0
		for (s in lenCounter.mSectors) {
			if (!unusedArr[s.n]) continue
			dLen -= lenCounter.minus(s)
			for (s1 in s.interLeft) {
				if (!unusedArr[s1.n]) {
					dLen += lenCounter.plus(s1)
					for (s2 in s.interRight) {
						if (!unusedArr[s2.n]) {
							dLen += lenCounter.plus(s2)
							if (dLen >= 0) {
								val chain = Chain(dLen)
								chain.lens.add(Int.MAX_VALUE)
								chain.lens.add(Int.MAX_VALUE)
								chain.lens.add(dLen)
								chain.remove.add(s)
								chain.add.add(s1)
								chain.add.add(s2)
								val resChain = findMinChainRecurs(chain, unusedArr, lenCounter)
								if (resChain.len < minChain.len) minChain = resChain
							}
							dLen -= lenCounter.minus(s2)
						}
					}
					dLen -= lenCounter.minus(s1)
				}
			}
			dLen += lenCounter.plus(s)
		}
		return minChain
	}

	private fun calcUp(mSectors: List<MultiSector>, allSubSectors: List<Sector>, maxLimit: Int): Int {
		val n = mSectors.size
		var kLen = 0
		val unusedSectors = mSectors.toMutableSet()
		val usedArr = BooleanArray(n)
		val lenCounter = LenCounterUp(mSectors, allSubSectors)
		for (i in 1 .. k) {
			// 1 way: Add one sector with maximum length increase
			val sMax = lenCounter.nextSector(unusedSectors)
			val sMaxLen = lenCounter.lenRest(sMax)

			// 2 way: Replace several sequential sectors with their sequential intersectors plus one
			val maxChain = findMaxChain(usedArr, lenCounter)

			if (maxChain.len > sMaxLen) {
				kLen += maxChain.len
				for (s in maxChain.remove) usedArr[s.n] = false
				for (s in maxChain.add) {
					usedArr[s.n] = true
					unusedSectors.remove(s)
				}
				unusedSectors.addAll(maxChain.remove)
				maxChain.apply(lenCounter)
			} else {
				kLen += sMaxLen
				usedArr[sMax.n] = true
				unusedSectors.remove(sMax)
				lenCounter.plus(sMax)
			}
			if (kLen >= maxLimit) break
		}
		return kLen
	}

	private fun calcDown(mSectors: List<MultiSector>, allSubSectors: List<Sector>, maxLimit: Int): Int {
		val n = mSectors.size
		var kLen = maxLimit
		val unusedArr = BooleanArray(n)
		val usedSectors = mSectors.toMutableSet()
		val lenCounter = LenCounterDown(mSectors, allSubSectors)
		for (i in n - 1 downTo k) {
			// 1 way: Remove one sector with minimum length decrease
			val sMin = lenCounter.nextSector(usedSectors)
			val sMinLen = lenCounter.lenRest(sMin)

			// 2 way: Replace several sequential sectors with their sequential intersectors minus one
			val minChain = findMinChain(unusedArr, lenCounter, sMinLen)

			if (minChain != null && minChain.len < sMinLen) {
				kLen -= minChain.len
				for (s in minChain.remove) unusedArr[s.n] = false
				for (s in minChain.add) {
					unusedArr[s.n] = true
					usedSectors.remove(s)
				}
				usedSectors.addAll(minChain.remove)
				minChain.apply(lenCounter)
			} else {
				kLen -= sMinLen
				unusedArr[sMin.n] = true
				usedSectors.remove(sMin)
				lenCounter.plus(sMin)
			}
			if (kLen <= 0) break
		}
		return kLen
	}

	private fun run(sectors: Array<Sector>): Double {
		val cutSectors = cutSectors(sectors)
		val largeSectors = removeContainedSectors(cutSectors)
		val (mSectors, allSubSectors) = convert(largeSectors)

		val maxLimit = allSubSectors.sumOf { it.len }
		if (mSectors.size <= k) {
			return maxLimit.toDouble() / pi
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
			calcUp(mSectors, allSubSectors, maxLimit)
		} else {
			calcDown(mSectors, allSubSectors, maxLimit)
		}

		return kLen.toDouble() / pi
	}
}

fun main() {
	println(TaskL3.run())
}
