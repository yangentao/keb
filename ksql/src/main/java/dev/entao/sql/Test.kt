package dev.entao.sql

class Test : Model() {

	@AutoInc
	@PrimaryKey
	var id: Int by model
	var name: String by model
}