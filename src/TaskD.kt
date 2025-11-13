fun main() {
	val (a, b, r) = readln().split(' ').map { it.toInt() }
	val c = if (a * a + b * b < r * r) {
		5
	} else if (a * a < r * r) {
		if (b * b < r * r) 4
		else if (b * b > r * r) 3
		else 3
	} else if (a * a > r * r)  {
		if (b * b < r * r) 3
		else if (b * b > r * r) 1
		else 2
	} else {
		if (b * b < r * r) 3
		else if (b * b > r * r) 2
		else 2
	}
	println(c)
}
