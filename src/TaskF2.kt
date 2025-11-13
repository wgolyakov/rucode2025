fun main() {
	val all = setOf('а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р',
		'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я')
	val glasn = setOf('а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я')
	val soglasn = setOf('б', 'в', 'г', 'д', 'ж', 'з', 'й', 'к', 'л', 'м', 'н', 'п', 'р', 'с', 'т', 'ф', 'х', 'ц', 'ч', 'ш', 'щ')
	val soglasn2 = setOf('б', 'в', 'г', 'д', 'ж', 'з', 'к', 'л', 'м', 'н', 'п', 'р', 'с', 'т', 'ф', 'х', 'ц', 'ч', 'ш', 'щ')
	val spec = setOf('ъ', 'ь')
	val d = 1_000_000_007
	//val n = readln().toInt()
	var goodCount = 0

	fun isGood(s: String): Boolean {
		if (s[0] in spec) return false
		for (pair in s.windowed(2)) {
			val a = pair[0]
			val b = pair[1]
			if (b in spec && a !in soglasn2) return false
			if (a in spec && b in soglasn) return false
			if (a in glasn && b in glasn) return false
			if (a in soglasn && b in soglasn) return false
		}
		return true
	}

	fun countRecurs(n: Int, pass: String) {
		if (pass.length == n) {
			if (isGood(pass)) goodCount++
		} else {
			for (c in all) countRecurs(n, "$pass$c")
		}
	}

	for (n in 1..6) {
		goodCount = 0
		countRecurs(n, "")
		println("$n: $goodCount")
	}
}
// n: good		+good		all			bad
// 1: 31		+0			33			2
// 2: 460		+429		1089		629
// 3: 7310		+6850		35937		28627
// 4: 109000	+101690		1185921		1076921
// 5: 1719100	+1610100	39135393	37416293
// 6: 25814000	+24094900	1291467969	1265653969
