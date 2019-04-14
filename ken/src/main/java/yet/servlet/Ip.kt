package yet.servlet

import dev.entao.kbase.*
import dev.entao.ken.HttpContext
import dev.entao.ken.anno.*
import dev.entao.ken.ex.clientIp
import dev.entao.ken.ex.headerUserAgent
import dev.entao.ken.ex.paramMap
import dev.entao.sql.AutoInc
import dev.entao.sql.Index
import dev.entao.sql.Length
import dev.entao.sql.Model
import dev.entao.sql.ModelClass
import dev.entao.sql.PrimaryKey
import dev.entao.yog.loge

@MaxRows(300_000)
class Ip : Model() {

	@AutoInc
	@PrimaryKey
	@Label("ID")
	var id: Int by model

	@Index
	@Label("IP")
	var ip: String by model

	@Label("Host")
	var host: String by model

	@Label("端口号")
	var port: Int by model

	@Index
	@Label("URI")
	var uri: String by model

	@Label("请求参数")
	@Length(3072)
	var query: String by model

	@Label("请求Agent")
	@Length(1024)
	var agent: String by model

	@Index
	@Label("APP用户ID")
	var appUserId: String by model

	@Index
	@Label("WEB用户ID")
	var accountId: String by model

	@Index
	var reqDate: java.sql.Date by model

	@Index
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
			ip.appUserId = context.userId.toString()
			ip.accountId = context.accountId.toString()
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