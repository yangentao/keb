@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.kage.ex

import dev.entao.kage.*
import dev.entao.kbase.Label
import dev.entao.ken.HttpContext
import dev.entao.ken.anno.*
import dev.entao.ken.ex.OrderBy
import dev.entao.ken.ex.orderBy
import dev.entao.kage.cardBodyPage
import dev.entao.kage.cardPage
import dev.entao.kage.widget.*
import dev.entao.ken.intList
import dev.entao.sql.AND
import dev.entao.sql.EQ
import dev.entao.sql.IN
import dev.entao.sql.Where
import dev.entao.ken.ex.model.Account
import dev.entao.ken.ok

@LoginWeb
@NavItem
@Label("WEB账号管理")
class AccountPage(context: HttpContext) : HtmlPage(context) {

	@IndexAction
	@NavItem(1)
	@Label("查询")
	fun listAction() {
		val w: Where? = EQ(Account::id, Account::status, Account::phone, Account::deptId) AND LIKE(Account::name)
		val od = OrderBy(Account::id)

		val rowCount = Account.countAll(w)
		val itemList = Account.findAll(w) {
			orderBy(od)
			limitPage()
		}

		cardBodyPage("账号") {
			queryForm {
				divRow {
					divCol {
						labelEditRow(Account::id)
						labelEditRow(Account::phone)
						labelSelectRowStatic(Account::status) {
							optionAll()
						}

					}
					divCol {
						labelEditRow(Account::name)
						labelSelectRowFromTable(Account::deptId) {
							optionNone()
						}
					}
				}
			}

			tableActionPanel {
				deleteChecked(::delArrAction)
				dialog(::dlgAction)
			}
			tableX(itemList, od) {
				columnCheck(Account::id)
				column(Account::id).linkTo(::viewAction, Account::id)
				column(Account::name)
				column(Account::phone)
				column(Account::status)
				column(Account::deptId)
				column(Account::lastIp)
				column(Account::lastLogin)
				columnActionGroup {
					actionLinkProp(::viewAction, Account::id)
					actionLinkProp(::editAction, Account::id)
					actionLinkProp(::disableAction, Account::id).reloadPage()
					actionLinkProp(::enableAction, Account::id).reloadPage()
					actionLinkProp(::delArrAction, Account::id) {
						reloadPage()
					}
				}
			}
			paginationByRowCount(rowCount)
		}

	}

	@Label("禁用", "禁用账号")
	fun disableAction(id: Int) {
		val a = Account.findByKey(id) ?: return
		a.updateByKey {
			a.status = Account.Disabled
		}
		resultSender.ok()
	}

	@Label("启用", "启用账号")
	fun enableAction(id: Int) {
		val a = Account.findByKey(id) ?: return
		a.updateByKey {
			a.status = Account.Enabled
		}
		resultSender.ok()
	}

//	@Label("Dialog")
	fun dlgAction(id: String = "") {
		val d = DialogBuild(context)
		d.title("Title")
		d.bodyBlock = {
			it.textEscaped("Hello Yang: $id")
		}
		val s = d.build().toString()
		htmlSender.print(s)

	}

	fun insertAction() {
		val r = Account()
		r.fromRequest()
		r.insert()
		redirect(::viewAction) {
			arg(r::id)
		}
	}

	@NavItem(2)
	@Label("添加", "添加账号")
	fun addAction() {
		cardPage {
			cardHeader("添加")
			cardBody {
				form(::insertAction) {
					labelEditRow(Account::name) {
						required = true
					}
					labelEditRow(Account::phone) {
						required = true
					}
					labelEditRow(Account::pwd) {
						required = true
					}
					labelRadioRowStatic(Account::status)

					labelSelectRowFromTable(Account::deptId) {
						optionNone()
					}
					submitRow()
				}
			}
		}

	}

	@ActionDanger
	@FormConfirm("要删除这条记录吗?")
	@Label("删除", "删除账号")
	fun delAction(id: Int) {
		Account.delete(Account::id EQ id)
		redirect(::listAction) {
			ok("已删除")
		}
	}

	@ActionDanger
	@Label("删除", "删除账号")
	@FormConfirm("要删除这些记录吗?")
	fun delArrAction(@NotEmpty id: String) {
		Account.delete(Account::id IN id.intList)
		resultSender.ok()
	}

	@Label("查看")
	fun viewAction(id: Int) {
		val r = Account.findByKey(id) ?: return
		cardPage {
			cardHeader("查看") {
				linkButton(::delAction) { param(r.id) }
				linkButton(::editAction) { param(r.id) }.btnPrimary()
			}
			cardBody {
				form {
					labelTextRow(r::id)
					labelTextRow(r::name)
					labelTextRow(r::phone)
					labelTextRow(r::status)
					labelTextRow(r::deptId)

				}
			}
		}
	}

	fun saveAction() {
		val r = Account()
		r.fromRequest()
		r.updateByKey()
		redirect(::viewAction) {
			arg(r::id)
		}
	}

	@Label("编辑", "编辑账号")
	fun editAction(id: Int) {
		val r = Account.findByKey(id) ?: return
		cardPage {
			cardHeader("编辑")
			cardBody {
				form(::saveAction) {
					labelEditRow(r::id) {
						readonly = true
					}
					labelEditRow(r::name) {
						required = true
					}
					labelEditRow(r::phone) {
						required = true
					}
					labelEditRow(r::pwd) {
						required = true
						typePassword()
					}
					labelRadioRowStatic(r::status)
					labelSelectRowFromTable(r::deptId) {
						optionNone()
					}
					submitRow()
				}
			}
		}
	}

}