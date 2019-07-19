package dev.entao.keb.page

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


//@ColumnWidth("50px");,
//@ColumnWidth("30%");,
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ColumnWidth(val value: String)