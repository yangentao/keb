package dev.entao.kava.sql

import dev.entao.kava.base.*
import dev.entao.kava.log.logd
import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation

//TODO decimal
//TODO mysql, postgresql, sql-server, oracle
class ModelMerge(private val modelClass: KClass<*>) {

	private val columnProperties: List<KMutableProperty<*>> = modelClass.modelProperties
	private val primaryKeyColumns: List<KMutableProperty<*>> = modelClass.modelPrimaryKeys

	init {
		val a = modelClass.findAnnotation<AutoCreateTable>()
		if (a == null || a.value) {
			val tabName = modelClass.sqlName

			val pList = columnProperties
			val existTable = ConnLook.named(modelClass).tableExists(tabName)
			if (!existTable) {
				createTableIfNotExists(ConnLook.named(modelClass))
			} else {
				val c = ConnLook.named(modelClass)
				val colList = c.tableDesc(tabName)
				mergeTable(c, tabName, colList, pList)

				val indexList = c.tableIndexList(tabName)
				mergeIndex(c, tabName, indexList, pList)

			}
		}
	}

	private fun mergeTable(c: Connection, tabName: String, cols: List<ColumnInfo>, ps: List<KMutableProperty<*>>) {
		for (p in ps) {
			val info = cols.find { it.columnName == p.userName }
			if (info == null) {
				val s = defColumnn(p)
				c.exec("ALTER TABLE $tabName ADD COLUMN $s")
			}
		}
	}

	private fun mergeIndex(c: Connection, tabName: String, cols: List<IndexInfo>, ps: List<KMutableProperty<*>>) {
		val ls = ps.filter { it.hasAnnotation<Index>() || it.hasAnnotation<ForeignKey>() }
		for (p in ls) {
			if (null == cols.find { it.colName == p.userName }) {
				val idxName = "${p.userName}_INDEX".sqlEscaped
				c.exec("ALTER TABLE $tabName ADD INDEX $idxName (${p.sqlName})")
			}
		}
	}

	fun createTableIfNotExists(c: Connection) {
		val tabName = modelClass.sqlName

		val ls = ArrayList<String>()
		columnProperties.forEach {
			ls.add(defColumnn(it))
		}
		val pks = primaryKeyColumns
		if (pks.isNotEmpty()) {
			val pkcol = pks.map { it.sqlName }.joinToString(",")
			ls.add("PRIMARY KEY ($pkcol)")
		}
		val uls = columnProperties.filter {
			val u = it.findAnnotation<Unique>()
			u != null && u.value.trim().isEmpty()
		}
		uls.forEach {
			val idxName = "${it.userName}_UNIQUE".sqlEscaped
			ls.add("UNIQUE INDEX $idxName (${it.sqlName} ASC)")
		}

		val uls2 = columnProperties.filter {
			val u = it.findAnnotation<Unique>()
			u != null && u.value.trim().isNotEmpty()
		}.toMutableList()
		if (uls2.isNotEmpty()) {
			while (uls2.isNotEmpty()) {
				val first = uls2.first()
				val uname = first.findAnnotation<Unique>()!!.value
				val cols = uls2.filter {
					it.findAnnotation<Unique>()?.value == first.findAnnotation<Unique>()?.value
				}
				val cs = cols.map { it.sqlName }.joinToString(",")
				val idxName = "${uname}_UNIQUE".sqlEscaped
				ls.add("UNIQUE INDEX $idxName ($cs)")
				uls2.removeAll(cols)
			}
		}
		val indexList = columnProperties.filter { it.hasAnnotation<Index>() || it.hasAnnotation<ForeignKey>() }
		indexList.forEach {
			val idxName = "${it.userName}_INDEX".sqlEscaped
			ls.add("INDEX $idxName (${it.sqlName})")
		}

		val n = c.createTable(tabName, ls)
		logd("创建表:", tabName, n)
	}

	private fun canNull(p: KMutableProperty<*>): Boolean {
		if (p.isPrimaryKey || p.hasAnnotation<NotNull>()) {
			return false
		}
		return p.returnType.isMarkedNullable
	}

	private fun defColumnn(p: KMutableProperty<*>): String {
		val sb = StringBuilder(64)
		sb.append(p.sqlName)
		sb.append(" ")
		val typeDefStr = makeTypeString(p)
		sb.append(typeDefStr)
		sb.append(" ")
		if (canNull(p)) {
			sb.append("NULL")
		} else {
			sb.append("NOT NULL")
		}
		sb.append(" ")
		if (!typeDefStr.startsWith("JSON") && !typeDefStr.startsWith("TEXT") && !typeDefStr.startsWith("BLOB")) {
			val dv = p.findAnnotation<DefaultValue>()
			val s = makeDefaultValue(p, dv?.value ?: "")
			if (s.isNotEmpty()) {
				sb.append("DEFAULT ")
				sb.append(s)
			}
		}
		sb.append(" ")
		if (p.hasAnnotation<AutoInc>()) {
			sb.append("AUTO_INCREMENT")
		}
		sb.append(" ")
		val lb = p.labelOnly
		if (lb != null) {
			sb.append("COMMENT '$lb'")
		}

		return sb.toString()
	}

	private fun makeTypeString(p: KMutableProperty<*>): String {
		val sqlType = p.findAnnotation<SQLType>()
		if (sqlType != null) {
			return sqlType.value
		}
		if (p.isTypeString) {
			val L = p.findAnnotation<Length>() ?: return "VARCHAR(256)"
			if (L.value <= 0) {
				return "VARCHAR(256)"
			} else if (L.value >= 65535) {
				return "TEXT(${L.value})"
			} else {
				return "VARCHAR(${L.value})"
			}
		}
		if (p.isTypeInt) {
			return "INT"
		}
		if (p.isTypeLong) {
			return "BIGINT"
		}
		if (p.isTypeFloat || p.isTypeDouble) {
			return "DOUBLE"
		}
		if (p.isTypeClass(java.sql.Date::class)) {
			return "DATE"
		}
		if (p.isTypeClass(java.sql.Time::class)) {
			return "TIME"
		}
		if (p.isTypeClass(java.sql.Timestamp::class)) {
			return "TIMESTAMP"
		}
		if (p.isTypeClass(java.util.Date::class)) {
			return "DATETIME"
		}
		if (p.isTypeClass(YsonArray::class) || p.isTypeClass(YsonObject::class)) {
			return "JSON"
		}
		if (p.isTypeClass(ByteArray::class)) {
			return "BLOB"
		}
		throw IllegalArgumentException("不支持的类型:" + p.fullName)
	}

	private fun makeDefaultValue(p: KMutableProperty<*>, s: String): String {
		if (p.isPrimaryKey || p.hasAnnotation<AutoInc>() || p.hasAnnotation<Unique>()) {
			return ""
		}

		if (p.isTypeString) {
			return "'$s'"
		}
		if (p.isTypeInt || p.isTypeLong || p.isTypeByte || p.isTypeShort) {
			if (s.isNotEmpty()) {
				return s
			}
			return "0"
		}
		if (p.isTypeFloat || p.isTypeDouble) {
			if (s.isNotEmpty()) {
				return s
			}
			return "0.0"
		}
		if (p.isTypeClass(java.sql.Date::class)) {
			if (s.isNotEmpty()) {
				return s
			}
			return "'1000-01-01'"
		}
		if (p.isTypeClass(java.sql.Time::class)) {
			if (s.isNotEmpty()) {
				return "'$s'"
			}
			return "'00:00:00'"
		}
		if (p.isTypeClass(java.sql.Timestamp::class)) {
			if (s.isNotEmpty()) {
				return "'$s'"
			}
			return "'1970-01-01 00:00:01'"
		}
		if (p.isTypeClass(java.util.Date::class)) {
			if (s.isNotEmpty()) {
				return "'$s'"
			}
			return "'1000-01-01 00:00:00'"
		}

		return ""
	}
}