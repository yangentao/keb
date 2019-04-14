@file:Suppress("unused")

package dev.entao.ken.ex.pages

import dev.entao.kbase.Hex
import dev.entao.kbase.Mimes
import dev.entao.ken.FileSender
import dev.entao.ken.HttpContext
import dev.entao.ken.HttpPage
import dev.entao.ken.anno.HttpMethod
import dev.entao.ken.anno.LoginWeb
import dev.entao.ken.filePart
import dev.entao.sql.EQ
import net.dongliu.apk.parser.ApkFile
import dev.entao.ken.utils.ApkInfo
import dev.entao.ken.ex.model.Upload
import javax.servlet.http.Part

/**
 * Created by entaoyang@163.com on 2017/4/14.
 */

class FilesPage(context: HttpContext) : HttpPage(context) {

	fun apkInfoAction(id: Int) {
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

		val info = ApkInfo.fromFileToJsonObject(file)
		if (info != null) {
			resultSender.obj(info)
			return
		}
		resultSender.failed(-1, "解析失败")
	}

	//上传一个文件
	@LoginWeb
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
		val fs = FileSender(context)
		fs.contentType(Mimes.ofFile(item.rawname))
		if (isMedia) {
			fs.media(file, item.rawname)
		} else {
			fs.attach(file, item.rawname)
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
			fs.contentType(mime)
			fs.media(file, item.rawname)
			return
		}
		if ("apk" in item.extName.toLowerCase()) {
			val apkFile = ApkFile(file)
			val data = apkFile.iconFile.data
			val headData = data.copyOfRange(0, 16)
			val hexS = Hex.encode(headData).toUpperCase()
			val mime2: String? = when {
				"FFD8FF" in hexS -> "image/jpeg"
				"89504E47" in hexS -> "image/png"
				else -> null
			}
			if (mime2 != null) {
				val fs = FileSender(context)
				fs.sendData(mime2, data)
				return
			}
		}
		context.abort(404, "文件已不存在")
	}

}