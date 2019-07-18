package dev.entao.keb.core


/**
 * Created by entaoyang@163.com on 2017/6/10.
 */

//需要登录后请求, 管理员权限
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpMethod(vararg val value: String)



@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class IndexAction(vararg val value: String)

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LengthRange(val minValue: Int, val maxValue: Int, val msg: String = "")

//@ColumnWidth("50px");,
//@ColumnWidth("30%");,
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ColumnWidth(val value: String)

