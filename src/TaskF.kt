fun main() {
	val d = 1_000_000_007
	fun add(vararg a: Int) = a.reduce { x, y -> (x + y) % d }
	fun mul(x: Int, y: Int) = ((x.toLong() * y) % d).toInt()

	val n = readln().toInt()
	// First letter any except two special
	var glasnCount = 10
	var soglasnCount = 20
	var yCount = 1
	var specCount = 0
	for (i in 2..n) {
		val glasnCount2 = mul(add(soglasnCount, yCount, specCount), 10)
		val soglasnCount2 = mul(glasnCount, 20)
		val yCount2 = glasnCount
		val specCount2 = mul(soglasnCount, 2)
		glasnCount = glasnCount2
		soglasnCount = soglasnCount2
		yCount = yCount2
		specCount = specCount2
	}
	println(add(glasnCount, soglasnCount, yCount, specCount))
}
