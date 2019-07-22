package dev.entao.keb.biz.model

import dev.entao.kava.base.Label
import dev.entao.kava.base.Length
import dev.entao.kava.base.substr
import dev.entao.kava.log.loge
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass
import dev.entao.keb.biz.ex.MaxRows
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.clientIp
import dev.entao.keb.core.headerUserAgent
import dev.entao.keb.core.paramMap

@MaxRows(300_000)
class Ip : Model() {

	@dev.entao.kava.sql.AutoInc
	@dev.entao.kava.sql.PrimaryKey
	@Label("ID")
	var id: Int by model

	@dev.entao.kava.sql.Index
	@Label("IP")
	var ip: String by model

	@Label("Host")
	var host: String by model

	@Label("端口号")
	var port: Int by model

	@dev.entao.kava.sql.Index
	@Label("URI")
	var uri: String by model

	@Label("请求参数")
	@Length(3072)
	var query: String by model

	@Label("请求Agent")
	@Length(1024)
	var agent: String by model

	@dev.entao.kava.sql.Index
	@Label("APP用户ID")
	var appUserId: String by model

	@dev.entao.kava.sql.Index
	@Label("WEB用户ID")
	var accountId: String by model

	@dev.entao.kava.sql.Index
	var reqDate: java.sql.Date by model

	@dev.entao.kava.sql.Index
	var reqTime: java.sql.Time by model

	companion object : ModelClass<Ip>() {
		fun save(context: HttpContext) {
			val req = context.request
			val ip = Ip()
			ip.ip = req.clientIp
			ip.host = req.remoteHost ?: ""
			ip.port = req.remotePort
			ip.uri = req.requestURI
			ip.query = req.queryString
					?: req.paramMap.map { it.key + "=[" + it.value.joinToString(",") + "]" }.joinToString(",")
			if (ip.query.length > 3000) {
				ip.query = ip.query.substr(0, 3000)
			}

			ip.agent = req.headerUserAgent ?: ""
			ip.accountId = context.account
			val d = System.currentTimeMillis()
			ip.reqDate = java.sql.Date(d)
			ip.reqTime = java.sql.Time(d)
			try {
				ip.insert()
			} catch (e: Exception) {
				loge(e)
			}
		}
	}
}