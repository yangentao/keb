@file:Suppress("PropertyName", "MemberVisibilityCanBePrivate", "unused")

package dev.entao.kava.sql

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