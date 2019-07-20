package dev.entao.keb.am.model

import dev.entao.kava.base.Label
import dev.entao.kava.base.Name
import dev.entao.kava.sql.*
import java.sql.Date
import java.sql.Time

class ApkVersion : Model() {

	@Label("ID")
	@PrimaryKey
	@AutoInc
	var id: Int  by model

	@Label("APP名称")
	var appName: String by model

	@Label("包名")
	var pkgName: String by model

	@Label("VersionCode")
	var versionCode: Int   by model

	@Label("VersionName")
	var versionName: String   by model

	@Label("介绍")
	var msg: String   by model

	@Label("禁用")
	var disable: Int by model

	@Label("上传者")
	var auth: Int by model

	@Label("资源ID")
	var resId: Int by model

	@Label("上传时间")
	var pub_datetime: Long by model

	@Label("上传日期")
	var pub_date: Date by model
	@Label("上传时间")
	var pub_time: Time by model

	companion object : ModelClass<ApkVersion>() {

		fun last(pkg: String): ApkVersion? {
			return findOne((ApkVersion::disable EQ 0) AND (ApkVersion::pkgName EQ pkg)) {
				desc(ApkVersion::versionCode)
			}
		}

		fun lastN(n: Int = 20): List<ApkVersion> {
			return findAll(ApkVersion::disable EQ 0) {
				limit(n)
				desc(ApkVersion::pub_datetime)
			}
		}

		fun latest(): List<ApkVersion> {
			return findAll().groupBy { it.pkgName }.map {
				it.value.maxBy { it.versionCode }
			}.filterNotNull().sortedBy { it.pkgName }
		}

		fun byPkg(pkg: String): List<ApkVersion> {
			return findAll(ApkVersion::pkgName EQ pkg) {
				desc(ApkVersion::versionCode)
			}
		}

	}
}