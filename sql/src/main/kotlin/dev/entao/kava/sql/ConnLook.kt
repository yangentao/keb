@file:Suppress("MemberVisibilityCanBePrivate")

package dev.entao.kava.sql

import dev.entao.kava.base.Task
import java.sql.Connection
import java.sql.DriverManager
import javax.naming.InitialContext
import javax.naming.NameNotFoundException
import javax.sql.DataSource
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

interface ConnMaker {
	fun defaultConnection(): Connection
	fun namedConnection(name: String): Connection
}

class MySQLConnMaker(val url: String, val user: String, val pwd: String) : ConnMaker {
	private val driverName = "com.mysql.cj.jdbc.Driver"
	override fun defaultConnection(): Connection {
		return namedConnection("")
	}

	@Throws
	override fun namedConnection(name: String): Connection {
		Class.forName(driverName)
		return DriverManager.getConnection(url, user, pwd)
	}
}

class PostgreSQLConnMaker(val url: String, val user: String, val pwd: String) : ConnMaker {
	private val driverName = "org.postgresql.Driver"
	override fun defaultConnection(): Connection {
		return namedConnection("")
	}

	@Throws
	override fun namedConnection(name: String): Connection {
		Class.forName(driverName)
		return DriverManager.getConnection(url, user, pwd)
	}
}

class WebContextConnMaker : ConnMaker {
	private val prefix = "java:comp/env/jdbc"
	private val ctx = InitialContext()
	private val nameList = ArrayList<String>()

	init {
		val ls = ctx.list(prefix)
		while (ls.hasMore()) {
			val a = ls.next()
			nameList += a.name
		}
	}

	override fun defaultConnection(): Connection {
		return namedConnection(nameList.first())
	}

	override fun namedConnection(name: String): Connection {
		val defaultDataSource = ctx.lookup("${prefix}/$name") as DataSource
		return defaultDataSource.connection
	}

}

private const val pgKeywors = "user,abort,access,aggregate,also,analyse,analyze,attach,backward,bit,cache,checkpoint,class,cluster,columns,comment,comments,concurrently,configuration,conflict,connection,content,conversion,copy,cost,csv,current_catalog,current_schema,database,delimiter,delimiters,depends,detach,dictionary,disable,discard,do,document,enable,encoding,encrypted,enum,event,exclusive,explain,extension,family,force,forward,freeze,functions,generated,greatest,groups,handler,header,if,ilike,immutable,implicit,import,include,index,indexes,inherit,inherits,inline,instead,isnull,label,leakproof,least,limit,listen,load,location,lock,locked,logged,mapping,materialized,mode,move,nothing,notify,notnull,nowait,off,offset,oids,operator,owned,owner,parallel,parser,passing,password,plans,policy,prepared,procedural,procedures,program,publication,quote,reassign,recheck,refresh,reindex,rename,replace,replica,reset,restrict,returning,routines,rule,schemas,sequences,server,setof,share,show,skip,snapshot,stable,standalone,statistics,stdin,stdout,storage,stored,strict,strip,subscription,support,sysid,tables,tablespace,temp,template,text,truncate,trusted,types,unencrypted,unlisten,unlogged,until,vacuum,valid,validate,validator,variadic,verbose,version,views,volatile,whitespace,wrapper,xml,xmlattributes,xmlconcat,xmlelement,xmlexists,xmlforest,xmlnamespaces,xmlparse,xmlpi,xmlroot,xmlserialize,xmltable,yes"
private const val mysqlKeywors = "ACCESSIBLE,ADD,ANALYZE,ASC,BEFORE,CASCADE,CHANGE,CONTINUE,DATABASE,DATABASES,DAY_HOUR,DAY_MICROSECOND,DAY_MINUTE,DAY_SECOND,DELAYED,DESC,DISTINCTROW,DIV,DUAL,ELSEIF,EMPTY,ENCLOSED,ESCAPED,EXIT,EXPLAIN,FIRST_VALUE,FLOAT4,FLOAT8,FORCE,FULLTEXT,GENERATED,GROUPS,HIGH_PRIORITY,HOUR_MICROSECOND,HOUR_MINUTE,HOUR_SECOND,IF,IGNORE,INDEX,INFILE,INT1,INT2,INT3,INT4,INT8,IO_AFTER_GTIDS,IO_BEFORE_GTIDS,ITERATE,JSON_TABLE,KEY,KEYS,KILL,LAG,LAST_VALUE,LEAD,LEAVE,LIMIT,LINEAR,LINES,LOAD,LOCK,LONG,LONGBLOB,LONGTEXT,LOOP,LOW_PRIORITY,MASTER_BIND,MASTER_SSL_VERIFY_SERVER_CERT,MAXVALUE,MEDIUMBLOB,MEDIUMINT,MEDIUMTEXT,MIDDLEINT,MINUTE_MICROSECOND,MINUTE_SECOND,NO_WRITE_TO_BINLOG,NTH_VALUE,NTILE,OPTIMIZE,OPTIMIZER_COSTS,OPTION,OPTIONALLY,OUTFILE,PURGE,READ,READ_WRITE,REGEXP,RENAME,REPEAT,REPLACE,REQUIRE,RESIGNAL,RESTRICT,RLIKE,SCHEMA,SCHEMAS,SECOND_MICROSECOND,SEPARATOR,SHOW,SIGNAL,SPATIAL,SQL_BIG_RESULT,SQL_CALC_FOUND_ROWS,SQL_SMALL_RESULT,SSL,STARTING,STORED,STRAIGHT_JOIN,TERMINATED,TINYBLOB,TINYINT,TINYTEXT,UNDO,UNLOCK,UNSIGNED,USAGE,USE,UTC_DATE,UTC_TIME,UTC_TIMESTAMP,VARBINARY,VARCHARACTER,VIRTUAL,WHILE,WRITE,XOR,YEAR_MONTH,ZEROFILL"

val pgKeySet: Set<String> = pgKeywors.split(',').toSet()
val mysqlKeySet: Set<String> = mysqlKeywors.toLowerCase().split(',').toSet()


fun Connection.escape(name: String): String {
	if (isMySQL && name in mysqlKeySet) {
		return "`$name`"
	}
	if (isPostgres && name in pgKeySet) {
		return "\"$name\""
	}
	return name

}

//先赋值maker, 后使用
object ConnLook {
	lateinit var maker: ConnMaker
	var logEnable = true

	private val threadLocal: ThreadLocal<HashMap<String, Connection>> =
			object : ThreadLocal<HashMap<String, Connection>>() {
				override fun initialValue(): HashMap<String, Connection> {
					return HashMap()
				}
			}

	init {
		Task.setCleanBlock("connlook", this::cleanThreadConnections)
	}

	fun cleanThreadConnections() {
		val map = threadLocal.get()
		for (c in map.values) {
			c.close()
		}
		map.clear()
		threadLocal.remove()
	}

	fun named(name: String): Connection {
		val map = threadLocal.get()
		return map.getOrPut(name) {
			maker.namedConnection(name)
		}
	}

	val first: Connection get() = maker.defaultConnection()

	fun named(modelClass: KClass<*>): Connection {
		val cname = modelClass.findAnnotation<ConnName>() ?: return first
		if (cname.value.isEmpty()) {
			return first
		}
		return named(cname.value)
	}
}

val KClass<*>.namedConn: Connection get() = ConnLook.named(this)