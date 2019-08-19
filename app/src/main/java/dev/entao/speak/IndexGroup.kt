package dev.entao.speak

import com.sun.corba.se.spi.orb.OperationFactory.listAction
import com.sun.tools.corba.se.idl.som.cff.Messages.msg
import dev.entao.kava.base.Label
import dev.entao.kava.base.MyDate
import dev.entao.kava.base.userLabel
import dev.entao.kava.base.userName
import dev.entao.kava.qr.QRImage
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.speak.model.Account
import dev.entao.keb.core.*
import dev.entao.keb.page.*
import dev.entao.keb.page.groups.BootTemplate
import dev.entao.keb.page.modules.ResGroup
import dev.entao.keb.page.modules.Upload
import dev.entao.keb.page.tag.*
import dev.entao.keb.page.widget.*
import net.dongliu.apk.parser.ApkFile
import java.io.File

@Label("员工")
class IndexGroup(context: HttpContext) : HttpGroup(context) {

	override fun indexAction() {
		context.writeHtml("Index")
	}

	fun logoutAction() {
		context.account = ""
		context.redirect(::indexAction.uri)
	}

	fun loginResultAction(username: String = "", pwd: String = "") {
		val a = Account.findOne((Account::phone EQ username) AND (Account::pwd EQ pwd))
		if (a == null) {
			context.backward {
				withoutMessage()
				error("用户名或密码错误")
			}
			return
		}
		context.account = username

		val back = httpParams.str(Keb.BACK_URL) ?: ""
		if (back.isNotEmpty()) {
			context.redirect(back)
		} else {
			context.redirect(::indexAction.uri)
		}

	}

	@LoginAction
	fun loginAction() {
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
							showMessagesIfPresent()
							cardBodyTitle("登录") {
								form(::loginResultAction) {
									val backurl = httpParams.str(Keb.BACK_URL)
									if (backurl != null) {
										hidden(Keb.BACK_URL, backurl)
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

	private fun writePage(block: TagCallback) {
		writeHtml(BootTemplate(context)) {
			html.head {
				title("APP管理")
			}
			html.body {
				divContainer {
					//					buildNavBar(this)
					this.block()
				}
			}
		}
	}

}