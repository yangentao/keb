package dev.entao.kava.sql

import dev.entao.json.*
import dev.entao.kava.base.Name
import dev.entao.kava.base.Prop
import dev.entao.kava.base.ownerClass
import dev.entao.kava.log.logd
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/6/10.
 */

val KClass<*>.sqlName: String
	get() {
		return this.findAnnotation<Name>()?.value ?: this.simpleName!!.toLowerCase()
	}

val Prop.sqlName: String
	get() {
		return this.findAnnotation<Name>()?.value ?: this.name.toLowerCase()
	}
val Prop.sqlFullName: String
	get() {
		return "${this.ownerClass!!.sqlName}.${this.sqlName}"
	}



typealias TabClass = KClass<*>

val Connection.isMySQL: Boolean get() = "MySQL" == this.metaData.databaseProductName
val Connection.isPostgres: Boolean get() = "PostgreSQL" == this.metaData.databaseProductName


private const val pgKeywors = "desc,user,abort,access,aggregate,also,analyse,analyze,attach,backward,bit,cache,checkpoint,class,cluster,columns,comment,comments,concurrently,configuration,conflict,connection,content,conversion,copy,cost,csv,current_catalog,current_schema,database,delimiter,delimiters,depends,detach,dictionary,disable,discard,do,document,enable,encoding,encrypted,enum,event,exclusive,explain,extension,family,force,forward,freeze,functions,generated,greatest,groups,handler,header,if,ilike,immutable,implicit,import,include,index,indexes,inherit,inherits,inline,instead,isnull,label,leakproof,least,limit,listen,load,location,lock,locked,logged,mapping,materialized,mode,move,nothing,notify,notnull,nowait,off,offset,oids,operator,owned,owner,parallel,parser,passing,password,plans,policy,prepared,procedural,procedures,program,publication,quote,reassign,recheck,refresh,reindex,rename,replace,replica,reset,restrict,returning,routines,rule,schemas,sequences,server,setof,share,show,skip,snapshot,stable,standalone,statistics,stdin,stdout,storage,stored,strict,strip,subscription,support,sysid,tables,tablespace,temp,template,text,truncate,trusted,types,unencrypted,unlisten,unlogged,until,vacuum,valid,validate,validator,variadic,verbose,version,views,volatile,whitespace,wrapper,xml,xmlattributes,xmlconcat,xmlelement,xmlexists,xmlforest,xmlnamespaces,xmlparse,xmlpi,xmlroot,xmlserialize,xmltable,yes"
private const val mysqlKeywors = "ACCESSIBLE,ADD,ANALYZE,ASC,BEFORE,CASCADE,CHANGE,CONTINUE,DATABASE,DATABASES,DAY_HOUR,DAY_MICROSECOND,DAY_MINUTE,DAY_SECOND,DELAYED,DESC,DISTINCTROW,DIV,DUAL,ELSEIF,EMPTY,ENCLOSED,ESCAPED,EXIT,EXPLAIN,FIRST_VALUE,FLOAT4,FLOAT8,FORCE,FULLTEXT,GENERATED,GROUPS,HIGH_PRIORITY,HOUR_MICROSECOND,HOUR_MINUTE,HOUR_SECOND,IF,IGNORE,INDEX,INFILE,INT1,INT2,INT3,INT4,INT8,IO_AFTER_GTIDS,IO_BEFORE_GTIDS,ITERATE,JSON_TABLE,KEY,KEYS,KILL,LAG,LAST_VALUE,LEAD,LEAVE,LIMIT,LINEAR,LINES,LOAD,LOCK,LONG,LONGBLOB,LONGTEXT,LOOP,LOW_PRIORITY,MASTER_BIND,MASTER_SSL_VERIFY_SERVER_CERT,MAXVALUE,MEDIUMBLOB,MEDIUMINT,MEDIUMTEXT,MIDDLEINT,MINUTE_MICROSECOND,MINUTE_SECOND,NO_WRITE_TO_BINLOG,NTH_VALUE,NTILE,OPTIMIZE,OPTIMIZER_COSTS,OPTION,OPTIONALLY,OUTFILE,PURGE,READ,READ_WRITE,REGEXP,RENAME,REPEAT,REPLACE,REQUIRE,RESIGNAL,RESTRICT,RLIKE,SCHEMA,SCHEMAS,SECOND_MICROSECOND,SEPARATOR,SHOW,SIGNAL,SPATIAL,SQL_BIG_RESULT,SQL_CALC_FOUND_ROWS,SQL_SMALL_RESULT,SSL,STARTING,STORED,STRAIGHT_JOIN,TERMINATED,TINYBLOB,TINYINT,TINYTEXT,UNDO,UNLOCK,UNSIGNED,USAGE,USE,UTC_DATE,UTC_TIME,UTC_TIMESTAMP,VARBINARY,VARCHARACTER,VIRTUAL,WHILE,WRITE,XOR,YEAR_MONTH,ZEROFILL"

val pgKeySet: Set<String> = pgKeywors.split(',').toSet()
val mysqlKeySet: Set<String> = mysqlKeywors.toLowerCase().split(',').toSet()

val String.trimSQL: String
	get() {
		return this.trim('`', '\"')
	}


fun Connection.esc(name: String): String {
	if (isMySQL && (name in mysqlKeySet)) {
		return "`$name`"
	}
	if (isPostgres && (name in pgKeySet)) {
		return "\"$name\""
	}
	return name

}

class SQLArgs(val sql: String, val args: List<Any?> = emptyList())

infix fun String.AS(other: String): String {
	return "$this AS $other"
}

fun Connection.valPos(value: Any?): String {
	if (this.isPostgres) {
		if (value is YsonObject || value is YsonArray) {
			return "?::json"
		}
	}
	return "?"
}


fun PreparedStatement.setParams(params: List<Any?>) {
	for (i in params.indices) {
		val v = params[i]
		val vv: Any? = when (v) {
			is YsonNull -> null
			is YsonObject -> v.toString()
			is YsonArray -> v.toString()
			is YsonString -> v.data
			is YsonNum -> v.data
			is YsonBool -> v.data
			is YsonBlob -> v.data
			else -> v

		}
		this.setObject(i + 1, vv)
	}
}

fun Connection.exec(sa: SQLArgs): Boolean {
	return this.exec(sa.sql, sa.args)
}

fun Connection.exec(sql: String, args: List<Any?> = emptyList()): Boolean {
	if (ConnLook.logEnable) {
		logd(sql)
	}
	val st = this.prepareStatement(sql)
	st.setParams(args)
	return st.use {
		it.execute()
	}
}

fun Connection.query(sa: SQLArgs): ResultSet {
	return query(sa.sql, sa.args)
}

fun Connection.query(sql: String, args: List<Any?>): ResultSet {
	if (ConnLook.logEnable) {
		logd(sql)
		logd(args)
	}
	val st = this.prepareStatement(sql)
	st.setParams(args)
	return st.executeQuery()
}

fun Connection.update(sa: SQLArgs): Int {
	return this.update(sa.sql, sa.args)
}

fun Connection.update(sql: String, args: List<Any?> = emptyList()): Int {
	if (ConnLook.logEnable) {
		logd(sql)
		logd(args)
	}
	val st = this.prepareStatement(sql)
	st.setParams(args)
	return st.use {
		it.executeUpdate()
	}
}


inline fun Connection.trans(block: (Connection) -> Unit) {
	try {
		this.autoCommit = false
		block(this)
		this.commit()
	} catch (ex: Exception) {
		this.rollback()
		throw ex
	} finally {
		this.autoCommit = true
	}
}


