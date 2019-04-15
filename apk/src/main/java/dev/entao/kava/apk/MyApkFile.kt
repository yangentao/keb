package dev.entao.kava.apk

import net.dongliu.apk.parser.ApkFile
import java.io.File

object MyApkFile {

	fun fromFile(file: File): ApkFile {
		return ApkFile(file)
	}

	fun iconData(file: File): ByteArray {
		val apkFile = fromFile(file)
		return apkFile.iconFile.data
	}
}