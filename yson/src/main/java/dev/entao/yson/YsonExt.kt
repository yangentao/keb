package dev.entao.yson

/**
 * Created by entaoyang@163.com on 2018/8/7.
 */


inline fun YsonArray.eachObject(block: (YsonObject) -> Unit) {
	this.forEach {
		if (it !is YsonNull) {
			block(it as YsonObject)
		}
	}

}


inline fun YsonArray.eachString(block: (String) -> Unit) {
	this.forEach {
		if (it !is YsonNull) {
			val ys = it as YsonString
			block(ys.data)
		}
	}

}

inline fun YsonArray.eachDouble(block: (Double) -> Unit) {
	this.forEach {
		if (it !is YsonNull) {
			if (it is YsonNum) {
				block(it.data.toDouble())
			}
		}
	}

}

inline fun YsonArray.eachInt(block: (Int) -> Unit) {
	this.forEach {
		if (it !is YsonNull) {
			if (it is YsonNum) {
				block(it.data.toInt())
			}
		}
	}

}

inline fun YsonArray.eachLong(block: (Long) -> Unit) {
	this.forEach {
		if (it !is YsonNull) {
			if (it is YsonNum) {
				block(it.data.toLong())
			}
		}
	}

}