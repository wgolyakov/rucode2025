fun main() {
	val a = readln().toInt()
	val b = readln().toInt()
	println(((a.toDouble()/b) * 10).toInt() % 10)
}
