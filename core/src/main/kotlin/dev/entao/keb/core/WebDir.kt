package dev.entao.keb.core

import java.io.File
import javax.servlet.FilterConfig

class WebDir {
	//war解压后的目录,  WEB-INF和META-INF所在的目录
	private lateinit var webRootFile: File
	private  var contextPath:String = ""

	@Suppress("UNUSED_PARAMETER")
	fun onConfig(filter: HttpFilter, filterConfig: FilterConfig) {
		webRootFile = File(filterConfig.servletContext.getRealPath("/"))
		this.contextPath = filterConfig.servletContext.contextPath
	}

	private val webParentFile: File get() = webRootFile.parentFile

	val baseDir: File by lazy {
		sureDir("_base")
	}

	val uploadDir: File by lazy {
		sureDir("_files")
	}
	val tmpDir: File by lazy {
		sureDir("_tmp")
	}

	val logDir: File by lazy {
		sureDir("_log")
	}

	private fun sureDir(dirName: String): File {
		return File(webParentFile, contextPath.trim('/') + dirName).apply {
			if (!exists()) {
				mkdir()
			}
		}
	}
}