import kotlin.math.*
import kotlin.random.Random

// Test 0 - 1.0
fun test0(): Array<Pair<Double, Double>> {
	//val n = 1
	//val k = 1
	return arrayOf(0.0 to PI)
}

// Test 1 - 1.0
fun test1(): Array<Pair<Double, Double>> {
	//val n = 5
	//val k = 3
	val sectors = arrayOf(
		0.1 to 1.5,
		1.5 to 3.0,
		0.0 to PI / 3,
		PI / 3 to PI * 2 / 3,
		PI * 2 / 3 to PI,
	)
	return sectors
}

// Test 2 - 1.0
fun test2(): Array<Pair<Double, Double>> {
	//val n = 5
	//val k = 3
	val sectors = arrayOf(
		0.1 to 1.6,
		1.4 to 3.0,
		0.0 to PI / 3 + 0.1,
		PI / 3 - 0.1 to PI * 2 / 3 + 0.1,
		PI * 2 / 3 - 0.1 to PI,
	)
	return sectors
}

// 0.595238109637828
fun test3(): Array<Pair<Double, Double>> {
	val n = 8
	//val k = 2
	val n1 = n / 2
	val n2 = n1 - 1
	val p1 = 0.1
	val p2 = 0.12
	val p3 = 0.3
	val dx = PI / (n1 - p1 * (n1 - 1))
	val dy = PI / (n2 - p2 * (n2 - 1) + p3 * 2)
	val sectors = Array(n) { i ->
		if (i < n1) {
			dx * (1.0 - p1) * i to dx * (1.0 - p1) * i + dx
		} else if (i < n1 + n2) {
			dy * p3 + dy * (1.0 - p2) * (i - n1) to dy * p3 + dy * (1.0 - p2) * (i - n1) + dy
		} else {
			0.0 to dy * 0.1
		}
	}
	return sectors
}

// 1.0
fun test4(): Array<Pair<Double, Double>> {
	val n = 1500
	//val k = n / 2
	val n1 = n / 2
	val n2 = n1 - 1
	val p1 = 0.1
	val p2 = 0.12
	val p3 = 0.3
	val dx = PI / (n1 - p1 * (n1 - 1))
	val dy = PI / (n2 - p2 * (n2 - 1) + p3 * 2)
	val sectors = Array(n) { i ->
		if (i < n1) {
			dx * (1.0 - p1) * i to dx * (1.0 - p1) * i + dx
		} else if (i < n1 + n2) {
			dy * p3 + dy * (1.0 - p2) * (i - n1) to dy * p3 + dy * (1.0 - p2) * (i - n1) + dy
		} else {
			0.0 to dy * 0.1
		}
	}
	return sectors
}

// 1.0
fun test5(): Array<Pair<Double, Double>> {
	val n = 10
	//val k = n / 2
	val n1 = n / 2
	val n2 = n1 - 1
	val p2 = 0.4
	val dx = PI / n1
	val dy = (PI - dx * p2 * 2) / n2
	val sectors = Array(n) { i ->
		if (i < n1) {
			dx * i to dx * i + dx
		} else if (i < n1 + n2) {
			dx * p2 + dy * (i - n1) to dx * p2 + dy * (i - n1) + dy
		} else {
			0.0 to dx * 0.1
		}
	}
	return sectors
}

// 1.0
fun test6(): Array<Pair<Double, Double>> {
	val n = 1500
	//val k = n / 2
	val n1 = n / 2
	val n2 = n1 - 1
	val p2 = 0.4 //0.5
	val dx = PI / n1
	val dy = (PI - dx * p2 * 2) / n2
	val sectors = Array(n) { i ->
		if (i < n1) {
			dx * i to dx * i + dx
		} else if (i < n1 + n2) {
			dx * p2 + dy * (i - n1) to dx * p2 + dy * (i - n1) + dy
		} else {
			0.0 to dx * 0.1
		}
	}
	return sectors
}

// 1.0
fun test7(): Array<Pair<Double, Double>> {
	val n = 1500
	//val k = 1499
	val dx = PI * 0.5 / (n - 1)
	val sectors = Array(n) { i -> dx * i to dx * i + PI * 0.5 }
	return sectors
}

// 0.9994074512999036
fun test8(): Array<Pair<Double, Double>> {
	val n = 1500
	//val k = 1499
	val p1 = 0.1
	val dx = PI / (n - p1 * (n - 1))
	val sectors = Array(n) { i -> dx * (1.0 - p1) * i to dx * (1.0 - p1) * i + dx }
	//println("Result: " + (PI - dx * (1.0 - p1 * 2)) / PI)
	return sectors
}

// 1.0
fun test9(): Array<Pair<Double, Double>> {
	val n = 1500
	val n1 = 1499
	//val k = 1499
	val p1 = 0.1
	val dx = PI / (n1 - p1 * (n1 - 1))
	val sectors = Array(n) { i ->
		if (i < 1499) {
			dx * (1.0 - p1) * i to dx * (1.0 - p1) * i + dx
		} else {
			PI - dx * 1.6 to PI - dx * 0.4
		}
	}
	return sectors
}

// 1.0
fun test10(): Array<Pair<Double, Double>> {
	val n = 1500
	//val k = 1499
	val n1 = (n / 1.5).roundToInt()
	val p = 0.7 / (n - n1 + 2)
	val dx = PI * p * 0.5 / (n1 - 1)
	val dy = PI * (1.0 - p) / (n - n1)
	val sectors = Array(n) { i ->
		if (i < n1) {
			dx * i to dx * i + PI * p * 0.5
		} else {
			PI * p + dy * (i - n1) to PI * p + dy * (i - n1) + dy
		}
	}
	return sectors
}

// 1.0
fun test11(): Array<Pair<Double, Double>> {
	val n = 1500
	//val k = 1499
	val n2 = (n / 1.5).roundToInt()
	val p = 0.7 / (n - n2 + 2)
	val dx = PI * p * 0.5 / (n2 - 1)
	val dy = PI * (1.0 - p) / (n - n2)
	val sectors = Array(n) { i ->
		if (i < n - n2) {
			dy * i to dy * i + dy
		} else {
			PI * (1.0 - p) + dx * (i - n + n2) to PI * (1.0 - p) + dx * (i - n + n2) + PI * p * 0.5
		}
	}
	return sectors
}

// 1.0
fun test12(): Array<Pair<Double, Double>> {
	val n = 1500
	//val k = 1499
	val dx = PI * 0.5 / n
	val sectors = Array(n) { i -> dx * i to PI - dx * i }
	return sectors
}

// 1.0
fun test13(): Array<Pair<Double, Double>> {
	val n = 1500
	//val k = 1499
	val n1 = 10
	val dx = PI * 0.5 / n
	val dg = PI / n1
	val sectors = Array(n) { i ->
		val g = i * n1 / n
		val i0 = g * n / n1
		dg * g + dx * (i - i0) to dg * (g + 1) - dx * (i - i0)
	}
	return sectors
}

// 0.5136986408199892
fun test14(): Array<Pair<Double, Double>> {
	val n = 14
	//val k = 3
	val n1 = n / 2
	val n2 = n1 - 1
	val p1 = 0.1
	val p2 = 0.12
	val p3 = 0.22
	val dx = PI / (n1 - p1 * (n1 - 1))
	val dy = PI / (n2 - p2 * (n2 - 1) + p3 * 2)
	val sectors = Array(n) { i ->
		if (i < n1) {
			dx * (1.0 - p1) * i to dx * (1.0 - p1) * i + dx
		} else if (i < n1 + n2) {
			dy * p3 + dy * (1.0 - p2) * (i - n1) to dy * p3 + dy * (1.0 - p2) * (i - n1) + dy
		} else {
			0.0 to dy * 0.1
		}
	}
	return sectors
}

// 0.5859375087037858
fun test15(): Array<Pair<Double, Double>> {
	val n = 12
	//val k = 3
	val n1 = n / 2
	val n2 = n1 - 1
	val p1 = 0.1
	val p2 = 0.12
	val p3 = 0.3
	val dx = PI / (n1 - p1 * (n1 - 1))
	val dy = PI / (n2 - p2 * (n2 - 1) + p3 * 2)
	val sectors = Array(n) { i ->
		if (i < n1) {
			dx * (1.0 - p1) * i to dx * (1.0 - p1) * i + dx
		} else if (i < n1 + n2) {
			dy * p3 + dy * (1.0 - p2) * (i - n1) to dy * p3 + dy * (1.0 - p2) * (i - n1) + dy
		} else {
			0.0 to dy * 0.1
		}
	}
	return sectors
}

// 0.9246119969657429
fun test16(): Array<Pair<Double, Double>> {
	val n = 12
	//val k = 5
	val n1 = n / 2
	val n2 = n1 - 1
	val p1 = 0.1
	val p2 = 0.12
	val p3 = 0.2
	val dx = PI / (n1 - p1 * (n1 - 1))
	val dy = PI / (n2 - p2 * (n2 - 1) + p3 * 2)
	val sectors = Array(n) { i ->
		if (i < n1) {
			dx * (1.0 - p1) * i to dx * (1.0 - p1) * i + dx
		} else if (i < n1 + n2) {
			dy * p3 + dy * (1.0 - p2) * (i - n1) to dy * p3 + dy * (1.0 - p2) * (i - n1) + dy
		} else {
			0.0 to dy * 0.1
		}
	}
	return sectors
}

// 0.7908484952687931
fun test17(): Array<Pair<Double, Double>> {
	val n = 20
	//val k = 7
	val n1 = n / 2
	val n2 = n1 - 1
	val p1 = 0.1
	val p2 = 0.12
	val p3 = 0.25
	val dx = PI / (n1 - p1 * (n1 - 1))
	val dy = PI / (n2 - p2 * (n2 - 1) + p3 * 2)
	val sectors = Array(n) { i ->
		if (i < n1) {
			dx * (1.0 - p1) * i to dx * (1.0 - p1) * i + dx
		} else if (i < n1 + n2) {
			dy * p3 + dy * (1.0 - p2) * (i - n1) to dy * p3 + dy * (1.0 - p2) * (i - n1) + dy
		} else {
			0.0 to dy * 0.1
		}
	}
	return sectors
}

fun randomSectors(seed: Int, n: Int, rMax: Int = 0): Array<Pair<Double, Double>> {
	val random = Random(seed)
	val sectors = Array(n) {
		val x = random.nextInt(-1000, 1000 + 1)
		var y: Int
		var c: Double
		do {
			y = random.nextInt(-1000, 1000 + 1)
			c = sqrt((x * x + y * y).toDouble())
		} while (c <= 1.0)
		var l = c.toInt()
		if (l.toDouble() < c) l++
		val r = random.nextInt(1, if (rMax > 0) min(l, rMax) else l)
		val betta = asin(r / c)
		val alfa = if (y >= 0) {
			acos(x / c)
		} else {
			if (x >= 0) -acos(x / c) else -acos(x / c) + PI * 2
		}
		alfa - betta to alfa + betta
	}
	return sectors
}

fun runTest(name: String, sectors: Array<Pair<Double, Double>>, n: Int, k: Int, res: Double, slow: Boolean,
			task: (sectors: Array<Pair<Double, Double>>, k: Int) -> Double, seed: Int? = null) {
	if (sectors.size != n) println("$name: Wrong n: ${sectors.size}")
	val time1 = System.currentTimeMillis()
	val res1: Double
	try {
		res1 = task(sectors, k)
	} catch (e :RuntimeException) {
		println("$name: Error")
		if (seed != null) println("seed: $seed")
		e.printStackTrace()
		return
	}
	val dt = (System.currentTimeMillis() - time1) / 1000.0
	if (slow && dt > 2) println("$name: Slow - $dt sec")
	if (res != -1.0 && abs(res1 - res) > 0.000001) {
		println("$name: Wrong result!")
		println("$res1")
		println("$res - correct result")
		if (seed != null) println("seed: $seed")
		return
	}
	if (!slow) {
		val res2 = TaskL2.runTest(sectors, k)
		if (abs(res1 - res2) > 0.000001) {
			println("$name: Wrong result!")
			println("$res1")
			println("$res2 - TaskL2")
			if (seed != null) println("seed: $seed")
			return
		}
	}
	if (res == -1.0 && slow) {
		println("$name: $res1")
	} else if (seed == null) {
		println("$name: OK")
	}
}

fun main() {
	val task = TaskL::runTest
	//val task = TaskL2::runTest
	//val task = TaskL3::runTest

	runTest("Test 0", test0(), 1, 1, 1.0, false, task)
	runTest("Test 1", test1(), 5, 3, 1.0, false, task)
	runTest("Test 1_1", test1(), 5, 1, 0.47746482982126914, false, task)
	runTest("Test 1_5", test1(), 5, 5, 1.0, false, task)
	runTest("Test 2", test2(), 5, 3, 1.0, false, task)
	runTest("Test 3", test3(), 8, 2, 0.595238109637828, false, task)
	runTest("Test 4", test4(), 1500, 750, 1.0, true, task)
	runTest("Test 4_479", test4(), 1500, 479, 0.7188083311819564, true, task)
	runTest("Test 4_493", test4(), 1500, 493, 0.7390663331224689, true, task)
	runTest("Test 4_494", test4(), 1500, 494, 0.7405133253033298, true, task)
	runTest("Test 4_495", test4(), 1500, 495, 0.7419603174841907, true, task)
	runTest("Test 4_498", test4(), 1500, 498, 0.7463012812943779, true, task)
	runTest("Test 4_501", test4(), 1500, 501, 0.7503470508819786, true, task)
	runTest("Test 4_502", test4(), 1500, 502, 0.7513512580951576, true, task)
	runTest("Test 4_503", test4(), 1500, 503, 0.7523554621252376, true, task)
	runTest("Test 4_504", test4(), 1500, 504, 0.7533596661553178, true, task)
	runTest("Test 4_505", test4(), 1500, 505, 0.7543638701853979, true, task)
	runTest("Test 4_514", test4(), 1500, 514, 0.7634016682589323, true, task)
	runTest("Test 4_518", test4(), 1500, 518, 0.7674184239003742, true, task)
	runTest("Test 4_544", test4(), 1500, 544, 0.7935274676683497, true, task)
	runTest("Test 5", test5(), 10, 5, 1.0, false, task)
	runTest("Test 6", test6(), 1500, 750, 1.0, true, task)
	runTest("Test 7", test7(), 1500, 1499, 1.0, true, task)
	runTest("Test 8", test8(), 1500, 1499, 0.9994074512999036, true, task)
	runTest("Test 8_700", test8(), 1500, 700, 0.5184801727875191, true, task)
	runTest("Test 8_750", test8(), 1500, 750, 0.5555144108196204, true, task)
	runTest("Test 9", test9(), 1500, 1499, 1.0, true, task)
	runTest("Test 10", test10(), 1500, 1499, 1.0, true, task)
	runTest("Test 11", test11(), 1500, 1499, 1.0, true, task)
	runTest("Test 12", test12(), 1500, 1499, 1.0, true, task)
	runTest("Test 13", test13(), 1500, 1499, 1.0, true, task)
	runTest("Test 14", test14(), 14, 3, 0.5136986408199892, false, task)
	runTest("Test 15", test15(), 12, 3, 0.5859375087037858, false, task)
	runTest("Test 16", test16(), 12, 5, 0.9246119969657429, false, task)
	runTest("Test 17", test17(), 20, 7, 0.7908484952687931, true, task)
	runTest("Test 18", randomSectors(-238345056, 8), 8, 3, -1.0, false, task)
	runTest("Test 19", randomSectors(1198475948, 8), 8, 3, -1.0, false, task)
	runTest("Test 20", randomSectors(-1530267609, 7), 7, 3, -1.0, false, task)
	runTest("Test 21", randomSectors(338352002, 9), 9, 4, -1.0, false, task)
	runTest("Test 22", randomSectors(732010784, 7), 7, 3, -1.0, false, task)
	runTest("Test 23", randomSectors(-1137542863, 7), 7, 3, -1.0, false, task)
	runTest("Test 24", randomSectors(137594073, 8), 8, 3, -1.0, false, task)
	runTest("Test 25", randomSectors(1672437547, 8), 8, 3, -1.0, false, task)
	runTest("Test 26", randomSectors(-1178594227, 7), 7, 3, -1.0, false, task)
	runTest("Test 27", randomSectors(888256507, 1500, 10), 1500, 100, 0.9748980788309405, true, task)
	runTest("Test 28", randomSectors(888256507, 1500, 3), 1500, 420, 0.6868178304970445, true, task)
	runTest("Test 29", randomSectors(-1468444328, 8), 8, 3, 0.6650863111567582, false, task)

	val n = 8
	val k = 3
	for (i in 1..100_000) {
		val seed = Random.nextInt()
		runTest("Random test $i", randomSectors(seed, n), n, k, -1.0, false, task, seed)
	}
}
