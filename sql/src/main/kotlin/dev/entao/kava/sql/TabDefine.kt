package dev.entao.kava.sql

import dev.entao.kava.base.*
import dev.entao.kava.json.YsonArray
import dev.entao.kava.json.YsonObject
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

private const val typeMYSQL: Int = 0
private const val typePostgresql: Int = 1

class DefTable(private val cls: KClass<*>) {
	private val conn: Connection by lazy { cls.namedConn }
	private val dbType: Int = if (conn.isMySQL) typeMYSQL else if (conn.isPostgres) typePostgresql else throw java.lang.IllegalArgumentException("只支持MySQL或PostgreSQL")
	private val name: String = cls.sqlName
	private val autoCreate: Boolean = cls.findAnnotation<AutoCreateTable>()?.value ?: true
	private val columns: List<DefColumn> = cls.modelProperties.map { DefColumn(it) }

	init {
		if (autoCreate) {
			if (!conn.tableExists(name)) {
				createTable(conn)
			} else {
				mergeTable()
				mergeIndex()
			}
		}
	}

	private fun mergeIndex() {
		val oldIdxs = conn.tableIndexList(name).map { it.colName.toLowerCase() }.toSet()
		val newIdxs = columns.filter { it.index }
		for (p in newIdxs) {
			if (p.name !in oldIdxs) {
				val idxName = "${p.name}_INDEX"
				conn.exec("CREATE INDEX  $idxName ON $name(${p.name})")
			}
		}
	}

	private fun mergeTable() {
		val cols: Set<String> = conn.tableDesc(name).map { it.columnName.toLowerCase() }.toSet()
		for (p in columns) {
			if (p.name !in cols) {
				val s = p.defColumnn(dbType)
				conn.exec("ALTER TABLE $name ADD COLUMN $s")
			}
		}
	}

	private fun createTable(conn: Connection) {
		val colList: MutableList<String> = columns.map { it.defColumnn(dbType) }.toMutableList()
		val pkCols = columns.filter { it.pk }.joinToString(",") { it.name }
		if (pkCols.isNotEmpty()) {
			colList += "PRIMARY KEY ($pkCols)"
		}
		val uniq = columns.filter { it.unique != null && it.unique.isEmpty() }
		for (u in uniq) {
			colList += "UNIQUE (${u.name}) "
		}
		val uniq2 = columns.filter { it.unique != null && it.unique.isNotEmpty() }.groupBy { it.unique }
		for ((k, ls) in uniq2) {
			val s = ls.joinToString(",") { it.name }
			colList += if (dbType == typeMYSQL) {
				"CONSTRAINT $k UNIQUE ($s) "
			} else {
				"UNIQUE ($s) "
			}
		}
		conn.createTable(name, colList)

		val idxList = columns.filter { it.index }
		for (idx in idxList) {
			conn.exec("CREATE INDEX ${idx.name}_index ON $name (${idx.name})")
		}
	}
}

class DefColumn(private val prop: Prop) {
	val name: String = prop.sqlName
	val pk: Boolean = prop.hasAnnotation<PrimaryKey>()
	val index: Boolean = prop.hasAnnotation<Index>()
	val unique: String? = prop.findAnnotation<Unique>()?.value
	private val autoInc: Boolean = prop.hasAnnotation<AutoInc>()
	private val notNull: Boolean = prop.hasAnnotation<NotNull>()
	private val sqlType: String? = prop.findAnnotation<SQLType>()?.value
	private val lengthValue: Int = prop.findAnnotation<Length>()?.value ?: 256
	private val defaultValue: String? = prop.findAnnotation<DefaultValue>()?.value
	private val labelValue: String? = prop.labelOnly
	private val decimal: Pair<Int, Int>?

	init {
		val n = prop.findAnnotation<DecimalDef>()
		decimal = if (n == null) {
			null
		} else {
			n.p to n.s
		}
	}

	fun defColumnn(dbType: Int): String {
		val sb = StringBuilder(64)
		sb.append(name)
		sb.append(" ")
		val typeDefStr = makeTypeString(dbType).toLowerCase()
		sb.append(typeDefStr)
		sb.append(" ")
		if (notNull) {
			sb.append("NOT NULL")
		} else {
			sb.append("NULL")
		}
		sb.append(" ")
		if ("json" !in typeDefStr && "blob" !in typeDefStr && "text" !in typeDefStr && "bytea" !in typeDefStr) {
			if (defaultValue != null && defaultValue.isNotEmpty()) {
				sb.append("DEFAULT ")
				if (prop.isTypeString) {
					sb.append("'$defaultValue'")
				} else {
					sb.append(defaultValue)
				}
			} else if (!notNull) {
				val s = makeDefaultValue()
				if (s.isNotEmpty()) {
					sb.append("DEFAULT ")
					sb.append(s)
				}
			}

		}
		sb.append(" ")
		if (autoInc) {
			if (dbType == typeMYSQL) {
				sb.append("AUTO_INCREMENT")
			}
		}
		sb.append(" ")
		if (labelValue != null) {
			sb.append("COMMENT '$labelValue'")
		}

		return sb.toString()
	}

	private fun makeTypeString(dbType: Int): String {
		if (sqlType != null) {
			return sqlType
		}
		if (prop.isTypeString) {
			return if (lengthValue >= 65535) {
				if (dbType == typePostgresql) {
					"text"
				} else {
					"longtext"
				}
			} else {
				"varchar($lengthValue)"
			}
		}
		if (prop.isTypeInt) {
			return if (dbType == typePostgresql) {
				if (autoInc) {
					"serial"
				} else {
					"integer"
				}
			} else {
				"integer"
			}

		}
		if (prop.isTypeLong) {
			return if (autoInc && dbType == typePostgresql) {
				"bigserial"
			} else {
				"bigint"
			}
		}
		if (prop.isTypeFloat) {
			return if (decimal != null) {
				if (dbType == typeMYSQL) {
					"decimal(${decimal.first},${decimal.second})"
				} else {
					"numeric(${decimal.first},${decimal.second})"
				}
			} else if (dbType == typeMYSQL) {
				"float"
			} else {
				"real"
			}
		}
		if (prop.isTypeDouble) {
			return if (decimal != null) {
				if (dbType == typeMYSQL) {
					"decimal(${decimal.first},${decimal.second})"
				} else {
					"numeric(${decimal.first},${decimal.second})"
				}
			} else if (dbType == typeMYSQL) {
				"double"
			} else {
				"double precision"
			}
		}
		if (prop.isTypeClass(java.sql.Date::class)) {
			return "date"
		}
		if (prop.isTypeClass(java.sql.Time::class)) {
			return "time"
		}
		if (prop.isTypeClass(java.sql.Timestamp::class)) {
			return "timestamp"
		}
		if (prop.isTypeClass(java.util.Date::class)) {
			return "DATETIME"
		}
		if (prop.isTypeClass(YsonArray::class) || prop.isTypeClass(YsonObject::class)) {
			return if (dbType == typeMYSQL) {
				"JSON"
			} else {
				"json" //jsonb
			}
		}
		if (prop.isTypeClass(ByteArray::class)) {
			return if (dbType == typeMYSQL) {
				if (lengthValue < 65535) {
					"blob"
				} else {
					"longblob"
				}
			} else {
				"bytea"
			}
		}
		throw IllegalArgumentException("不支持的类型:" + prop.fullName)
	}

	private fun makeDefaultValue(): String {
		if (pk || autoInc || unique != null) {
			return ""
		}
		if (prop.isTypeString) {
			return "''"
		}
		if (prop.isTypeInt || prop.isTypeLong || prop.isTypeByte || prop.isTypeShort) {
			return "0"
		}
		if (prop.isTypeFloat || prop.isTypeDouble) {
			return "0"
		}
		if (prop.isTypeClass(java.sql.Date::class)) {
			return "'1970-01-01'"
		}
		if (prop.isTypeClass(java.sql.Time::class)) {
			return "'00:00:00'"
		}
		if (prop.isTypeClass(java.sql.Timestamp::class)) {
			return "'1970-01-01 00:00:00'"
		}
		if (prop.isTypeClass(java.util.Date::class)) {
			return "'1970-01-01 00:00:00'"
		}
		return ""
	}
}