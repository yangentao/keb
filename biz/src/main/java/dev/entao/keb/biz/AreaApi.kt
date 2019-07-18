package dev.entao.keb.biz

import dev.entao.kava.json.ysonObject
import dev.entao.kava.sql.EQ
import dev.entao.keb.core.render.YsonRender

class AreaApi(context: dev.entao.keb.core.HttpContext) : dev.entao.keb.core.HttpGroup(context) {

	fun citiesOfProvAction(provId: String) {
		val ls = City.findAll(City::parentId EQ provId)
		YsonRender(context).writeArray(ls) { a ->
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