package dev.entao.keb.biz

import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass

/**
 * Created by entaoyang@163.com on 2018/7/31.
 */

class Province : Model() {

	@dev.entao.kava.sql.PrimaryKey
	var id: String by model


	var name: String by model


	companion object : ModelClass<Province>() {

	}
}