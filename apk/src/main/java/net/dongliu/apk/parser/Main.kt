package net.dongliu.apk.parser

import java.io.File
import java.io.IOException

/**
 * Main method for parser apk
 *
 * @author Liu Dong &lt;dongliu@live.cn&gt;
 */
object Main {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val dir = File("/Users/entaoyang/Downloads/")
        dir.listFiles().filter { it.name.endsWith(".png") }.forEach {
            it.delete()
        }
        val f = File(dir, "a.apk")
        println(f.exists())
        val apkFile = ApkFile(f)
        println(apkFile.apkMeta)
        println(apkFile.manifestXml)
        val icon = apkFile.appIcon192 ?: return
        val fd = File(dir, "192.png")
        fd.writeBytes(icon.data!!)

//        for (ic in apkFile.appIcons) {
//            println(ic.density)
//            println(ic.path)
//            val fd = File(dir, ic.density.toString() + ".png")
//            if (ic.data != null) {
//                fd.writeBytes(ic.data!!)
//            }
//        }

    }
}
