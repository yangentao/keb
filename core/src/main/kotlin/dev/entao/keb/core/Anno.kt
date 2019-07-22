package dev.entao.keb.core

import dev.entao.kava.base.hasAnnotation
import dev.entao.kava.base.ownerClass

/**
 * 通用注释
 * Created by yangentao on 2016/12/14.
 */

//需要登录后请求
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NeedLogin

//登录的uri
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginAction

//登录的uri
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogoutAction

//参数或属性的最小值Int
@Target(AnnotationTarget.PROPERTY,
		AnnotationTarget.FIELD,
		AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class MinValue(val value: String, val msg: String = "")

//参数或属性的最大值Int
@Target(AnnotationTarget.PROPERTY,
		AnnotationTarget.FIELD,
		AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class MaxValue(val value: String, val msg: String = "")

//参数或属性的最大值Int
@Target(AnnotationTarget.PROPERTY,
		AnnotationTarget.FIELD,
		AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValueRange(val minVal: String, val maxVal: String, val msg: String = "")

//字符串非空, 也可以用于集合
@Target(AnnotationTarget.PROPERTY,
		AnnotationTarget.FIELD,
		AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotEmpty

//trim后的字符串非空
@Target(AnnotationTarget.PROPERTY,
		AnnotationTarget.FIELD,
		AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotBlank

//字符串长度限制, 也可用于数组或JsonArray
@Target(AnnotationTarget.PROPERTY,
		AnnotationTarget.FIELD,
		AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Match(val value: String, val msg: String = "")

//需要登录后请求, 管理员权限
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpMethod(vararg val value: String)

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LengthRange(val minValue: Int, val maxValue: Int, val msg: String = "")

val HttpAction.isNeedLogin: Boolean
	get() {
		return this.hasAnnotation<NeedLogin>() || this.ownerClass!!.hasAnnotation<NeedLogin>()
	}

//需要token验证
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NeedToken

val HttpAction.isNeedToken: Boolean
	get() {
		return this.hasAnnotation<NeedToken>() || this.ownerClass!!.hasAnnotation<NeedToken>()
	}
