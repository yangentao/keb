package dev.entao.keb.biz.ex

import dev.entao.kava.base.*
import dev.entao.kava.log.loge
import dev.entao.kava.sql.*
import dev.entao.keb.biz.model.Account
import dev.entao.keb.biz.model.ResAccess
import dev.entao.keb.biz.model.TokenTable
import dev.entao.keb.core.*
import dev.entao.keb.core.render.ResultRender
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

//限定表的最多行数, 必须有自增的id主键
//每小时删除旧数据
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MaxRows(val value: Int)

class DefaultPermAcceptor : PermAcceptor {
	private val accessMap = HashMap<String, Int>()

	override fun prepare(context: HttpContext) {
		val aid = context.accountId
		if (aid < 0) {
			return
		}
		val ac = Account.findByKey(aid)
		if (ac != null) {
			ResAccess.findAll(ResAccess::objId to ac.deptId, ResAccess::objType to ResAccess.TDept).forEach {
				accessMap[it.uri] = it.judge
			}
			ResAccess.findAll(ResAccess::objId to ac.id, ResAccess::objType to ResAccess.TAccount).forEach {
				if (it.judge == ResAccess.Allow) {
					accessMap[it.uri] = it.judge
				}
			}

		}
	}

	override fun accept(context: HttpContext, uri: String): Boolean {
		if (!context.filter.webConfig.allowResAccess) {
			return true
		}
		val uu = uri.substringBefore('?')
		if (context.accountId == 0) {
			return true
		}
		val n = accessMap[uu] ?: return true
		return n == ResAccess.Allow

	}

}

//限制表的记录行数
//TableLimitTimer(Ip::class, 10000)  限制Ip表10000行记录, 每小时删除一次旧数据
//TableLimitTimer(Ip::class)  函数有MaxRows注释决定
class TableLimitTimer(private val cls: KClass<out Model>, limitValue: Int = 0) : HttpTimer {

	private val maxRow: Int = if (limitValue > 0) {
		limitValue
	} else {
		cls.findAnnotation<MaxRows>()?.value ?: 0
	}
	private val pk: Prop1?

	init {
		pk = prepare()
	}

	private fun prepare(): Prop1? {
		if (maxRow <= 0) {
			return null
		}
		val ks = cls.modelPrimaryKeys
		if (ks.size != 1) {
			loge("必须是唯一整形自增主键")
			return null
		}
		val pk = ks.first()
		if (!(pk.hasAnnotation<PrimaryKey>() && pk.hasAnnotation<AutoInc>())
				|| !(pk.isTypeLong || pk.isTypeInt)) {
			loge("必须是整形自增主键")
			return null
		}
		return pk as Prop1

	}

	override fun onHour(h: Int) {
		if (pk != null) {
			this.limitTable(cls, pk, maxRow)
		}
	}

	private fun limitTable(cls: KClass<out Model>, pk: Prop1, maxRow: Int) {
		if (maxRow <= 0) {
			return
		}
		val c = ConnLook.named(cls)
		val r = c.query {
			select(pk)
			from(cls)
			desc(pk)
			limit(1, maxRow)
		}

		when {
			pk.isTypeInt -> {
				val n = r.intValue ?: return
				c.delete(cls, pk LT n)
			}
			pk.isTypeLong -> {
				val n = r.longValue ?: return
				c.delete(cls, pk LT n)
			}
			else -> r.closeSafe()
		}

	}

}

//需要登录后请求
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginApp

//需要登录后请求
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginWeb

object AuthAppAcceptor : HttpSlice {
	override fun beforeService(context: HttpContext, router: Router): Boolean {
		context.os = context.httpParams.str("os") ?: ""
		context.token = context.httpParams.str(HttpContext.TOKEN) ?: ""
		val needLoginApp: Boolean = router.function.hasAnnotation<LoginApp>() || router.cls.hasAnnotation<LoginApp>()
		if (context.token.isNotEmpty()) {
			val t = TokenTable.findOne((TokenTable::token EQ context.token) AND (TokenTable::type EQ context.os))
			if (t != null && !t.isExpired) {
				context.userId = t.userId.toInt()
			}
		}
		if (needLoginApp && context.userId == 0) {
			ResultRender(context).failed("未登录")
			return false
		}
		return true
	}
}

object AuthWebAcceptor : HttpSlice {
	override fun beforeService(context: HttpContext, router: Router): Boolean {
		context.accountId = context.getSession(HttpContext.ACCOUNT_ID)?.toIntOrNull() ?: 0

		val needLoginWeb: Boolean = router.function.hasAnnotation<LoginWeb>() || router.cls.hasAnnotation<LoginWeb>()

		if (needLoginWeb && context.accountId == 0) {
			val url = Url(context.filter.webConfig.loginUri)
			val q = context.request.queryString
			val burl = if (q != null && q.isNotEmpty()) {
				context.request.requestURL.toString() + "?" + q
			} else {
				context.request.requestURL.toString()
			}
			url.replace(Keb.BACK_URL, burl.base64Encoded)
			context.redirect(url.build())
			return false
		}

		return true
	}
}


