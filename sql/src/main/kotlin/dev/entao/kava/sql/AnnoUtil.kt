package dev.entao.kava.sql

import dev.entao.kava.base.Exclude
import dev.entao.kava.base.Name
import dev.entao.kava.base.userName
import dev.entao.kava.base.ownerClass
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/4/6.
 */

val String.sqlEscaped: String
	get() {
		return this
//		val buf = StringBuilder(this.length + 2)
//		if (!this.startsWith('`')) {
//			buf.append('`')
//		}
//		buf.append(this)
//		if (!this.endsWith('`')) {
//			buf.append('`')
//		}
//		return buf.toString()
	}

//==
val KClass<*>.sqlName: String
	get() {
		return this.userName
	}

val KProperty<*>.sqlName: String
	get() {
		return this.userName.sqlEscaped
	}
val KProperty<*>.sqlFullName: String
	get() {
		return "${this.ownerClass!!.userName}.${this.sqlName}"
	}


val KProperty<*>.isExcluded: Boolean
	get() {
		return this.findAnnotation<Exclude>() != null
	}
val KProperty<*>.isPrimaryKey: Boolean
	get() {
		return this.findAnnotation<PrimaryKey>() != null
	}
