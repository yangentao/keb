@file:Suppress("PropertyName", "MemberVisibilityCanBePrivate", "unused")

package dev.entao.kava.sql

import dev.entao.kava.base.Name

/**
 * Created by entaoyang@163.com on 2017/6/10.
 */
//TABLE_CAT=apps, TABLE_SCHEM=null, TABLE_NAME=person4, COLUMN_NAME=id, DATA_TYPE=4, TYPE_NAME=INT, COLUMN_SIZE=10, BUFFER_LENGTH=65535, DECIMAL_DIGITS=null, NUM_PREC_RADIX=10, NULLABLE=0, REMARKS=, COLUMN_DEF=null, SQL_DATA_TYPE=0, SQL_DATETIME_SUB=0, CHAR_OCTET_LENGTH=null, ORDINAL_POSITION=1, IS_NULLABLE=NO, SCOPE_CATALOG=null, SCOPE_SCHEMA=null, SCOPE_TABLE=null, SOURCE_DATA_TYPE=null, IS_AUTOINCREMENT=YES, IS_GENERATEDCOLUMN=NO,
//TABLE_CAT=apps, TABLE_SCHEM=null, TABLE_NAME=person4, COLUMN_NAME=person, DATA_TYPE=-1, TYPE_NAME=JSON, COLUMN_SIZE=1073741824, BUFFER_LENGTH=65535, DECIMAL_DIGITS=null, NUM_PREC_RADIX=10, NULLABLE=0, REMARKS=, COLUMN_DEF=null, SQL_DATA_TYPE=0, SQL_DATETIME_SUB=0, CHAR_OCTET_LENGTH=null, ORDINAL_POSITION=2, IS_NULLABLE=NO, SCOPE_CATALOG=null, SCOPE_SCHEMA=null, SCOPE_TABLE=null, SOURCE_DATA_TYPE=null, IS_AUTOINCREMENT=NO, IS_GENERATEDCOLUMN=NO,
class ColumnInfo : Model() {
    var TABLE_CAT: String? by model
    var TABLE_SCHEM: String? by model
    var TABLE_NAME: String by model
    var COLUMN_NAME: String by model
    var DATA_TYPE: Int by model
    var TYPE_NAME: String by model
    var COLUMN_SIZE: Int? by model
    var NULLABLE: Int by model
    var IS_NULLABLE: String by model
    var IS_AUTOINCREMENT: String by model
    var IS_GENERATEDCOLUMN: String by model


    val tableName: String get() = TABLE_NAME
    val columnName: String get() = COLUMN_NAME
    val typeName: String get() = TYPE_NAME
    val autoInc: Boolean get() = IS_AUTOINCREMENT == "YES"
    val nullable: Boolean get() = IS_NULLABLE == "YES"

}

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