package dev.entao.keb.page


//js,css,image常量连接
object R {

	const val myJS = "/ylib/my.js"
	const val myCSS = "/ylib/my.css"
	const val fileImageDefault = "/ylib/file_miss.png"
	const val navbarLeft = "/ylib/navbar-fixed-left.css"
	const val navbarRight = "/ylib/navbar-fixed-right.css"

	const val jquery = "/ylib/jquery-3.3.1.min.js"
	const val popperJS = "/ylib/propper.min.js"
	const val bootJS = "/ylib/bootstrap.min.js"
	const val buttonsJS = "/ylib/buttons.js"


	const val bootCSS = "/ylib/bootstrap.css"
	const val awesomeCSS = "/ylib/awesome.min.css"

}

//常量应用字符串
object S {
	const val ALL = "全部"
	const val NONE = "无"
	const val PatternDouble = "[0-9]+([\\.][0-9]+)?"
	const val prePage = "上页"
	const val nextPage = "下页"
	const val firstPage = "首页"
	const val lastPage = "末页"
	const val morePage = "..."

}

//应用参数
object P {

	const val pageSize = 50

	//是否倒序
	const val dataDesc = "data-desc"
	//排序字段名
	const val dataSortCol = "data-sortcol"
	const val dataPage = "data-page"

	//不能变, my.js中是固定的
	const val pageN = "p"
	//不能变, my.js中是固定的
	const val ascKey = "asc_key"
	//不能变, my.js中是固定的
	const val descKey = "desc_key"

	const val QUERY_FORM = "queryForm"
}


