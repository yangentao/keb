package dev.entao.kage.ex

import dev.entao.kava.base.DefaultValue
import dev.entao.kava.base.Name
import dev.entao.sql.AutoInc
import dev.entao.sql.PrimaryKey
import dev.entao.ken.HttpContext
import dev.entao.sql.Model
import dev.entao.sql.ModelClass
import java.io.File
import java.util.*
import javax.servlet.http.Part

/**
 * Created by entaoyang@163.com on 2017/4/5.
 */

@Name("upload")
class Upload : Model() {

	@PrimaryKey
	@AutoInc
	var id: Int by model
	var localFileName: String by model
	var extName: String by model
	var dir: String by model
	@DefaultValue("")
	var subdir: String by model
	var rawname: String by model
	var size: Int by model
	var contentType: String by model
	var userId: String by model
	var accountId: String by model
	var uploadTime: Long by model

	fun localFile(context: HttpContext): File {
		if (subdir.isEmpty()) {
			return File(context.uploadDir, localFileName)
		} else {
			return File(File(context.uploadDir, subdir), localFileName)
		}
	}

	companion object : ModelClass<Upload>() {
		const val SUBDIR = "subdir"

		fun checkSubDirParam(d: String): String {
			val sb = StringBuilder()
			for (c in d) {
				if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') {
					sb.append(c)
				} else {
					sb.append("_")
				}
			}
			return sb.toString()
		}

		fun fromContext(context: HttpContext, part: Part): Upload {
			val uuid = UUID.randomUUID().toString()
			val ext = part.submittedFileName.substringAfterLast('.', "")
			val localfilename = if (ext.isEmpty()) {
				uuid
			} else {
				"$uuid.$ext"
			}
			val m = Upload()
			m.localFileName = localfilename
			m.extName = ext
			m.dir = context.uploadDir.absolutePath
			m.contentType = part.contentType
			m.rawname = part.submittedFileName
			m.size = part.size.toInt()
			m.userId = context.userId.toString()
			m.uploadTime = System.currentTimeMillis()
			m.subdir = checkSubDirParam(context.httpParams.str(SUBDIR)?.trim()
					?: "")
			if (m.subdir.isNotEmpty()) {
				val subF = File(context.uploadDir, m.subdir)
				if (!subF.exists()) {
					subF.mkdirs()
				}
			}
			return m
		}
	}
}
