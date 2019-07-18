@file:Suppress("unused")

package dev.entao.keb.page

import dev.entao.kava.base.Mimes
import dev.entao.kava.base.Thumb
import dev.entao.kava.sql.EQ
import dev.entao.keb.core.HttpMethod
import dev.entao.keb.core.filePart
import dev.entao.keb.core.render.FileSender
import dev.entao.keb.core.render.ResultRender
import dev.entao.keb.page.ex.Upload
import java.io.File
import javax.servlet.http.Part

/**
 * Created by entaoyang@163.com on 2017/4/14.
 */

class FilesApi(context: dev.entao.keb.core.HttpContext) : dev.entao.keb.core.HttpGroup(context) {

	private val resultSender = ResultRender(context)
//	fun apkInfoAction(id: Int) {
//		val item = Upload.findByKey(id)
//		if (item == null) {
//			resultSender.failed(-1, "无效的标识")
//			return
//		}
//		val file = item.localFile(context)
//		if (!file.exists()) {
//			resultSender.failed(-1, "文件已不存在")
//			return
//		}
//
//		val info = ApkInfo.fromFileToJsonObject(file)
//		if (info != null) {
//			resultSender.obj(info)
//			return
//		}
//		resultSender.failed(-1, "解析失败")
//	}

	//上传一个文件
	@HttpMethod("POST")
	fun uploadAction() {
		val part: Part? = request.filePart
		if (part == null) {
			context.abort(400, "没有file part")
			return
		}

		val m = Upload.fromContext(context, part)
		val file = m.localFile(context)

		try {
			part.write(file.absolutePath)
		} catch (ex: Exception) {
			file.delete()
			resultSender.failed("写文件失败")
			return
		} finally {
			part.delete()
		}

		if (m.insert()) {
			resultSender.int(m.id)
		} else {
			resultSender.failed("保存失败")
		}
	}

	fun downloadAction(id: Int) {
		sendFile(id, false)
	}

	fun mediaAction(id: Int) {
		sendFile(id, true)
	}

	private fun sendFile(id: Int, isMedia: Boolean) {
		val item = Upload.findOne(Upload::id EQ id)
		if (item == null) {
			resultSender.failed(-1, "无效的标识")
			return
		}
		val file = item.localFile(context)
		if (!file.exists()) {
			resultSender.failed(-1, "文件已不存在")
			return
		}
		val maxWidth = httpParams.int("w") ?: 0
		if (maxWidth > 0) {
			val wFile = File(context.tmpDir, item.localFileName + "_w$maxWidth")
			if (!wFile.exists()) {
				Thumb(file).maxWidth(maxWidth).toFile(wFile)
			}
			if (wFile.exists()) {
				sendFile(wFile, item.rawname, isMedia)
				return
			}
		}

		sendFile(file, item.rawname, isMedia)
		return
	}

	private fun sendFile(file: File, rawName: String, isMedia: Boolean) {
		val fs = FileSender(context)
		if (isMedia) {
			fs.media(file, Mimes.ofFile(rawName))
		} else {
			fs.attach(file, Mimes.ofFile(rawName), rawName)
		}
	}

	fun imgAction(id: Int) {
		val item = Upload.findOne(Upload::id EQ id)
		if (item == null) {
			context.abort(404, "无效的标识")
			return
		}
		val file = item.localFile(context)
		if (!file.exists()) {
			context.abort(404, "文件已不存在")
			return
		}
		val mime = Mimes.ofFile(item.rawname)
		if ("image" in mime) {
			val fs = FileSender(context)
			fs.media(file, mime)
			return
		}
//		if ("apk" in item.extName.toLowerCase()) {
//			val data = MyApkFile.iconData(file)
//			val headData = data.copyOfRange(0, 16)
//			val hexS = Hex.encode(headData).toUpperCase()
//			val mime2: String? = when {
//				"FFD8FF" in hexS -> "image/jpeg"
//				"89504E47" in hexS -> "image/png"
//				else -> null
//			}
//			if (mime2 != null) {
//				val fs = FileSender(context)
//				fs.sendData(data, mime2)
//				return
//			}
//		}
		context.abort(404, "文件已不存在")
	}

	companion object {
		fun urlOf(context: dev.entao.keb.core.HttpContext, id: Int): String {
			return context.path.action(FilesApi::downloadAction).param(id).fullUrlOf(context.request)
		}

		fun removeRes(context: dev.entao.keb.core.HttpContext, resId: Int) {
			val item = Upload.findOne(Upload::id EQ resId) ?: return
			val file = item.localFile(context)
			file.delete()
			Upload.delete(Upload::id EQ resId)
		}
	}
}