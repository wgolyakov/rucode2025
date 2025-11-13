import kotlin.random.Random

// Joyful submatrix - all columns and rows contains even number of ones.
fun main() {
	fun joyful(a: Array<IntArray>, i: Int, j: Int, w: Int, h: Int): Boolean {
		if (w < 2 || h < 2) {
			for (y in i until i + h) {
				for (x in j until j + w) {
					if (a[y][x] == 1) return false
				}
			}
			return true
		} else {
			for (y in i until i + h) {
				var hor = 0
				for (x in j until j + w) {
					if (a[y][x] == 1) hor++
				}
				if (hor % 2 == 1) return false
			}
			for (x in j until j + w) {
				var ver = 0
				for (y in i until i + h) {
					if (a[y][x] == 1) ver++
				}
				if (ver % 2 == 1) return false
			}
			return true
		}
	}

	val a = arrayOf(
		intArrayOf(0, 1, 1),
		intArrayOf(0, 1, 1),
	)
	val r = a.size
	val c = a.first().size

	//val r = 500
	//val c = 500

	//val a = Array(r) { IntArray(c) { Random.nextInt(2) } }
	//val a = Array(r) { IntArray(c) { 1 } }
	//val a = Array(r) { IntArray(c) { if (it % 35 == 0) 1 else 0 } }

	//val a = Array(r) { i -> IntArray(c) { j -> if (i == j || i == j - 1) 1 else 0 } }
	//for (i in 0 until r) a[i][50] = 1
	//for (i in 0 until r) a[i][333] = 1

	//val a = Array(r) { i -> IntArray(c) { j -> if ((i + j) % 2 == 0) 1 else 0 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if ((i / 2 + j / 2) % 2 == 0) 1 else 0 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if (i == j || i - 1 == j) 1 else 0 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if (i == j || i - 1 == j || c - j - 1 == i || c - j == i) 1 else 0 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if (i == j) 0 else 1 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if (i == j || i == j - 1) 0 else 1 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if (i == j || i == c - j - 1) 0 else 1 } }

	//val d = 313
	//val a = Array(r) { i -> IntArray(c) { j -> if (i % d == j % d || (i - 1) % d == j % d) 1 else 0 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if (i % d == j % d || i % d - 1 == j % d) 1 else 0 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if ((c - j - 1) % d == i % d || (c - j) % d == i % d) 1 else 0 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if (i % d == j % d || i % d - 1 == j % d || (c - j - 1) % d == i % d || (c - j) % d == i % d) 1 else 0 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if (i % d == j % d) 0 else 1 } }
	//val a = Array(r) { i -> IntArray(c) { j -> if (j % 2 == 0 && i < j || j % 2 == 1 && i <= j) 1 else 0 } }

	//val a = Array(r) { IntArray(c) }
	//for (j in 0 until c) {
	//	if ( j < c / 2) {
	//		for (i in j until r - j) a[i][j] = 1
	//	} else {
	//		for (i in c - j - 1 until r - (c - j) + 1) a[i][j] = 1
	//	}
	//}

	//println(a.joinToString("\n") { it.toList().toString() })

	var count = 0
	for (i in 0 until r) {
		for (j in 0 until c) {
			for (w in 1 .. c - j) {
				for (h in 1 .. r - i) {
					if (joyful(a, i, j, w, h)) count++
				}
			}
		}
	}
	println(count)
}
