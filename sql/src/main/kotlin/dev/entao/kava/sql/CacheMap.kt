package dev.entao.kava.sql

class CacheMap<K, V>(val onMissing: (K) -> V?) {

	val map = HashMap<K, V?>()

	fun get(key: K): V? {
		if (!map.containsKey(key)) {
			val v = onMissing(key)
			map[key] = v
		}
		return map[key]
	}

	fun remove(key: K): V? {
		return map.remove(key)
	}
}