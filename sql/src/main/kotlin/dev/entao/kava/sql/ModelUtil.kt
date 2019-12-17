package dev.entao.kava.sql

import dev.entao.kava.base.isPublic
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * Created by entaoyang@163.com on 2017/3/31.
 */


val KClass<*>.modelProperties: List<KMutableProperty<*>>
	get() {
		return classPropCache.getOrPut(this) {
			findModelProperties(this)
		}
	}

val KClass<*>.modelPrimaryKeys: List<KMutableProperty<*>>
	get() {
		return this.modelProperties.filter {
			it.isPrimaryKey
		}
	}

private val classPropCache = HashMap<KClass<*>, List<KMutableProperty<*>>>(64)

private fun findModelProperties(cls: KClass<*>): List<KMutableProperty<*>> {
	return cls.memberProperties.filter {
		if (it !is KMutableProperty<*>) {
			false
		} else if (it.isAbstract || it.isConst || it.isLateinit) {
			false
		} else if (!it.isPublic) {
			false
		} else !it.isExcluded
	}.map { it as KMutableProperty<*> }
}

