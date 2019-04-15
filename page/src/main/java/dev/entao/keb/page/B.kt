package dev.entao.keb.page

/**
 * Created by entaoyang@163.com on 2018/3/21.
 */






//bootstrap 和 html 常量字符串, 如类名,参数名
//自定义参数请放在P类中
object B {

	const val tablePrimary = "table-primary"
	const val tableSecondary = "table-secondary"
	const val tableSuccess = "table-success"
	const val tableDanger = "table-danger"
	const val tableWarning = "table-warning"
	const val tableInfo = "table-info"

	const val DownArrow = "&darr;"
	const val UpArrow = "&uarr;"

	const val GET = "GET"
	const val POST = "POST"

	const val isValid = "is-valid"
	const val isInValid = "is-invalid"

	const val mrAuto = "mr-auto"

	const val active = "active"
	const val disabled = "disabled"

	const val lead = "lead"
	const val navbar = "navbar"
	const val navbarBrand = "navbar-brand"
	const val navbarToggler = "navbar-toggler"
	const val navbarTogglerIcon = "navbar-toggler-icon"
	const val navbarCollapse = "navbar-collapse"
	const val navbarNav = "navbar-nav"
	const val collapse = "collapse"
	const val alignSelfCenter = "align-self-center"
	const val justifyContentMdCenter = "justify-content-md-center"

	const val bgPrimary = "bg-primary"
	const val bgSecondary = "bg-secondary"
	const val bgSuccess = "bg-success"
	const val bgDanger = "bg-danger"
	const val bgWarning = "bg-warning"
	const val bgInfo = "bg-info"
	const val bgLight = "bg-light"
	const val bgDark = "bg-dark"

	const val btnLarge = "btn-lg"
	const val btnSmall = "btn-sm"
	const val btnBlock = "btn-block"

	const val close = "close"

	const val button = "button"
	const val submit = "submit"
	const val alert = "alert"
	const val group = "group"
	const val toolbar = "toolbar"

	const val textLeft = "text-left"
	const val textRight = "text-right"
	const val textCenter = "text-center"
	const val textJustify = "text-justify"
	const val textNowrap = "text-nowrap"
	const val textLowercase = "text-lowercase"
	const val textCapitalize = "text-capitalize"

	const val textPrimary = "text-primary"
	const val textSuccess = "text-success"
	const val textSecondary = "text-secondary"
	const val textInfo = "text-info"
	const val textWarning = "text-warning"
	const val textDanger = "text-danger"
	const val textWhite = "text-white"

	const val textMuted = "text-muted"

	const val formInline = "form-inline"
	const val formGroup = "form-group"
	const val formControl = "form-control"

	const val col = "col"
	const val row = "row"

	const val colMd1 = "col-md-1"
	const val colMd2 = "col-md-2"
	const val colMd3 = "col-md-3"
	const val colMd4 = "col-md-4"
	const val colMd5 = "col-md-5"
	const val colMd6 = "col-md-6"
	const val colMd7 = "col-md-7"
	const val colMd8 = "col-md-8"
	const val colMd9 = "col-md-9"
	const val colMd10 = "col-md-10"
	const val colMd11 = "col-md-11"
	const val colMd12 = "col-md-12"

	const val btn = "btn"

	const val btnDefault = "btn-default"
	const val btnPrimary = "btn-primary"
	const val btnSecondary = "btn-secondary"
	const val btnSuccess = "btn-success"
	const val btnDanger = "btn-danger"
	const val btnWarning = "btn-warning"
	const val btnInfo = "btn-info"
	const val btnLight = "btn-light"
	const val btnDark = "btn-dark"
	const val btnLink = "btn-link"

	const val btnOutlineDefault = "btn-outline-default"
	const val btnOutlinePrimary = "btn-outline-primary"
	const val btnOutlineSecondary = "btn-outline-secondary"
	const val btnOutlineSuccess = "btn-outline-success"
	const val btnOutlineDanger = "btn-outline-danger"
	const val btnOutlineWarning = "btn-outline-warning"
	const val btnOutlineInfo = "btn-outline-info"
	const val btnOutlineLight = "btn-outline-light"
	const val btnOutlineDark = "btn-outline-dark"

	val btnThemeList = listOf(btnDefault,
			btnPrimary,
			btnSecondary,
			btnSuccess,
			btnDanger,
			btnWarning,
			btnInfo,
			btnLight,
			btnDark,
			btnLink,
			btnOutlineDefault,
			btnOutlinePrimary,
			btnOutlineSecondary,
			btnOutlineSuccess,
			btnOutlineDanger,
			btnOutlineWarning,
			btnOutlineInfo,
			btnOutlineLight,
			btnOutlineDark
	)

	const val alertDefault = "alert-default"
	const val alertPrimary = "alert-primary"
	const val alertSecondary = "alert-secondary"
	const val alertSuccess = "alert-success"
	const val alertDanger = "alert-danger"
	const val alertWarning = "alert-warning"
	const val alertInfo = "alert-info"
	const val alertLight = "alert-light"
	const val alertDark = "alert-dark"

	const val badgeDefault = "badge-default"
	const val badgePrimary = "badge-primary"
	const val badgeSecondary = "badge-secondary"
	const val badgeSuccess = "badge-success"
	const val badgeDanger = "badge-danger"
	const val badgeWarning = "badge-warning"
	const val badgeInfo = "badge-info"
	const val badgeLight = "badge-light"
	const val badgeDark = "badge-dark"

	const val imgThumbnail = "img-thumbnail"

	const val table = "table"
	const val tableStriped = "table-striped"
	const val tableBordered = "table-bordered"
	const val tableHover = "table-hover"
	const val tableCondensed = "table-condensed"
	const val tableResponsive = "table-responsive"
	const val theadDark = "thead-dark"
	const val theadLight = "thead-light"

	object Alert {
		const val Warning = "alert alert-warning alert-dismissible"
		const val Success = "alert alert-success alert-dismissible"
		const val Info = "alert alert-info alert-dismissible"
		const val Danger = "alert alert-danger alert-dismissible"
	}

	const val flex = "d-flex"

	object Flex {
		const val row = "flex-row"
		const val rowReverse = "flex-row-reverse"
		const val column = "flex-column"
		const val columnReverse = "flex-column-reverse"
		const val wrap = "flex-wrap"
		const val noWrap = "flex-nowrap"

		const val justifyContentStart = "justify-content-start"
		const val justifyContentEnd = "justify-content-end"
		const val justifyContentCenter = "justify-content-center"
		const val justifyContentBetween = "justify-content-between"
		const val justifyContentAround = "justify-content-around"
		const val alignItemsStart = "align-items-start"
		const val alignItemsEnd = "align-items-end"
		const val alignItemsCenter = "align-items-center"
		const val alignItemsBaseline = "align-items-baseline"
		const val alignItemsStretch = "align-items-stretch"

		const val alignContentStart = "align-content-start"
		const val alignContentEnd = "align-content-end"
		const val alignContentCenter = "align-content-center"
		const val alignContentBaseline = "align-content-baseline"
		const val alignContentStretch = "align-content-stretch"

		const val alignSelfStart = "align-self-start"
		const val alignSelfEnd = "align-self-end"
		const val alignSelfCenter = "align-self-center"
		const val alignSelfBaseline = "align-self-baseline"
		const val alignSelfStretch = "align-self-stretch"

		const val order1 = "order-1"
		const val order2 = "order-2"
		const val order3 = "order-3"
		const val order4 = "order-4"
		const val order5 = "order-5"
		const val order6 = "order-6"
		const val order7 = "order-7"
		const val order8 = "order-8"
		const val order9 = "order-9"
		const val order10 = "order-10"
		const val order11 = "order-11"
		const val order12 = "order-12"

	}
}