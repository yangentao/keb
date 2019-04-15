package dev.entao.keb.biz

import dev.entao.keb.biz.model.Area

object ProvinceData {
	fun isZhiXia(provinceName: String): Boolean {
		return provinceName.startsWith("北京") ||
				provinceName.startsWith("上海") ||
				provinceName.startsWith("天津") ||
				provinceName.startsWith("重庆") ||
				provinceName.startsWith("香港") ||
				provinceName.startsWith("澳门")
	}

	val zhiXiaCodeSet = setOf("110000", "120000", "310000", "500000", "810000", "820000")

	fun nameOf(code: String): String {
		return map[code] ?: ""
	}

	val map: HashMap<String, String> by lazy {
		val m = HashMap<String, String>()
		all.forEach {
			m[it.code] = it.name
		}
		m
	}

	val all: List<Area> = listOf(
			Area("110000", "北京市", ""),
			Area("120000", "天津市", ""),
			Area("130000", "河北省", ""),
			Area("140000", "山西省", ""),
			Area("150000", "内蒙古", ""),
			Area("210000", "辽宁省", ""),
			Area("220000", "吉林省", ""),
			Area("230000", "黑龙江省", ""),
			Area("310000", "上海市", ""),
			Area("320000", "江苏省", ""),
			Area("330000", "浙江省", ""),
			Area("340000", "安徽省", ""),
			Area("350000", "福建省", ""),
			Area("360000", "江西省", ""),
			Area("370000", "山东省", ""),
			Area("410000", "河南省", ""),
			Area("420000", "湖北省", ""),
			Area("430000", "湖南省", ""),
			Area("440000", "广东省", ""),
			Area("450000", "广西", ""),
			Area("460000", "海南省", ""),
			Area("500000", "重庆市", ""),
			Area("510000", "四川省", ""),
			Area("520000", "贵州省", ""),
			Area("530000", "云南省", ""),
			Area("540000", "西藏", ""),
			Area("610000", "陕西省", ""),
			Area("620000", "甘肃省", ""),
			Area("630000", "青海省", ""),
			Area("640000", "宁夏", ""),
			Area("650000", "新疆", ""),
			Area("710000", "台湾省", ""),
			Area("810000", "香港", ""),
			Area("820000", "澳门", "")
	)
}