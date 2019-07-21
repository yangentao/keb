package dev.entao.keb.am

import dev.entao.kava.base.Label
import dev.entao.kava.base.MyDate
import dev.entao.kava.base.userLabel
import dev.entao.kava.base.userName
import dev.entao.kava.qr.QRImage
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.keb.am.model.Account
import dev.entao.keb.am.model.ApkVersion
import dev.entao.keb.am.model.UploadRes
import dev.entao.keb.core.*
import dev.entao.keb.page.*
import dev.entao.keb.page.ex.Upload
import dev.entao.keb.page.ex.fromRequest
import dev.entao.keb.page.ex.showMessagesIfPresent
import dev.entao.keb.page.ex.writeHtml
import dev.entao.keb.page.html.*
import dev.entao.keb.page.widget.*
import net.dongliu.apk.parser.ApkFile
import java.io.File
import javax.imageio.ImageIO

@Label("员工")
class IndexGroup(context: HttpContext) : HttpGroup(context) {

	override fun indexAction() {
		redirect(::listAction)
	}

	fun checkAction(pkg: String) {
		val a = ApkVersion.last(pkg)
		if (a != null) {
			resultSender.obj(a.toJson(
					ApkVersion::pkgName,
					ApkVersion::versionCode,
					ApkVersion::versionName,
					ApkVersion::msg,
					ApkVersion::resId))
		} else {
			resultSender.failed(-1, "没有新版本")
		}

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

	fun pkgAction(pkg: String) {
		val a = ApkVersion.findOne(ApkVersion::pkgName EQ pkg) {
			desc(ApkVersion::versionCode)
		} ?: return

		bootPage {
			head {
				title(a.appName)
			}
			body {
				divContainer {
					divRow {
						divCol {
							cardBodyTitle(a.appName) {
								ul {
									li {
										+("版本号   :" + a.versionName)
									}
									li {
										+("数字版本号:" + a.versionCode)

									}
									li {
										+("上传时间  :" + MyDate(a.pub_datetime).formatDateTime())
									}
								}

								p {
									+"点击右上角菜单, 在浏览器中打开, 然后点下载按钮下载"
								}
								linkButton(ResGroup::downloadAction, a.resId).btnPrimary()
							}
						}
					}
				}
			}
		}
	}

	private fun imgUrl(pkg: String): String {
		return context.resUri("imgPkg/$pkg.png")
	}

	private fun imgFile(pkg: String): File {
		val d = File(context.filter.webDir.webRootFile, "imgPkg")
		if (!d.exists()) {
			d.mkdirs()
		}
		return File(d, "$pkg.png")
	}

	private fun makeQR(imageFile: File, ver: ApkVersion) {
		val fullUrl = UriMake(context, ::pkgAction).param(ver.pkgName).fullUrl
		val ur = Upload.findByKey(ver.resId) ?: return
		try {
			val data = ApkFile(ur.localFile(context)).appIconMax?.data ?: return
			val tmpFile = File(context.tmpDir, "" + System.currentTimeMillis())
			tmpFile.writeBytes(data)
			QRImage(fullUrl).icon(tmpFile).makeToFile(imageFile)
			tmpFile.delete()
		} catch (ex: Exception) {
			QRImage(fullUrl).makeToFile(imageFile)
		}
	}

	private fun buildImage(tag: Tag, item: ApkVersion) {
		val file = imgFile(item.pkgName)
		if (!file.exists()) {
			makeQR(file, item)
		}
		if (file.exists()) {
			tag.img {
				src = imgUrl(item.pkgName)
				width = "200px"
				height = "200px"
			}
		}
	}

	@Label("列表-包名")
	fun listpkgAction(pkg: String) {
		val items = ApkVersion.byPkg(pkg)
		writePage {
			card {
				cardBody {
					val firstItem = items.firstOrNull()
					if (firstItem != null) {
						buildImage(this, firstItem)
					}

					tableX(items) {
						column(ApkVersion::id)
						column(ApkVersion::appName)
						column(ApkVersion::pkgName)
						column(ApkVersion::versionCode)
						column(ApkVersion::versionName)
						column(ApkVersion::msg)
						column(ApkVersion::pub_date)
						columnActionGroup {
							actionLinkProp(ResGroup::downloadAction, ApkVersion::resId)
							if (context.isLogined) {
								actionLinkProp(::delAction, ApkVersion::id).reloadPage()
							}
						}
					}
				}
			}
		}
	}

	@NeedLogin
	@FormConfirm("要删除此项吗? ")
	@Label("删除")
	fun delAction(id: Int) {
		val a = ApkVersion.findByKey(id) ?: return
		ResGroup.deleteRes(context, a.resId)
		a.deleteByKey()
		resultSender.ok()
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
						column(ApkVersion::pkgName).linkTo(::listpkgAction)
						column(ApkVersion::versionCode)
						column(ApkVersion::versionName)
						column(ApkVersion::msg)
						column(ApkVersion::pub_date)
						columnActionGroup {
							actionLinkProp(ResGroup::downloadAction, ApkVersion::resId)
							if (context.isLogined) {
								actionLinkProp(::delpkgAction, ApkVersion::pkgName).reloadPage()
							}
						}
					}
				}
			}
		}
	}

	@NeedLogin
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

	@NeedLogin
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