import kotlin.math.abs
import kotlin.math.min

// Mode 1: v = 1/1sec
// Mode 2: v = 2/1sec
// Mode 3: v = 3/1sec, 2/2sec, 1/3sec (3-2)
// Mode 4: v = 4/1sec, 2/2sec
// Mode 5: v = 5/1sec, 2/2sec, 4/2sec, 3/3sec (5-2), 1/3sec (5-4)
// Mode 6: v = 6/1sec, 2/2sec, 4/2sec
// ...
// Mode n odd: v = n/1sec, 2/2sec, 4/2sec, ..., (n-1)/2sec, 1/3sec, 3/3sec, ..., (n-2)/3sec
// Mode n even: v = n/1sec, 2/2sec, 4/2sec, ..., (n-2)/2sec
fun main() {
	fun Int.even() = this % 2 == 0

	data class Point(val x: Int, val y: Int, val z: Int) {
		fun even() = (x + y + z).even()
	}

	fun distance(p1: Point, p2: Point) = abs(p2.x - p1.x) + abs(p2.y - p1.y) + abs(p2.z - p1.z)

	fun countTimeRecurs(srModes: List<Int>, distance: Int, t0: Int = 0, tMax: Int = Int.MAX_VALUE): Int {
		var s = distance
		var count = t0
		var tMin = tMax
		for ((i, d) in srModes.withIndex()) {
			if (count >= tMin) break
			if (d <= s) {
				val c = s / d
				count += c
				s -= d * c
				if (s == 0) break
				if (count >= tMin) break
			}
			if ((d - s).even()) {
				if (count + 3 < tMin) tMin = count + 3
			} else if (!d.even()) {
				if (count + 4 < tMin) tMin = count + 4
			}
			val t = countTimeRecurs(srModes.subList(0, i) + srModes.subList(i + 1, srModes.size), d - s, count + 1, tMin)
			if (t < tMin) tMin = t
		}
		return if (s == 0) min(count, tMin) else tMin
	}

	fun time(p1: Point, p2: Point, modes: IntArray): Int {
		val srModes = modes.sorted().reversed()
		val distance = distance(p1, p2)
		if (distance in modes) return 1
		for (m1 in modes) {
			for (m2 in modes) {
				if (m1 + m2 == distance || m1 - m2 == distance) return 2
			}
		}
		if (modes.any { it > distance && (it - distance).even() }) return 3
		return countTimeRecurs(srModes, distance)
	}

	val (n, m) = readln().split(' ').map { it.toInt() } // 1..15
	val modes = readln().split(' ').map { it.toInt() }.toIntArray() // 1..300_000
	val asteroids = Array(m) {
		val (xi, yi, zi) = readln().split(' ').map { it.toInt() } // -100_000..100_000
		Point(xi, yi, zi)
	}

	if (asteroids.any { !it.even() } && modes.all { it.even() }) {
		println(-1)
		return
	}
	val p0 = Point(0, 0, 0)
	val points = if (p0 in asteroids) asteroids else arrayOf(p0) + asteroids
	val iStart = points.indexOf(p0)
	val adj = Array(points.size) { IntArray(points.size) }
	for (i in points.indices) {
		for (j in i .. points.lastIndex) {
			val t = time(points[i], points[j], modes)
			adj[i][j] = t
			adj[j][i] = t
		}
	}
	val indexes = points.indices.toMutableSet()
	indexes.remove(iStart)
	val path = mutableListOf(iStart)
	var i = iStart
	var t = 0
	while (indexes.isNotEmpty()) {
		val (jMin, tMin) = adj[i].withIndex().filter { it.index in indexes }.minBy { it.value }
		t += tMin
		path.add(jMin)
		indexes.remove(jMin)
		i = jMin
	}
	println(t)
}
