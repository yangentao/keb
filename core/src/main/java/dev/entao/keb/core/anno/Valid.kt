package dev.entao.keb.core.anno

/**
 * 参数检查
 * Created by entaoyang@163.com on 2017/4/15.
 */

//step属性
@Target(AnnotationTarget.PROPERTY,
		AnnotationTarget.FIELD,
		AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class StepValue(val value: String)

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

