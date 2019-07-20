package dev.entao.keb.am.model

import dev.entao.kava.base.Name
import dev.entao.kava.sql.AutoInc
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass
import dev.entao.kava.sql.PrimaryKey

@Name("upload")
class UploadRes : Model() {

	@PrimaryKey
	@AutoInc
	var id: Int by model
	var uuid: String by model
	var dir: String by model
	var rawname: String by model
	var size: Int by model
	var contentType: String by model
	var userId: Int by model


	companion object : ModelClass<UploadRes>()
}
