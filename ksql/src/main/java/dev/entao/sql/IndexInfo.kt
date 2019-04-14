package dev.entao.sql

import dev.entao.kbase.Name

//TABLE_CAT=campus, TABLE_SCHEM=null, TABLE_NAME=ip, NON_UNIQUE=false, INDEX_QUALIFIER=,
// INDEX_NAME=PRIMARY, TYPE=3, ORDINAL_POSITION=1, COLUMN_NAME=id, ASC_OR_DESC=A, CARDINALITY=6,
// PAGES=0, FILTER_CONDITION=null,
class IndexInfo : Model() {

	@Name("TABLE_CAT")
	var dbName: String by model

	@Name("TABLE_NAME")
	var tableName: String by model

	@Name("INDEX_NAME")
	var indexName: String by model

	@Name("COLUMN_NAME")
	var colName: String by model

	@Name("TYPE")
	var type: Int by model

}