package dev.entao.kava.apk

import net.dongliu.apk.parser.ApkFile

val ApkFile.apkInfo: ApkInfo
    get() {
        val info = ApkInfo()
        val meta = this.apkMeta ?: return info
        info.label = meta.label ?: ""
        info.packageName = meta.packageName ?: ""
        info.versionCode = meta.versionCode ?: 0
        info.versionName = meta.versionName ?: ""
        info.minSdkVersion = meta.minSdkVersion ?: ""
        info.maxSdkVersion = meta.maxSdkVersion ?: ""
        return info
    }