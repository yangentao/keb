@file:Suppress("PrivatePropertyName", "MemberVisibilityCanBePrivate", "unused")

package net.dongliu.apk.parser

import net.dongliu.apk.parser.bean.*
import net.dongliu.apk.parser.exception.ParserException
import net.dongliu.apk.parser.parser.*
import net.dongliu.apk.parser.struct.resource.ResourceTable
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.zip.ZipFile

class ApkFile(private val apkFile: File, private val preferLocale: Locale = Locale.SIMPLIFIED_CHINESE) {
    private val RES_FILE = "resources.arsc"
    private var MANIFEST_FILE = "AndroidManifest.xml"

    private var resourceTable: ResourceTable = ResourceTable()
    private var locales: Set<Locale> = emptySet()
    var iconPaths: List<IconPath> = emptyList()

    var manifestXml: String? = null
    var apkMeta: ApkMeta? = null

    init {
        val resData = fileData(RES_FILE)
        if (resData != null) {
            val buffer = ByteBuffer.wrap(resData)
            val rp = ResourceTableParser(buffer)
            rp.parse()
            this.resourceTable = rp.resourceTable
            this.locales = rp.locales
        }
        val xmlTranslator = XmlTranslator()
        val apkTranslator = ApkMetaTranslator(this.resourceTable, this.preferLocale)
        val xmlStreamer = CompositeXmlStreamer(xmlTranslator, apkTranslator)

        val data = fileData(MANIFEST_FILE) ?: throw ParserException("Manifest file not found")
        transBinaryXml(data, xmlStreamer)
        this.manifestXml = xmlTranslator.xml
        this.apkMeta = apkTranslator.apkMeta
        this.iconPaths = apkTranslator.iconPaths
    }


    fun fileData(path: String): ByteArray? {
        ZipFile(apkFile).use { zf ->
            val entry = zf.getEntry(path) ?: return null
            zf.getInputStream(entry).use {
                return it.readBytes()
            }
        }
    }


    /**
     * This method return icons specified in android manifest file, application.
     * The icons could be file icon, color icon, or adaptive icon, etc.
     *
     * @return icon files.
     */
    // adaptive icon?
    val allIcons: List<IconFace>
        @Throws(IOException::class)
        get() {
            val iconPaths = this.iconPaths
            if (iconPaths.isEmpty()) {
                return emptyList()
            }
            val iconFaces = ArrayList<IconFace>(iconPaths.size)
            for (iconPath in iconPaths) {
                val filePath = iconPath.path
                if (filePath.endsWith(".xml")) {
//                    val data = fileData(filePath) ?: continue
//                    val iconParser = AdaptiveIconParser()
//                    transBinaryXml(data, iconParser)
//                    var backgroundIcon: Icon? = null
//                    if (iconParser.background != null) {
//                        backgroundIcon = fileIcon(iconParser.background, iconPath.density)
//                    }
//                    var foregroundIcon: Icon? = null
//                    if (iconParser.foreground != null) {
//                        foregroundIcon = fileIcon(iconParser.foreground, iconPath.density)
//                    }
//                    val icon = AdaptiveIcon(foregroundIcon, backgroundIcon)
//                    iconFaces.add(icon)
                } else {
                    val icon = fileIcon(filePath, iconPath.density)
                    iconFaces.add(icon)
                }
            }
            return iconFaces
        }

    val appIcon72: Icon?
        get() {
            val ip = this.iconPaths.find { it.density == 240 } ?: return null
            return fileIcon(ip.path, ip.density)
        }
    val appIcon96: Icon?
        get() {
            val ip = this.iconPaths.find { it.density == 320 } ?: return null
            return fileIcon(ip.path, ip.density)
        }
    val appIcon144: Icon?
        get() {
            val ip = this.iconPaths.find { it.density == 480 } ?: return null
            return fileIcon(ip.path, ip.density)
        }
    val appIcon192: Icon?
        get() {
            val ip = this.iconPaths.find { it.density == 640 } ?: return null
            return fileIcon(ip.path, ip.density)
        }
//    val appIconMax: Icon?
//        get() {
//            val ip = this.iconPaths.maxBy { it.density } ?: return null
//            return fileIcon(ip.path, ip.density)
//        }

    val appIcons: List<Icon>
        get() {
            val icons = ArrayList<Icon>(iconPaths.size + 1)
            for (ip in this.iconPaths) {
                icons += fileIcon(ip.path, ip.density)
            }
            return icons
        }


    fun transBinaryXml(path: String): String? {
        val data = fileData(path) ?: return null
        val t = XmlTranslator()
        transBinaryXml(data, t)
        return t.xml
    }

    private fun transBinaryXml(data: ByteArray, xmlStreamer: XmlStreamer) {
        val p = BinaryXmlParser(ByteBuffer.wrap(data), resourceTable)
        p.locale = this.preferLocale
        p.xmlStreamer = xmlStreamer
        p.parse()
    }

    private fun fileIcon(filePath: String, density: Int): Icon {
        return Icon(filePath, density, fileData(filePath))
    }
}
