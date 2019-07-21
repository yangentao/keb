package dev.entao.keb.am

import dev.entao.kava.apk.apkInfo
import dev.entao.kava.base.Hex
import dev.entao.kava.base.Label
import dev.entao.kava.base.firstParamName
import dev.entao.kava.json.YsonObject
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.core.render.FileSender
import dev.entao.keb.page.R
import dev.entao.keb.page.ex.Upload
import dev.entao.keb.page.groups.ResGroup
import dev.entao.keb.page.html.Tag
import dev.entao.keb.page.widget.configUpload
import net.dongliu.apk.parser.ApkFile

@Label("APK")
class ApkGroup(context: HttpContext) : HttpGroup(context) {

	override fun indexAction() {
		context.abort(404)
	}

	fun infoAction(id: Int) {
		val item = Upload.findByKey(id)
		if (item == null) {
			resultSender.failed(-1, "无效的标识")
			return
		}
		val file = item.localFile(context)
		if (!file.exists()) {
			resultSender.failed(-1, "文件已不存在")
			return
		}

		try {
			val info = ApkFile(file).apkInfo
			val yo = YsonObject()
			yo.str("label", info.label)
			yo.str("versionCode", info.versionCode.toString())
			yo.str("versionName", info.versionName)
			yo.str("packageName", info.packageName)
			resultSender.obj(yo)
		} catch (ex: Exception) {
			resultSender.failed(-1, "解析失败")
		}
	}

	fun imageAction(id: Int) {
		val item = Upload.findByKey(id)
		if (item == null) {
			resultSender.failed(-1, "无效的标识")
			return
		}
		val file = item.localFile(context)
		if (!file.exists()) {
			resultSender.failed(-1, "文件已不存在")
			return
		}
		if ("apk" == item.extName.toLowerCase()) {
			try {
				val data = ApkFile(file).appIconMax?.data
				if (data != null) {
					val headData = data.copyOfRange(0, 16)
					val hexS = Hex.encode(headData).toUpperCase()
					val mime2: String? = when {
						"FFD8FF" in hexS -> "image/jpeg"
						"89504E47" in hexS -> "image/png"
						else -> null
					}
					if (mime2 != null) {
						val fs = FileSender(context)
						fs.sendData(data, mime2)
						return
					}
				}
			} catch (ex: Exception) {

			}
		}
		resultSender.failed(-1, "无效文件")
	}

	companion object {

		fun configRes(tag: Tag) {
			if (ResGroup::class in tag.httpContext.filter.routeManager.allGroups) {
				val uploadUri = tag.httpContext.actionUri(ResGroup::uploadAction)
				val viewUri = tag.httpContext.actionUri(ApkGroup::imageAction)
				val viewParam = ApkGroup::imageAction.firstParamName ?: "id"
				val missImg = tag.httpContext.resUri(R.fileImageDefault)
				tag.configUpload(uploadUri, viewUri, viewParam, 30, missImg)
			}
		}
	}

}