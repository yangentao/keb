package dev.entao.biz

import dev.entao.ken.HttpContext
import dev.entao.ken.HttpPage
import dev.entao.kava.sql.EQ
import dev.entao.kava.json.ysonObject

class AreaApi(context: HttpContext) : HttpPage(context) {

	fun citiesOfProvAction(provId: String) {
		val ls = City.findAll(City::parentId EQ provId)
		jsonSender.arr(ls) { a ->
			ysonObject {
				"code" to a.id
				"label" to a.name
			}
		}
	}

	fun citiesAction(provId: String) {
		val ls = City.findAll(City::parentId EQ provId)
		resultSender.arr(ls) { a ->
			ysonObject {
				"code" to a.id
				"label" to a.name
			}
		}
	}

	fun provinceAction() {
		val ls = Province.findAll {
			asc(Province::id)
		}
		resultSender.arr(ls) { p ->
			ysonObject {
				"code" to p.id
				"label" to p.name
			}
		}
	}

	fun initpAction() {
		val ps = ProvinceData.all
		ps.forEach {
			val p = Province()
			p.id = it.code
			p.name = it.name
			p.insert()
		}
		htmlSender.text("OK")
	}

	fun initcAction() {
		val cls = CityData.all
		cls.forEach {
			val c = City()
			c.id = it.code
			c.name = it.name
			c.parentId = it.parent
			c.insert()
		}
		htmlSender.text("OK")
	}

}