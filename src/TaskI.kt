import kotlin.math.min

fun main() {
	val n = readln().toInt()
	val a = readln().split(' ').map { it.toLong() }.toLongArray()
	val maxBit = min(64 - a.max().countLeadingZeroBits() + 2, 60)
	val opLimit = (n + 10).toLong()
	val b = LongArray(maxBit) { 1L shl it }
	val zapas = BooleanArray(maxBit)
	val op = Array(maxBit) { IntArray(n) }
	val sumOp = IntArray(maxBit)
	var minOp = opLimit.toInt()
	for (bit in maxBit - 1 downTo 0) {
		val t = b[bit]
		val opb = op[bit]
		var opSum = 0
		for (i in 0 until n) {
			val ai = a[i]
			if (ai and t != 0L) continue
			val plus = min(t - (ai % t), opLimit).toInt()
			if (ai < t) {
				opb[i] = plus
				opSum += plus
			} else {
				val minus = min((ai % t) + 1, opLimit).toInt()
				if (plus == minus) zapas[bit] = true
				if (plus <= minus) {
					opb[i] = plus
					opSum += plus
				} else {
					opb[i] = -minus
					opSum += minus
				}
			}
			if (opSum > minOp + 1 || opSum >= opLimit) break
		}
		if (opSum < minOp) minOp = opSum
		sumOp[bit] = opSum
	}
	if (n % 2 == 1) {
		println(minOp)
		return
	}
	// XOR may be 0
	val zapasCount = sumOp.withIndex().count { it.value == minOp && zapas[it.index] }
	if (zapasCount > 0) {
		println(minOp)
		return
	}
	val minBits = sumOp.withIndex().filter { it.value == minOp }.map { it.index }
	for (bit in minBits) {
		val opb = op[bit]
		val a2 = LongArray(n) { a[it] + opb[it] }
		if (a2.reduce { x, y -> x xor y } != 0L) {
			println(minOp)
			return
		}
	}
	val minOp1 = minOp + 1
	if (minBits.size > 1 || minBits.single() != 0) {
		println(minOp1)
		return
	}
	val minBits1 = sumOp.withIndex().filter { it.value == minOp1 }.map { it.index }
	for (bit in minBits1) {
		val opb = op[bit]
		val a2 = LongArray(n) { a[it] + opb[it] }
		if (a2.reduce { x, y -> x xor y } != 0L) {
			println(minOp1)
			return
		}
	}
	println(minOp + 2)
}
