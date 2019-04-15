package dev.entao.kava.apk

import dev.entao.kava.json.YsonObject
import dev.entao.kava.json.ysonObject
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.util.*

/**
 * Created by entaoyang@163.com on 2017/9/28.
 */

//net.dongliu:apk-parser:2.2.0
class ApkInfo {
	var label: String = ""
	var packageName: String = ""
	var versionCode: Long = 0
	var versionName: String = ""
	var minSdkVersion: String = ""
	var maxSdkVersion: String? = null


	companion object {
		//			val f = File("/Users/yangentao/Downloads/mcall-39200.apk")
		fun from(file: File): ApkInfo? {
			try {
				val apk = ApkFile(file)
				apk.preferredLocale = Locale.SIMPLIFIED_CHINESE
				val meta = apk.apkMeta
				val info = ApkInfo()
				info.label = meta.label
				info.packageName = meta.packageName
				info.versionCode = meta.versionCode
				info.versionName = meta.versionName
				info.minSdkVersion = meta.minSdkVersion
				info.maxSdkVersion = meta.maxSdkVersion
				apk.close()
				return info
			} catch (ex: Exception) {
				ex.printStackTrace()
			}
			return null
		}

		fun fromFileToJsonObject(file: File): YsonObject? {
			val info = from(file)
			if (info != null) {
				if (info.packageName.isNotEmpty() && info.versionName.isNotEmpty()) {
					return ysonObject {
						"label" to info.label
						"packageName" to info.packageName
						"versionCode" to info.versionCode
						"versionName" to info.versionName
						"minSdkVersion" to info.minSdkVersion
						"maxSdkVersion" to info.maxSdkVersion
					}
				}
			}
			return null
		}
	}
}