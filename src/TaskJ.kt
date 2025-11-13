// Joyful submatrix - all columns and rows contains even number of ones.
fun main() {
	val (r, c) = readln().split(' ').map { it.toInt() }
	val a = Array(r) { readln().split(' ').map { it.toInt() }.toIntArray() }

	val deltaVer = Array(r + 1) { IntArray(c) }
	for (j in 0 until c) {
		var n = 0
		for (i in 0 .. r) {
			deltaVer[i][j] = n
			if (i < r && a[i][j] == 1) n++
		}
	}

	val deltaHor = Array(r) { IntArray(c + 1) }
	for (i in 0 until r) {
		val deltaHorI = deltaHor[i]
		val aI = a[i]
		var n = 0
		for (j in 0 .. c) {
			deltaHorI[j] = n
			if (j < c && aI[j] == 1) n++
		}
	}

	val firstOddHor = Array(c) { j -> Array(c - j) { intArrayOf(r, 1) } } // [i, h]

	fun deltaVer(i: Int, j: Int, h: Int): Int {
		return deltaVer[i + h][j] - deltaVer[i][j]
	}

	fun deltaHor(i: Int, j: Int, w: Int): Int {
		val deltaHorI = deltaHor[i]
		return deltaHorI[j + w] - deltaHorI[j]
	}

	fun evenHor(i: Int, j: Int, w: Int) = deltaHor(i, j, w) and 1 == 0

	fun evenHorSubmatrix(i: Int, j: Int, w0: Int, h: Int): Boolean {
		val arr = firstOddHor[j][w0]
		val iLast = arr[0]
		var hCurr = arr[1]
		for (t in iLast - 1 downTo i) {
			hCurr = if (evenHor(t, j, w0 + 1)) hCurr + 1 else 1
		}
		arr[0] = i
		arr[1] = hCurr
		return h < hCurr
	}

	var result = 0L
	val kj = IntArray(c + 1)
	for (i in r - 1 downTo 0) {
		// h = 1
		var k = 0
		for (j in 0 until c) {
			if (a[i][j] == 0) result += ++k else k = 0
		}
		val pairLast = IntArray(c) { j -> j + 1 }
		for (h in 2 .. r - i) {
			var lastOddVerJ = c
			k = 0
			for (j in c - 1 downTo 0) {
				val dv = deltaVer(i, j, h)
				if (dv and 1 == 0) { // even vertical
					if (dv == 0) { // all zeros vertical
						result += ++k
					} else {
						k = 0
						val jFromLast = pairLast[j]
						if (jFromLast < lastOddVerJ) {
							for (jFrom in jFromLast until lastOddVerJ) {
								if (evenHorSubmatrix(i, j, jFrom - j, h)) {
									k = kj[jFrom + 1] + 1
									pairLast[j] = jFrom
									break
								}
							}
							if (k == 0) pairLast[j] = lastOddVerJ
						}
						result += k
					}
				} else {
					lastOddVerJ = j
					k = 0
				}
				kj[j] = k
			}
		}
	}
	println(result)
}
