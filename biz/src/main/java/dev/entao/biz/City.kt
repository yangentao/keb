package dev.entao.biz

import dev.entao.sql.ForeignKey
import dev.entao.sql.ForeignLabel
import dev.entao.sql.PrimaryKey
import dev.entao.sql.Model
import dev.entao.sql.ModelClass

/**
 * Created by entaoyang@163.com on 2018/7/31.
 */


class City : Model() {

	@PrimaryKey
	var id: String by model


	var name: String by model

	@ForeignLabel("name")
	@ForeignKey(Province::class)
	var parentId: String by model


	companion object : ModelClass<City>() {

	}
}