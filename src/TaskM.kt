import kotlin.math.abs
import kotlin.math.min

// Mode 1: v = 1/1sec
// Mode 2: v = 2/1sec
// Mode 3: v = 3/1sec; 2/2sec; 1/3sec (3-2)
// Mode 4: v = 4/1sec; 2/2sec
// Mode 5: v = 5/1sec; 2/2sec, 4/2sec; 3/3sec (5-2), 1/3sec (5-4)
// Mode 6: v = 6/1sec; 2/2sec, 4/2sec
// ...
// Mode s odd:  v = s/1sec; 2/2sec, 4/2sec, ..., (s-1)/2sec; 1/3sec, 3/3sec, ..., (s-2)/3sec
// Mode s even: v = s/1sec; 2/2sec, 4/2sec, ..., (s-2)/2sec
//
// Pair of modes: m1, m2:
// s1 = |m1 - m2|
// s2 = m1 + m2
// v = s1/2sec, (s1+2)/2sec, (s1+4)/2sec, ..., (s2-2)/2sec, s2/2sec
fun main() {
	fun Int.even() = this % 2 == 0
	fun Int.odd() = this % 2 != 0

	data class Point(val x: Int, val y: Int, val z: Int) {
		fun odd() = (x + y + z).odd()
	}

	fun distance(p1: Point, p2: Point) = abs(p2.x - p1.x) + abs(p2.y - p1.y) + abs(p2.z - p1.z)

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

	fun totalTime(mask: Int, curr: Int, adj: Array<IntArray>, dp: Array<IntArray>): Int {
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

	fun tsp(adj: Array<IntArray>): Int {
		val n = adj.size
		val dp = Array(n) { IntArray(1 shl n) { -1 } }
		// Start from point 0, with only point 0 visited initially (mask = 1)
		return totalTime(1, 0, adj, dp)
	}

	val (n, m) = readln().split(' ').map { it.toInt() } // 1..15
	val modes = readln().split(' ').map { it.toInt() }.toIntArray() // 1..300_000
	val asteroids = Array(m) {
		val (xi, yi, zi) = readln().split(' ').map { it.toInt() } // -100_000..100_000
		Point(xi, yi, zi)
	}

	if (asteroids.any { it.odd() } && modes.all { it.even() }) {
		println(-1)
		return
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
	val fullTimes = fullTimes(modes, maxDistance)
	val shortTimes = shortTimes(modes)
	val adj = Array(points.size) { IntArray(points.size) }
	for (i in points.indices) {
		for (j in i .. points.lastIndex) {
			val t = time(distances[i][j], fullTimes, shortTimes)
			adj[i][j] = t
			adj[j][i] = t
		}
	}
	val t = tsp(adj)
	println(t)
}
