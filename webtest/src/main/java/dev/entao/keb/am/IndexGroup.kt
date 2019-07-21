package dev.entao.keb.am

import dev.entao.kava.base.Label
import dev.entao.kava.base.MyDate
import dev.entao.kava.base.userLabel
import dev.entao.kava.base.userName
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.keb.am.model.Account
import dev.entao.keb.am.model.ApkVersion
import dev.entao.keb.core.*
import dev.entao.keb.page.*
import dev.entao.keb.page.ex.fromRequest
import dev.entao.keb.page.ex.showMessagesIfPresent
import dev.entao.keb.page.ex.writeHtml
import dev.entao.keb.page.html.*
import dev.entao.keb.page.widget.*

@Label("员工")
class IndexGroup(context: HttpContext) : HttpGroup(context) {

	override fun indexAction() {
		listAction()
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
		val items = ApkVersion.latest()
		writePage {
			card {
				cardBody {
					tableX(items) {
						column(ApkVersion::id)
						column(ApkVersion::appName)
						column(ApkVersion::pkgName)
						column(ApkVersion::versionCode)
						column(ApkVersion::versionName)
						column(ApkVersion::msg)
						column(ApkVersion::pub_date)
						columnActionGroup {
							actionLinkProp(ResGroup::downloadAction, ApkVersion::id)
							if (context.isLogined) {
								actionLinkProp(::delpkgAction, ApkVersion::pkgName).reloadPage()
							}
						}
					}
				}
			}
		}
	}

	@FormConfirm("要删除此项吗? ")
	@Label("删除")
	fun delpkgAction(pkg: String) {
		val ls = ApkVersion.byPkg(pkg)
		for (a in ls) {
			ResGroup.deleteRes(context, a.resId)
		}
		ApkVersion.delete(ApkVersion::pkgName EQ pkg)
		resultSender.ok()
	}

	fun addResultAction() {
		val m = ApkVersion()
		m.fromRequest(context)
		val d = MyDate()
		m.pub_date = d.sqlDate
		m.pub_time = d.sqlTime
		m.pub_datetime = d.time
		m.account = context.account
		m.insert()
		redirect(::listAction) {
			ok("保存成功")
		}
	}

	@NeedLogin
	@Label("添加")
	fun addAction() {
		writePage {
			card {
				cardBody {
					form(::addResultAction) {
						formRow {
							labelEditGroup(ApkVersion::appName).addClass("col")
							labelEditGroup(ApkVersion::pkgName).addClass("col")
						}
						formRow {
							labelEditGroup(ApkVersion::versionCode).addClass("col")
							labelEditGroup(ApkVersion::versionName).addClass("col")
						}
						labelEditGroup(ApkVersion::msg)
						val g = labelFileGroup(ApkVersion::resId)
						val resTag = g.findChildDeep {
							it.tagName == "input" && it.type == "hidden"
						}
						if (resTag != null) {
							scriptBlock {
								"""
									Yet.onUploadOK = function(resId){
										${'$'}.ajax({                                                                           
											type:"GET",                                                                    
											url:"${ApkGroup::infoAction.uri}",                                 
											data:'id=' + resId,                                                          
											success: function(msg){
												console.log(msg);
												if(msg && msg.code == 0 && msg.data){                                      
													console.log("OKK");
													var verCode = msg.data.versionCode;                                    
													var verName = msg.data.versionName;                                    
													var appName = msg.data.label;                                          
													var pkgName = msg.data.packageName;                                    
													${'$'}("input[name='${ApkVersion::appName.userName}']").val(appName);         
													${'$'}("input[name='${ApkVersion::pkgName.userName}']").val(pkgName);         
													${'$'}("input[name='${ApkVersion::versionCode.userName}']").val(verCode);     
													${'$'}("input[name='${ApkVersion::versionName.userName}']").val(verName);     
													${'$'}("input[name='${ApkVersion::msg.userName}']").val(verName);             
												}else {                                                                    
												}                                                                          
											}                                                                              
										});                                                                                
									};                                                                                      
								""".trimIndent()
							}
						}
						submit()
					}
				}
			}
			ApkGroup.configRes(this)
		}

	}

}