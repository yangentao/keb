@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.kava.qr


import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

class QRImage(val content: String) {
    var format = "png"    //图片的格式
    var qrSize = 400      //二维码图片的大小
    var iconPercent = 0.20  // 图标的高宽是二维码高宽的1/5
    var level: ErrorCorrectionLevel = ErrorCorrectionLevel.M

    //中间小图标, 常见的如android应用程序的图标
    private var iconFile: File? = null

    //中间小图标, 常见的如android应用程序的图标
    fun icon(file: File): QRImage {
        this.iconFile = file
        return this
    }

    fun makeToBuffer(): ByteArray? {
        try {
            val img = make()
            val os = ByteArrayOutputStream(16 * 1024)
            ImageIO.write(img, format, os)
            return os.toByteArray()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun makeToFile(saveToFile: File): Boolean {
        try {
            val img = make()
            return ImageIO.write(img, format, saveToFile)
        } catch (ex: Exception) {
        }
        return false
    }

    private fun make(): BufferedImage {
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "utf-8"    //指定字符编码为“utf-8”
        hints[EncodeHintType.ERROR_CORRECTION] = level  //指定二维码的纠错等级为中级
        hints[EncodeHintType.MARGIN] = 2    //设置图片的边距
        val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints)
        val qrImg = MatrixToImageWriter.toBufferedImage(bitMatrix)
        val iconF = iconFile ?: return qrImg
        val iconImage = ImageIO.read(iconF)
        val dst = BufferedImage(qrSize, qrSize, BufferedImage.TYPE_INT_RGB)
        val g = dst.graphics
        g.drawImage(qrImg, 0, 0, qrSize, qrSize, null)
        val iW = (qrSize * iconPercent).toInt()
        val iH = (qrSize * iconPercent).toInt()
        val scaledIcon = iconImage.getScaledInstance(iW, iH, Image.SCALE_SMOOTH)
        g.drawImage(scaledIcon, (qrSize - iW) / 2, (qrSize - iH) / 2, iW, iH, null)
        return dst
    }

    companion object {
        fun scan(file: File): String? {
            return scan(ImageIO.read(file))
        }

        fun scan(bufImage: BufferedImage): String? {
            val bmp = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(bufImage)))
            val hints = HashMap<DecodeHintType, Any>()
            hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
            hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(BarcodeFormat.QR_CODE)
            val r = MultiFormatReader().decode(bmp, hints)
            return r?.text
        }
    }
}