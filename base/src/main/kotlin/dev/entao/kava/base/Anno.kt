package dev.entao.kava.base

import java.text.DecimalFormat
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class NumberFormat(val pattern: String)

//字段长度--字符串
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Length(val value: Int, val msg: String = "")


//是否忽略
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Exclude


//表或字段(属性)的名字
//路由时, controller名字或action名字
@Target(AnnotationTarget.CLASS,
		AnnotationTarget.PROPERTY,
		AnnotationTarget.FIELD,
		AnnotationTarget.FUNCTION,
		AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Name(val value: String)

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Label(val value: String, val desc: String = "")

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultValue(val value: String)


@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HideClient

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class FormDate(val value: String = "yyyy-MM-dd")

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class FormTime(val value: String = "HH:mm:ss")


//字符串长度限制, 也可用于数组或JsonArray
@Target(AnnotationTarget.PROPERTY,
		AnnotationTarget.FIELD,
		AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Trim

//==name

val KProperty<*>.fullName: String
	get() {
		val tabName = this.ownerClass!!.userName
		val fname = this.findAnnotation<Name>()?.value ?: this.name
		return "$tabName.$fname"
	}
val KClass<*>.userName: String
	get() {
		return this.findAnnotation<Name>()?.value ?: this.simpleName!!
	}
val KFunction<*>.userName: String
	get() {
		return this.findAnnotation<Name>()?.value ?: this.name
	}
val KProperty<*>.userName: String
	get() {
		return this.findAnnotation<Name>()?.value ?: this.name
	}

val KParameter.userName: String
	get() {
		return this.findAnnotation<Name>()?.value ?: this.name ?: throw IllegalStateException("参数没有名字")
	}

//label
val KClass<*>.userLabel: String
	get() {
		return this.findAnnotation<Label>()?.value ?: this.userName
	}

val KClass<*>.userDesc: String
	get() {
		val lb = this.findAnnotation<Label>()
		if (lb != null) {
			if (lb.desc.isNotEmpty()) {
				return lb.desc
			}
			if (lb.value.isNotEmpty()) {
				return lb.value
			}
		}
		return this.userName
	}

val KFunction<*>.userLabel: String
	get() {
		return this.findAnnotation<Label>()?.value ?: this.userName
	}
val KFunction<*>.userDesc: String
	get() {
		val lb = this.findAnnotation<Label>()
		if (lb != null) {
			if (lb.desc.isNotEmpty()) {
				return lb.desc
			}
			if (lb.value.isNotEmpty()) {
				return lb.value
			}
		}
		return this.userName
	}
val KProperty<*>.labelOnly: String?
	get() {
		return this.findAnnotation<Label>()?.value
	}
val KProperty<*>.userLabel: String
	get() {
		return this.findAnnotation<Label>()?.value ?: this.userName
	}


//==default value

val KProperty<*>.defaultValue: String?
	get() {
		return this.findAnnotation<DefaultValue>()?.value
	}

val KProperty<*>.isHideClient: Boolean
	get() {
		return this.hasAnnotation<HideClient>()
	}

inline fun <reified T : Annotation> KAnnotatedElement.hasAnnotation(): Boolean = null != this.findAnnotation<T>()


//12345.format(",###.##")
//12345.6789.format("0,000.00")
fun Number.format(pattern: String): String {
	return if (pattern.isEmpty()) {
		this.toString()
	} else {
		DecimalFormat(pattern).format(this)
	}
}