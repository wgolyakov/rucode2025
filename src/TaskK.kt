fun main() {
	data class Point(val x: Int, val y: Int)

	fun findEmptyPoint(grid: Array<IntArray>): Point? {
		for ((i, row) in grid.withIndex()) {
			for ((j, c) in row.withIndex()) {
				if (c == 0) return Point(j, i)
			}
		}
		return null
	}

	fun neighbors(grid: Array<IntArray>, n: Int, m: Int, p: Point): List<Point> {
		return listOf(
			Point(p.x - 1, p.y),
			Point(p.x, p.y + 1),
			Point(p.x + 1, p.y),
			Point(p.x, p.y - 1)
		).filter { it.x >= 0 && it.y >= 0 && it.x < m && it.y < n && grid[it.y][it.x] == 0 }
	}

	fun allNeighbors(grid: Array<IntArray>, n: Int, m: Int): Array<Array<List<Point>>> {
		val allNeighbors = Array(n) { Array(m) { listOf<Point>() } }
		for ((i, row) in grid.withIndex()) {
			for ((j, c) in row.withIndex()) {
				if (c == 0) allNeighbors[i][j] = neighbors(grid, n, m, Point(j, i))
			}
		}
		return allNeighbors
	}

	fun removeImpossibleWays(allNeighbors: Array<Array<List<Point>>>) {
		var changed = true
		while (changed) {
			changed = false
			for ((i, row) in allNeighbors.withIndex()) {
				for ((j, neighbors) in row.withIndex()) {
					if (neighbors.size > 2) {
						f@ for (i1 in neighbors.indices) {
							val n1 = neighbors[i1]
							if (allNeighbors[n1.y][n1.x].size < 3) {
								for (i2 in i1 + 1 until neighbors.size) {
									val n2 = neighbors[i2]
									if (allNeighbors[n2.y][n2.x].size < 3) {
										allNeighbors[i][j] = listOf(n1, n2)
										for (p in neighbors - n1 - n2) {
											allNeighbors[p.y][p.x] = allNeighbors[p.y][p.x] - Point(j, i)
										}
										changed = true
										break@f
									}
								}
							}
						}
					}
				}
			}
		}
	}

	fun bfsDistances(n: Int, m: Int, allNeighbors: Array<Array<List<Point>>>, start: Point): Array<IntArray> {
		val distances = Array(n) { IntArray(m) { -1 } }
		val queue = mutableListOf<Point>()
		distances[start.y][start.x] = 0
		queue.add(start)
		while (queue.isNotEmpty()) {
			val curr = queue.removeFirst()
			val currDistance = distances[curr.y][curr.x]
			for (next in allNeighbors[curr.y][curr.x]) {
				if (distances[next.y][next.x] == -1) {
					distances[next.y][next.x] = currDistance + 1
					queue.add(next)
				}
			}
		}
		return distances
	}

	fun connected(grid: Array<IntArray>, n: Int, m: Int, allNeighbors: Array<Array<List<Point>>>, start: Point): Boolean {
		val distances = bfsDistances(n, m, allNeighbors, start)
		for ((i, row) in grid.withIndex()) {
			for ((j, c) in row.withIndex()) {
				if (c == 0 && distances[i][j] == -1) return false
			}
		}
		return true
	}

	fun badDiagonal(grid: Array<IntArray>, n: Int, m: Int): Boolean {
		if (n != m || n % 2 == 0) return false
		for ((i, row) in grid.withIndex()) {
			for ((j, c) in row.withIndex()) {
				if (c == 1 && (i + j) % 2 == 0) return false
			}
		}
		return true
	}

	fun tspRecurs(grid: Array<IntArray>, n: Int, m: Int, allNeighbors: Array<Array<List<Point>>>, exits: Array<IntArray>,
				  end: Point, path: MutableList<Point>, visited: Array<BooleanArray>, unvisited: Int, p: Point): Boolean {
		if (p == end) return unvisited == 1
		val freeNeighbors = allNeighbors[p.y][p.x].filter { !visited[it.y][it.x] }
		val smallNeighbors = freeNeighbors.filter { exits[it.y][it.x] == 2 }
		val neighbors = when (smallNeighbors.size) {
			0 -> freeNeighbors.sortedBy { exits[it.y][it.x] }
			1 -> smallNeighbors
			else -> return false
		}
		if (neighbors.isNotEmpty()) {
			visited[p.y][p.x] = true
			path.add(p)
			for (n in freeNeighbors) exits[n.y][n.x]--
			for (next in neighbors) {
				val res = tspRecurs(grid, n, m, allNeighbors, exits, end, path, visited, unvisited - 1, next)
				if (res) return true
			}
			visited[p.y][p.x] = false
			path.removeLast()
			for (n in freeNeighbors) exits[n.y][n.x]++
		}
		return false
	}

	fun tsp(grid: Array<IntArray>, n: Int, m: Int): Boolean {
		val start = findEmptyPoint(grid) ?: return false
		val allNeighbors = allNeighbors(grid, n, m)
		removeImpossibleWays(allNeighbors)
		val sDown = Point(start.x, start.y + 1)
		val sRight = Point(start.x + 1, start.y)
		val sDownEmpty = sDown.x < m && sDown.y < n && grid[sDown.y][sDown.x] == 0
		val sRightEmpty = sRight.x < m && sRight.y < n && grid[sRight.y][sRight.x] == 0
		if (sDownEmpty != sRightEmpty) return false
		var unvisited = grid.sumOf { row -> row.count { it == 0 } }
		if (unvisited % 2 == 1) return false
		val exits = Array(n) { IntArray(m) }
		for ((i, row) in grid.withIndex()) {
			for ((j, c) in row.withIndex()) {
				if (c == 1) continue
				val neighbors = allNeighbors[i][j]
				if (neighbors.size < 2) return false
				exits[i][j] = neighbors.size
			}
		}
		if (!connected(grid, n, m, allNeighbors, start)) return false
		if (badDiagonal(grid, n, m)) return false
		val visited = Array(n) { BooleanArray(m) }
		visited[start.y][start.x] = true
		val path = mutableListOf(start)
		exits[start.y][start.x]--
		exits[sDown.y][sDown.x]--
		unvisited--
		return tspRecurs(grid, n, m, allNeighbors, exits, sRight, path, visited, unvisited, sDown)
	}

	val t = readln().toInt()
	for (s in 0 until t) {
		val (n, m) = readln().split(' ').map { it.toInt() }
		val grid = Array(n) {
			readln().split(' ').map { it.toInt() }.toIntArray()
		}
		val result = tsp(grid, n ,m)
		println(if (result) "Yes" else "No")
	}
}
