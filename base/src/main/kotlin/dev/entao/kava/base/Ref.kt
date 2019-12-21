package dev.entao.kava.base

import kotlin.jvm.internal.CallableReference
import kotlin.jvm.internal.FunctionReference
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType

fun KType.isClass(kcls: KClass<*>): Boolean {
	return this.classifier == kcls
}

val KType.isTypeString: Boolean get() = this.isClass(String::class)
val KType.isTypeInt: Boolean get() = this.isClass(Int::class) || this.isClass(java.lang.Integer::class)
val KType.isTypeLong: Boolean get() = this.isClass(Long::class) || this.isClass(java.lang.Long::class)
val KType.isTypeByte: Boolean get() = this.isClass(Byte::class) || this.isClass(java.lang.Byte::class)
val KType.isTypeShort: Boolean get() = this.isClass(Short::class) || this.isClass(java.lang.Short::class)
val KType.isTypeChar: Boolean get() = this.isClass(Char::class) || this.isClass(java.lang.Character::class)
val KType.isTypeBoolean: Boolean get() = this.isClass(Boolean::class) || this.isClass(java.lang.Boolean::class)
val KType.isTypeFloat: Boolean get() = this.isClass(Float::class) || this.isClass(java.lang.Float::class)
val KType.isTypeDouble: Boolean get() = this.isClass(Double::class) || this.isClass(java.lang.Double::class)
val KType.isTypeByteArray: Boolean get() = this.isClass(ByteArray::class)

val KType.genericArgs: List<KTypeProjection> get() = this.arguments.filter { it.variance == KVariance.INVARIANT }
val KType.isGeneric: Boolean get() = this.arguments.isNotEmpty()

/*
* class A(){
*       fun fa(){}
* }
*
* class B: A(){
*
* }
*
* A::fa.ownerClass => A::class
* A()::fa.ownerClass => A::class
* B::fa.ownerClass => B::class
* B()::fa.ownerClass => B::class
 */
val KFunction<*>.ownerClass: KClass<*>?
	get() {
		if (this is FunctionReference) {
			if (this.boundReceiver != CallableReference.NO_RECEIVER) {
				return this.boundReceiver::class
			}
			val c = this.owner as? KClass<*>
			if (c != null) {
				return c
			}
		} else {
			return this.javaMethod?.declaringClass?.kotlin
		}
		return null
	}
val KFunction<*>.ownerObject: Any?
	get() {
		if (this is FunctionReference) {
			if (this.boundReceiver != CallableReference.NO_RECEIVER) {
				return this.boundReceiver
			}
		}
		return null
	}

val KFunction<*>.firstParamName: String?
	get() {
		return this.valueParameters.firstOrNull()?.userName
	}

val KFunction<*>.secondParamName: String?
	get() {
		return this.valueParameters.secondOrNull()?.userName
	}

val KProperty<*>.ownerClass: KClass<*>?
	get() {
		if (this is CallableReference) {
			if (this.boundReceiver != CallableReference.NO_RECEIVER) {
				return this.boundReceiver::class
			}
			val c = this.owner as? KClass<*>
			if (c != null) {
				return c
			}
		} else {
			return this.javaField?.declaringClass?.kotlin
		}

		return null
	}

val KProperty<*>.ownerObject: Any?
	get() {
		if (this is CallableReference) {
			if (this.boundReceiver != CallableReference.NO_RECEIVER) {
				return this.boundReceiver::class
			}
		}
		return null
	}

val KProperty<*>.firstGenericType: KClass<*>? get() = this.returnType.arguments.firstOrNull()?.type?.classifier as? KClass<*>

val KProperty<*>.isTypeString: Boolean get() = this.returnType.isTypeString
val KProperty<*>.isTypeInt: Boolean get() = this.returnType.isTypeInt
val KProperty<*>.isTypeLong: Boolean get() = this.returnType.isTypeLong
val KProperty<*>.isTypeByte: Boolean get() = this.returnType.isTypeByte
val KProperty<*>.isTypeShort: Boolean get() = this.returnType.isTypeShort
val KProperty<*>.isTypeChar: Boolean get() = this.returnType.isTypeChar
val KProperty<*>.isTypeBoolean: Boolean get() = this.returnType.isTypeBoolean
val KProperty<*>.isTypeFloat: Boolean get() = this.returnType.isTypeFloat
val KProperty<*>.isTypeDouble: Boolean get() = this.returnType.isTypeDouble
val KProperty<*>.isTypeByteArray: Boolean get() = this.returnType.isTypeByteArray
val KProperty<*>.isTypeHashSet: Boolean get() = this.returnType.isClass(HashSet::class)
val KProperty<*>.isTypeArrayList: Boolean get() = this.returnType.isClass(ArrayList::class)

fun KProperty<*>.isTypeClass(cls: KClass<*>): Boolean {
	return this.returnType.javaType == cls.java
}

val KProperty<*>.isTypeEnum: Boolean
	get() {
		return this.javaField?.type?.isEnum ?: false
	}

val KProperty<*>.strValue: String?
	get() {
		return this.getValue()?.toString()
	}

fun KProperty<*>.getValue(): Any? {
	if (this.getter.parameters.isEmpty()) {
		return this.getter.call()
	}
	return null
}

fun KProperty<*>.getValue(inst: Any): Any? {
	if (this.getter.parameters.isEmpty()) {
		return this.getter.call()
	}
	return this.getter.call(inst)
}

fun KProperty<*>.getBindValue(): Any? {
	if (this.getter.parameters.isEmpty()) {
		return this.getter.call()
	}
	return null
}

fun KMutableProperty<*>.setValue(inst: Any, value: Any?) {
	try {
		this.setter.call(inst, value)
	} catch (t: Throwable) {
		println("setValue Error: " + this.userName + "." + this.name + ", value=" + value?.toString())
		t.printStackTrace()
		throw  t
	}
}

val KProperty<*>.isPublic: Boolean get() = this.visibility == KVisibility.PUBLIC

@Suppress("UNCHECKED_CAST")
fun <V> strToV(v: String, property: KProperty<*>): V {
	val retType = property.returnType
	if (retType.isTypeString) {
		return v as V
	}
	val fd = property.findAnnotation<FormDate>()
	if (fd != null && retType.classifier == Long::class) {
		if (v.isEmpty()) {
			return 0L as V
		}
		val lv: Long = MyDate.parse(fd.value, v)?.time ?: 0L
		return lv as V
	}
	val c = TextConverts[retType.classifier]
	if (c != null) {
		return c.fromText(v) as V
	}


	throw IllegalArgumentException("不支持的类型${property.fullName}")
}

@Suppress("UNCHECKED_CAST")
fun <V> defaultValueOfProperty(p: KProperty<*>): V {
	val retType = p.returnType

	val c = TextConverts[retType.classifier]
	if (c != null) {
		return c.defaultValue as V
	}

	throw IllegalArgumentException("不支持的类型modelMap: ${retType.classifier}, ${p.fullName}")
}
//
//class Hello {
//	fun ff() {
//		println("Hello.ff")
//	}
//}
//
//fun testFF() {
//	logd("testff")
//}
//
//fun main() {
//	val h = Hello()
//	val f1 = h::ff
//	val m = Hello::class.java.methods.first { it.name == "ff" }
//	if (m != null) {
//		logd(m.name)
//		val km = m.kotlinFunction
//		logd(km?.name)
//	}
//
//	val c = ::testFF
//	logd(c.ownerClass)
//	c.call()
//
//}
