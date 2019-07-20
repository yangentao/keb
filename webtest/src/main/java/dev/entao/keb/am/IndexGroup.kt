package dev.entao.keb.am

import dev.entao.kava.base.Label
import dev.entao.kava.base.userLabel
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.keb.am.model.Account
import dev.entao.keb.core.*
import dev.entao.keb.page.*
import dev.entao.keb.page.ex.writeHtml
import dev.entao.keb.page.html.*
import dev.entao.keb.page.widget.*

@Label("员工")
class IndexGroup(context: HttpContext) : HttpGroup(context) {

	override fun indexAction() {
		addAction()
	}

	fun logoutAction() {
		context.clearLoginAccount()
		context.redirect(::indexAction.uri)
	}

	fun loginResultAction(username: String = "", pwd: String = "") {
		val a = Account.findOne((Account::phone EQ username) AND (Account::pwd EQ pwd))
		if (a == null) {
			context.backward {
				withoutMessage()
				err("用户名或密码错误")
			}
			return
		}
		context.setLoginAccount(username)

		val back = httpParams.str(KebConst.BACK_URL) ?: ""
		if (back.isNotEmpty()) {
			context.redirect(back)
		} else {
			context.redirect(::indexAction.uri)
		}

	}

	@LoginAction
	fun loginAction(username: String? = null, pwd: String? = null, errorMsg: String? = null) {
		bootPage {
			head {
				title("登录")
			}
			body {
				divContainer {
					divRow {
						style = "margin-top:3cm"
						divCol6 {
							offsetCol(3)
							if (errorMsg != null) {
								alertError { +errorMsg }
							}
							cardBodyTitle("登录") {
								form(::loginResultAction) {
									val backurl = httpParams.str(KebConst.BACK_URL)
									if (backurl != null) {
										hidden(KebConst.BACK_URL, backurl)
									}
									labelEditGroup("用户名", "username") {
									}
									labelEditGroup("密码", "pwd") {
										typePassword()
									}
									submit("登录")
								}
							}
						}
					}
				}
			}
		}
	}

	private fun buildNavBar(ptag: Tag) {
		val logined = context.isLogined
		var acList = listOf(::listAction, ::addAction)
		if (!logined) {
			acList = acList.filter { !it.isNeedLogin }
		}
		ptag.navbarDark("APP管理") {
			navbarLeft {
				acList.forEach {
					val u = context.actionUri(it)
					navbarItemLink(it.userLabel, u, context.currentUri == u)
				}
			}
			navbarRight {
				if (context.isLogined) {
					navbarItemLink("注销", context.actionUri(::logoutAction))
				} else {
					navbarItemLink("登录", context.actionUri(::loginAction))
				}
			}
		}
	}

	private fun writePage(block: TagCallback) {
		writeHtml(AppPage(context)) {
			html.head {
				title("APP管理")
			}
			html.body {
				divContainer {
					buildNavBar(this)
					this.block()
				}
			}
		}
	}

	@Label("列表")
	fun listAction() {
		writePage {
			p {
				+" List  "
			}
		}
	}

	@NeedLogin
	@Label("添加")
	fun addAction() {
		writePage {
			p {
				+" ADD "
			}
		}

	}

}