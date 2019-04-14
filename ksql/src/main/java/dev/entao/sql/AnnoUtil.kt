package dev.entao.sql

import dev.entao.kbase.Name
import dev.entao.kbase.userName
import dev.entao.kbase.ownerClass
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/4/6.
 */



//==
val KClass<*>.sqlName: String
	get() {
		return "`" + this.userName + "`"
	}

val KProperty<*>.sqlFullName: String
	get() {
		val tabName = this.ownerClass!!.userName
		val fname = this.findAnnotation<Name>()?.value ?: this.name
		return "`$tabName`.`$fname`"
	}


val KProperty<*>.isExcluded: Boolean
	get() {
		return this.findAnnotation<Exclude>() != null
	}
val KProperty<*>.isPrimaryKey: Boolean
	get() {
		return this.findAnnotation<PrimaryKey>() != null
	}
