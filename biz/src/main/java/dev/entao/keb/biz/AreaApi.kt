package dev.entao.keb.biz

import dev.entao.kava.json.ysonArray
import dev.entao.kava.json.ysonObject
import dev.entao.kava.sql.EQ
import dev.entao.keb.core.render.ResultRender
import dev.entao.keb.core.render.YsonRender

class AreaApi(context: dev.entao.keb.core.HttpContext) : dev.entao.keb.core.HttpGroup(context) {

	fun citiesOfProvAction(provId: String) {
		val ls = City.findAll(City::parentId EQ provId)
		val ya = ysonArray(ls){ a ->
			ysonObject {
				"code" to a.id
				"label" to a.name
			}
		}
		context.writeJSON(ya.yson())

	}

	fun citiesAction(provId: String) {
		val ls = City.findAll(City::parentId EQ provId)
		ResultRender(context).arr(ls) { a ->
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
		ResultRender(context).arr(ls) { p ->
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
		context.writeHtml("OK")
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
		context.writeHtml("OK")
	}

}