package dev.entao.ken.anno

import dev.entao.kava.base.userName
import dev.entao.sql.*
import dev.entao.kava.log.loge
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/4/6.
 */


val KProperty<*>.formOptionsMap: Map<String, String>
	get() {
		val fs = this.findAnnotation<FormOptions>() ?: return emptyMap()
		val arr = fs.options
		val map = LinkedHashMap<String, String>()
		arr.forEach {
			val kv = it.split(":")
			if (kv.size == 2) {
				map[kv[0]] = kv[1]
			} else if (kv.size == 1) {
				map[kv[0]] = kv[0]
			}
		}
		return map
	}

private fun keyValueMapByTable(tableName: String, keyCol: String, labelCol: String, w: Where? = null): Map<String, String> {
	val map = LinkedHashMap<String, String>()
	if (keyCol == labelCol) {
		val q = SQLQuery().from(tableName).select(keyCol).asc(keyCol).distinct().where(w)
		val ls = ConnLook.first.query(q).allRows()
		ls.forEach {
			val k = it[keyCol]
			if (k != null) {
				map.put(k.toString(), k.toString())
			}
		}
	} else {
		val q = SQLQuery().from(tableName).select(keyCol, labelCol).asc(labelCol).distinct().where(w)
		val ls = ConnLook.first.query(q).allRows()
		ls.forEach {
			val k = it[keyCol]
			val v = it[labelCol]
			if (k != null && v != null) {
				map[k.toString()] = v.toString()
			}
		}
	}
	return map
}

fun KProperty<*>.selectOptionsTable(w: Where? = null): Map<String, String> {
	val map = HashMap<String, String>()
	val fk = this.findAnnotation<ForeignKey>()
	val fcol = this.findAnnotation<ForeignLabel>()

	if (fk != null && fcol != null) {
		if (fk.cls.modelPrimaryKeys.size != 1) {
			loge("主键必须只有一列")
			return emptyMap()
		}
		return keyValueMapByTable(fk.cls.sqlName, fk.cls.modelPrimaryKeys.first().userName, fcol.labelCol, w)
	}
	val ft = this.findAnnotation<FormSelectFromTable>() ?: return map
	if (ft.keyCol.isEmpty()) {
		return map
	}
	return keyValueMapByTable(ft.tableName, ft.keyCol, ft.labelCol, w)
}

fun KProperty<*>.singleSelectDisplay(v: Any): String? {
	val a = this.formOptionsMap[v.toString()]
	if (a != null) {
		return a
	}
	val fk = this.findAnnotation<ForeignKey>()
	val fcol = this.findAnnotation<ForeignLabel>()
	if (fk != null && fcol != null) {
		if (fk.cls.modelPrimaryKeys.size != 1) {
			loge("主键必须只有一列")
			return null
		}
		return findLableOfKey(fk.cls.sqlName, fk.cls.modelPrimaryKeys.first().userName, fcol.labelCol, v)
	}

	val ft = this.findAnnotation<FormSelectFromTable>() ?: return null
	return findLableOfKey(ft.tableName, ft.keyCol, ft.labelCol, v)
}

private fun findLableOfKey(tableName: String, keyCol: String, labelCol: String, keyValue: Any): String? {
	val q = SQLQuery().from(tableName).select(labelCol).limit(1).where(keyCol EQ keyValue)
	val a: Any? = ConnLook.first.query(q).anyValue
	return a?.toString() ?: ""
}