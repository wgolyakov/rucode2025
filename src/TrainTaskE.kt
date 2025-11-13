import java.util.BitSet

fun main() {
	fun toUBytes(x: ULong): Array<UByte> {
		val bytes = Array<UByte>(8) { 0U }
		bytes[0] = x.toUByte()
		bytes[1] = (x shr 8).toUByte()
		bytes[2] = (x shr 16).toUByte()
		bytes[3] = (x shr 24).toUByte()
		bytes[4] = (x shr 32).toUByte()
		bytes[5] = (x shr 40).toUByte()
		bytes[6] = (x shr 48).toUByte()
		bytes[7] = (x shr 56).toUByte()
		return bytes
	}

	fun toULong(bytes: Array<UByte>): ULong {
		return bytes[0].toULong() or
				(bytes[1].toULong() shl 8) or
				(bytes[2].toULong() shl 16) or
				(bytes[3].toULong() shl 24) or
				(bytes[4].toULong() shl 32) or
				(bytes[5].toULong() shl 40) or
				(bytes[6].toULong() shl 48) or
				(bytes[7].toULong() shl 56)
	}

	fun swap(x: ULong): ULong {
		return toULong(toUBytes(x).reversed().toTypedArray())
	}

	fun directionBit(x: ULong): Boolean {
		val bytes = toUBytes(x)
		for (b in 0 until 4) {
			val b1 = bytes[b].toUInt()
			val b2 = bytes[7 - b].toUInt()
			for (i in 0 until 8) {
				val bit1 = b1 and (1U shl i) != 0U
				val bit2 = b2 and (1U shl i) != 0U
				if (bit1 != bit2) return bit1
			}
		}
		return false
	}

	fun directionLongs(list: List<ULong>): List<ULong> {
		val directionBits = BitSet()
		for ((i, x) in list.withIndex()) {
			directionBits.set(i, directionBit(x))
		}
		val result = directionBits.toLongArray().map { it.toULong() }
		if (result.isNotEmpty()) return result
		return listOf(0U)
	}

	fun unMirror(x: ULong): ULong {
		val bytes = toUBytes(x)
		bytes[4] = 0U
		bytes[5] = 0U
		bytes[6] = 0U
		bytes[7] = 0U
		return toULong(bytes)
	}

	fun decode(arr: MutableList<ULong>, dirLongs: List<ULong>) {
		val directionBits = BitSet.valueOf(dirLongs.map { it.toLong() }.toLongArray())
		for (i in 0 until arr.size) {
			if (directionBit(arr[i]) != directionBits.get(i)) arr[i] = swap(arr[i])
		}
	}

	val type = readln()
	if (type == "encode") {
		val n = readln().toInt()
		val arr = readln().split(' ').map { it.toULong() }
		val dirLongs = directionLongs(arr)
		val dirLongsCount = dirLongs.size.toULong()
		val dirDirLong = directionLongs(dirLongs).first()
		val mirroredLong = dirDirLong or swap(dirDirLong)
		val mirroredCount = dirLongsCount or swap(dirLongsCount)
		val result = arr + dirLongs + mirroredCount + mirroredLong
		println(result.size)
		println(result.joinToString(" "))
	} else if (type == "decode") {
		val k = readln().toInt()
		val list = readln().split(' ').map { it.toULong() }
		val mirroredLong = list.last()
		val dirDirLong = unMirror(mirroredLong)
		val mirroredCount = list[k - 2]
		val dirLongsCount = unMirror(mirroredCount).toInt()
		val dirLongs = list.subList(k - 2 - dirLongsCount, k - 2).toMutableList()
		val arr = list.subList(0, k - 2 - dirLongsCount).toMutableList()
		decode(dirLongs, listOf(dirDirLong))
		decode(arr, dirLongs)
		println(arr.joinToString(" "))
	}
}
