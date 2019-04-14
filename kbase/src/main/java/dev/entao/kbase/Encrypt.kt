package dev.entao.kbase

import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by entaoyang@163.com on 2018/7/12.
 */

object Encrypt {

	fun md5(value: String): String {
		val md5 = MessageDigest.getInstance("MD5")
		md5.update(value.toByteArray())
		val m = md5.digest()// 加密
		return Hex.encode(m)
	}

	object B64 {
		fun encode(value: String): String {
			if (value.isEmpty()) {
				return ""
			}
			val e = Base64.getUrlEncoder()
			return e.encodeToString(value.toByteArray())
		}

		fun decode(value: String): String {
			if (value.isEmpty()) {
				return ""
			}
			val e = Base64.getUrlDecoder()
			val ar = e.decode(value)
			return String(ar, Charsets.UTF_8)
		}
	}
}

class DesEcb(val key: ByteArray) {
	var algo = "DES/ECB/NoPadding"

	fun noPadding(): DesEcb {
		algo = "DES/ECB/NoPadding"
		return this
	}

	fun pkcs5(): DesEcb {
		algo = "DES/ECB/PKCS5Padding"
		return this
	}

	fun encode(data: ByteArray): ByteArray {
		val cipher = Cipher.getInstance(algo)
		cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "DES"))
		return cipher.doFinal(data)
	}

	fun decode(data: ByteArray): ByteArray {
		val cipher = Cipher.getInstance(algo)
		cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "DES"))
		return cipher.doFinal(data)
	}
}

class DecCbc(val key: ByteArray) {
	var algo = "DES/CBC/NoPadding"

	fun noPadding(): DecCbc {
		algo = "DES/CBC/NoPadding"
		return this
	}

	fun pkcs5(): DecCbc {
		algo = "DES/CBC/PKCS5Padding"
		return this
	}

	fun encode(data: ByteArray): ByteArray {
		val securekey = SecretKeyFactory.getInstance("DES").generateSecret(DESKeySpec(key))
		val cipher = Cipher.getInstance(algo)
		cipher.init(Cipher.ENCRYPT_MODE, securekey, IvParameterSpec(key))
		return cipher.doFinal(data)
	}

	@Throws(Exception::class)
	fun decode(data: ByteArray): ByteArray {
		val securekey = SecretKeyFactory.getInstance("DES").generateSecret(DESKeySpec(key))
		val cipher = Cipher.getInstance(algo)
		cipher.init(Cipher.DECRYPT_MODE, securekey, IvParameterSpec(key))
		return cipher.doFinal(data)
	}
}