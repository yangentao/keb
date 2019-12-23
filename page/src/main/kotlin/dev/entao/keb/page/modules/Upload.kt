package dev.entao.keb.page.modules

import dev.entao.kava.base.*
import dev.entao.kava.sql.*
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.account.account
import dev.entao.keb.core.account.tokenUserId
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
	@Index
	var userId: String by model
	@Index
	var accountId: String by model
	@Index
	var uploadTime: TimestampSQL by model

	var platform: String by model

	fun localFile(context: HttpContext): File {
		return if (subdir.isEmpty()) {
			File(context.uploadDir, localFileName)
		} else {
			File(File(context.uploadDir, subdir), localFileName)
		}
	}

	companion object : ModelClass<Upload>() {
		const val SUBDIR = "subdir"
		const val PLATFORM = "platform"

		fun checkSubDirParam(d: String): String {
			val sb = StringBuilder()
			for (c in d) {
				if (c.isLetterOrDigit() || c == '_') {
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
			val m = Upload()
			m.localFileName = if (ext.isEmpty()) {
				uuid
			} else {
				"$uuid.$ext"
			}

			m.extName = ext
			m.dir = context.uploadDir.absolutePath
			m.contentType = part.contentType
			m.rawname = part.submittedFileName
			m.size = part.size.toInt()
			m.accountId = context.account
			m.userId = context.tokenUserId?.toString() ?: ""
			m.uploadTime = TimeMill.asTimestamp
			m.platform = context.httpParams.str(PLATFORM) ?: context.httpParams.str("os") ?: ""
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
