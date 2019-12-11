package dev.entao.keb.page


//js,css,image常量连接
object R {

	const val fileImageDefault = "/ylib/file_miss.png"
	const val myJS = "/ylib/my.js"
	const val myJS2 = "/ylib/my2.js"
	const val myCSS = "/ylib/my.css"

	const val sidebarCSS = "/ylib/sidebar.css"

	const val jquery = "/ylib/jquery-3.3.1.min.js"
	const val popperJS = "/ylib/popper.min.js"
//	const val buttonsJS = "/ylib/buttons.js"

	const val bootCSS = "/ylib/boot/bootstrap.css"
	const val bootJS = "/ylib/boot/bootstrap.min.js"

	const val faCSS = "/ylib/fa511/css/all.min.css"
	const val faJS = "/ylib/fa511/js/all.min.js"

	const val mdeCSS = "/ylib/mde/simplemde.css"
	const val mdeJS = "/ylib/mde/simplemde.min.js"

	const val mdviewCSS = "/ylib/mdview/markdown.css"
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

	var pageSize = 50
	//不能变, my.js中是固定的
	var pageArg = "p"
	const val dataPage = "data-page"

	//是否倒序
	const val dataSortDesc = "data-desc"
	//排序字段名
	const val dataSortBy = "data-sortcol"


	const val sortBy = "sortby"
	const val sortDesc = "sortdesc"
	//不能变, my.js中是固定的
//	const val ascKey = "asc_key"
	//不能变, my.js中是固定的
//	const val descKey = "desc_key"

	const val QUERY_FORM = "queryForm"
}


