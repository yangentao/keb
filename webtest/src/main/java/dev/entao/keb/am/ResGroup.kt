@file:Suppress("unused")

package dev.entao.keb.am

import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.page.ex.Upload
import dev.entao.kava.base.Mimes
import dev.entao.kava.base.firstParamName
import dev.entao.kava.sql.EQ
import dev.entao.keb.core.HttpMethod
import dev.entao.keb.core.render.FileSender
import dev.entao.keb.core.render.ResultRender
import dev.entao.keb.page.FilesPage
import dev.entao.keb.page.R
import dev.entao.keb.page.html.Tag
import dev.entao.keb.page.widget.configUpload
import javax.servlet.http.Part

/**
 * Created by entaoyang@163.com on 2017/4/14.
 */

class ResGroup(context: HttpContext) : HttpGroup(context) {



	override fun indexAction() {
		context.abort(404)
	}

	//上传一个文件
	@HttpMethod("POST")
	fun uploadAction() {
		val part: Part? = context.firstFilePart
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
			context.abort(404, "无效的标识")
//			resultSender.failed(-1, "无效的标识")
			return
		}
		val file = item.localFile(context)
		if (!file.exists()) {
			context.abort(404, "无效的标识")
//			resultSender.failed(-1, "文件已不存在")
			return
		}
		val fs = FileSender(context)
		if (isMedia) {
			fs.media(file, Mimes.ofFile(item.rawname))
		} else {
			fs.attach(file, Mimes.ofFile(item.rawname), item.rawname)
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
			fs.media(file, Mimes.ofFile(item.rawname))
			return
		}

		context.abort(404, "文件已不存在")
	}

	companion object {

		fun configRes(tag: Tag) {
			if (ResGroup::class in tag.httpContext.filter.routeManager.allGroups) {
				val uploadUri = tag.httpContext.actionUri(ResGroup::uploadAction)
				val viewUri = tag.httpContext.actionUri(ResGroup::imgAction)
				val viewParam = ResGroup::imgAction.firstParamName ?: "id"
				val missImg = tag.httpContext.resUri(R.fileImageDefault)
				tag.configUpload(uploadUri, viewUri, viewParam, 30, missImg)
			}
		}
	}

}