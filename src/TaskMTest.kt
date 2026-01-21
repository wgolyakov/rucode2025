import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

fun Int.even() = this % 2 == 0
fun Int.odd() = this % 2 != 0

// Test
private class TaskMTest1(val seed: Int) {
	private data class Point(val x: Int, val y: Int, val z: Int) {
		fun odd() = (x + y + z).odd()
	}

	private fun distance(p1: Point, p2: Point) = abs(p2.x - p1.x) + abs(p2.y - p1.y) + abs(p2.z - p1.z)

	fun fullTimes(modes: IntArray, maxDistance: Int): IntArray {
		val sortedModes = modes.sorted()
		val maxMode = modes.max()
		val overDistance = maxDistance + maxMode
		val times = IntArray(overDistance + 1) { Int.MAX_VALUE / 2 }
		times[0] = 0
		for (v in sortedModes) {
			for (d in 0..overDistance - v) {
				if (times[d] + 1 < times[d + v]) times[d + v] = times[d] + 1
			}
		}
		// One step back
		for (v in sortedModes) {
			for (d in overDistance downTo v) {
				if (times[d] + 1 < times[d - v]) times[d - v] = times[d] + 1
			}
		}
		return times
	}

	fun shortTimes(modes: IntArray): IntArray {
		val maxMode = modes.max()
		val maxOddMode = modes.filter { it.odd() }.maxOrNull() ?: -1
		val times = IntArray(maxMode * 2 + 1) { Int.MAX_VALUE / 2 }
		times[0] = 0
		for (d in 2 until maxMode step 2) if (times[d] > 2) times[d] = 2
		if (maxOddMode != -1) {
			for (d in 1 until maxOddMode step 2) if (times[d] > 3) times[d] = 3
		}
		val sortedModes = modes.sorted()
		for (i in sortedModes.indices) {
			val m1 = sortedModes[i]
			for (j in i .. sortedModes.lastIndex) {
				val m2 = sortedModes[j]
				for (d in m2 - m1 .. m1 + m2 step 2) if (times[d] > 2) times[d] = 2
			}
		}
		return times
	}

	fun time(distance: Int, fullTimes: IntArray, shortTimes: IntArray): Int {
		if (distance == 0) return 0
		var tMin = Int.MAX_VALUE / 2
		for (i in shortTimes.indices) {
			if (distance - i < 0) break
			tMin = min(tMin, fullTimes[distance - i] + shortTimes[i])
		}
		for (i in shortTimes.indices) {
			if (distance + i >= fullTimes.size) break
			tMin = min(tMin, fullTimes[distance + i] + shortTimes[i])
		}
		return tMin
	}

	private fun totalTime(mask: Int, curr: Int, adj: Array<IntArray>, dp: Array<IntArray>): Int {
		val n = adj.size
		// if all points are visited
		if (mask == (1 shl n) - 1) return 0
		// If the value has already been computed, return it from the dp table
		if (dp[curr][mask] != -1) return dp[curr][mask]
		var tMin = Int.MAX_VALUE
		// Try visiting every point that has not been visited yet
		for (i in 0 until n) {
			// If point i is not visited
			if ((mask and (1 shl i)) == 0) {
				// Visit point i and update the mask
				tMin = min(tMin, adj[curr][i] + totalTime(mask or (1 shl i), i, adj, dp))
			}
		}
		dp[curr][mask] = tMin
		return tMin
	}

	private fun tsp(adj: Array<IntArray>): Int {
		val n = adj.size
		val dp = Array(n) { IntArray(1 shl n) { -1 } }
		// Start from point 0, with only point 0 visited initially (mask = 1)
		return totalTime(1, 0, adj, dp)
	}

	fun run(): Int {
		//val (n, m) = readln().split(' ').map { it.toInt() } // 1..15
		//val modes = readln().split(' ').map { it.toInt() }.toIntArray() // 1..300_000
		//val asteroids = Array(m) {
		//	val (xi, yi, zi) = readln().split(' ').map { it.toInt() } // -100_000..100_000
		//	Point(xi, yi, zi)
		//}

		val random = Random(seed)
		val n = random.nextInt(1, 5)
		val m = random.nextInt(1, 5)
		val modes = IntArray(n) { random.nextInt(1, 300 + 1) }
		val asteroids = Array(m) {
			Point(
				random.nextInt(-100, 100 + 1),
				random.nextInt(-100, 100 + 1),
				random.nextInt(-100, 100 + 1)
			)
		}

		if (asteroids.any { it.odd() } && modes.all { it.even() }) {
			//println(-1)
			return -1
		}
		val p0 = Point(0, 0, 0)
		val points = arrayOf(p0) + (asteroids.toList() - p0)
		val distances = Array(points.size) { IntArray(points.size) }
		var maxDistance = 0
		for (i in points.indices) {
			for (j in i..points.lastIndex) {
				val distance = distance(points[i], points[j])
				distances[i][j] = distance
				distances[j][i] = distance
				if (distance > maxDistance) maxDistance = distance
			}
		}
		val fullTimes = fullTimes(modes, maxDistance)
		val shortTimes = shortTimes(modes)
		val adj = Array(points.size) { IntArray(points.size) }
		for (i in points.indices) {
			for (j in i..points.lastIndex) {
				val t = time(distances[i][j], fullTimes, shortTimes)
				adj[i][j] = t
				adj[j][i] = t
			}
		}
		val t = tsp(adj)
		//println(t)
		return t
	}
}

private class TaskMTest2(val seed: Int) {
	private data class Point(val x: Int, val y: Int, val z: Int) {
		fun odd() = (x + y + z).odd()
	}

	private fun distance(p1: Point, p2: Point) = abs(p2.x - p1.x) + abs(p2.y - p1.y) + abs(p2.z - p1.z)

	fun calcAllTimes(modes: IntArray, maxDistance: Int): IntArray {
		val sortedModes = modes.sorted()
		val maxMode = modes.max()
		val maxOddMode = modes.filter { it.odd() }.maxOrNull() ?: -1
		val overDistance = maxDistance + maxMode
		val times = IntArray(overDistance + 1) { Int.MAX_VALUE / 2 }
		val queue = mutableListOf(0)
		times[0] = 0
		while (queue.isNotEmpty()) {
			val distance = queue.removeFirst()
			val t = times[distance]
			for (v in sortedModes) {
				val d1 = distance - v
				if (d1 >= 0 && t + 1 < times[d1]) {
					times[d1] = t + 1
					queue.add(d1)
				}
				val d2 = distance + v
				if (d2 <= overDistance && t + 1 < times[d2]) {
					times[d2] = t + 1
					queue.add(d2)
				}
			}
			for ((i, m1) in sortedModes.withIndex()) {
				for (m2 in sortedModes.subList(i, sortedModes.size)) {
					for (v in m2 - m1 .. m1 + m2 step 2) {
						val d1 = distance - v
						if (d1 >= 0 && t + 2 < times[d1]) {
							times[d1] = t + 2
							queue.add(d1)
						}
						val d2 = distance + v
						if (d2 <= overDistance && t + 2 < times[d2]) {
							times[d2] = t + 2
							queue.add(d2)
						}
					}
				}
			}
			for (v in 2 until maxMode step 2) {
				val d1 = distance - v
				if (d1 >= 0 && t + 2 < times[d1]) {
					times[d1] = t + 2
					queue.add(d1)
				}
				val d2 = distance + v
				if (d2 <= overDistance && t + 2 < times[d2]) {
					times[d2] = t + 2
					queue.add(d2)
				}
			}
			if (maxOddMode != -1) {
				for (v in 1 until maxOddMode step 2) {
					val d1 = distance - v
					if (d1 >= 0 && t + 3 < times[d1]) {
						times[d1] = t + 3
						queue.add(d1)
					}
					val d2 = distance + v
					if (d2 <= overDistance && t + 3 < times[d2]) {
						times[d2] = t + 3
						queue.add(d2)
					}
				}
			}
		}
		return times
	}

	fun timeRecurs2(asteroid: Int, adj: Array<IntArray>, asteroids: MutableSet<Int>, tCurr: Int, tMax: Int, path: MutableSet<Int>): Int {
		if (asteroids.size <= 1) return tCurr
		if (tCurr >= tMax) return tMax
		path.add(asteroid)
		asteroids.remove(asteroid)
		var tMin = tMax
		val times = adj[asteroid]
		val ways = asteroids.map { it to times[it] }.sortedBy { it.second }
		for ((nextAsteroid, time) in ways) {
			val t = timeRecurs2(nextAsteroid, adj, asteroids, tCurr + time, tMin, path)
			if (t < tMin)  {
				tMin = t
			}
		}
		path.remove(asteroid)
		asteroids.add(asteroid)
		return tMin
	}

	fun run(): Int {
		//val (n, m) = readln().split(' ').map { it.toInt() } // 1..15
		//val modes = readln().split(' ').map { it.toInt() }.toIntArray() // 1..300_000
		//val asteroids = Array(m) {
		//	val (xi, yi, zi) = readln().split(' ').map { it.toInt() } // -100_000..100_000
		//	Point(xi, yi, zi)
		//}

		val random = Random(seed)
		val n = random.nextInt(1, 5)
		val m = random.nextInt(1, 5)
		val modes = IntArray(n) { random.nextInt(1, 300 + 1) }
		val asteroids = Array(m) {
			Point(
				random.nextInt(-100, 100 + 1),
				random.nextInt(-100, 100 + 1),
				random.nextInt(-100, 100 + 1)
			)
		}

		if (asteroids.any { it.odd() } && modes.all { it.even() }) {
			//println(-1)
			return -1
		}

		val p0 = Point(0, 0, 0)
		val points = arrayOf(p0) + (asteroids.toList() - p0)
		val distances = Array(points.size) { IntArray(points.size) }
		var maxDistance = 0
		for (i in points.indices) {
			for (j in i .. points.lastIndex) {
				val distance = distance(points[i], points[j])
				distances[i][j] = distance
				distances[j][i] = distance
				if (distance > maxDistance) maxDistance = distance
			}
		}
		val allTimes = calcAllTimes(modes, maxDistance)
		val adj = Array(points.size) { IntArray(points.size) }
		for (i in points.indices) {
			for (j in i..points.lastIndex) {
				val t = allTimes[distances[i][j]]
				adj[i][j] = t
				adj[j][i] = t
			}
		}
		if (adj.size > 2) {
			for (i in adj.indices) {
				for (j in i + 1..adj.lastIndex) {
					var tMax = 0
					for (a in adj.indices) {
						if (a == i || a == j) continue
						val t1 = adj[i][a]
						val t2 = adj[a][j]
						if (tMax < t1 + t2) tMax = t1 + t2
					}
					if (adj[i][j] > tMax) {
						println("Direct way is longer than the detour: ${adj[i][j]} > $tMax")
					}
				}
			}
		}
		//val t = tsp(adj)
		val t = timeRecurs2(0, adj, adj.indices.toMutableSet(), 0, Int.MAX_VALUE, mutableSetOf())
		//println(t)
		return t
	}
}

fun main() {
	for (i in 1..10_000) {
		val seed = Random.nextInt()
		val t1 = TaskMTest1(seed).run()
		val t2 = TaskMTest2(seed).run()
		if (t1 != t2) {
			println("Wrong result $i, seed: $seed")
			println(t1)
			println(t2)
		}
	}
}
