@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.biz

import dev.entao.kage.*
import dev.entao.kava.base.Label
import dev.entao.ken.anno.*
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.kava.sql.IN
import dev.entao.kava.sql.Where
import dev.entao.kage.widget.*
import dev.entao.ken.HttpContext
import dev.entao.ken.ex.OrderBy
import dev.entao.ken.ex.orderBy
import dev.entao.biz.model.Role
import dev.entao.ken.ok

@LoginWeb
@NavItem
@Label("角色管理")
class RolePage(context: HttpContext) : HtmlPage(context) {

	fun indexAction() {
		redirect(::listAction)
	}

	@NavItem( 1)
	@Label("查询")
	fun listAction() {
		val w: Where? = EQ(Role::id, Role::status) AND LIKE(Role::name)
		val od = OrderBy(Role::id, false)
		val rowCount = Role.countAll(w)
		val itemList = Role.findAll(w) {
			orderBy(od)
			limitPage()
		}

		cardPage {
			cardHeader("查询")
			cardBody {
				queryForm {
					labelEditRow(Role::id)
					labelEditRow(Role::name)
					labelSelectRowStatic(Role::status, httpParams.str(Role::status)) {
						option("", "全部").bringToFirst()
					}
				}

				tableActionPanel {
					deleteChecked(::delArrAction)
				}
				tableX(itemList, od) {
					columnCheck(Role::id)
					column(Role::id).linkTo(::viewAction)
					column(Role::name)
					column(Role::status)
					columnActionGroup {
						actionLinkProp(::viewAction, Role::id)
						actionLinkProp(::editAction, Role::id)
						actionLinkProp(::delArrAction, Role::id) {
							reloadPage()
						}
					}
				}
				paginationByRowCount(rowCount)
			}
		}
	}

	fun insertAction() {
		val r = Role()
		r.fromRequest()
		r.insert()
		redirect(::viewAction) {
			arg(r::id)
		}
	}

	@NavItem( 2)
	@Label("添加", "添加角色")
	fun addAction() {
		cardPage {
			cardHeader("添加")
			cardBody {
				form(::insertAction) {
					labelEditRow(Role::name) {
						required = true
					}
					labelSelectRowStatic(Role::status) {

					}
					submitRow()
				}
			}
		}

	}

	@Label("删除", "删除角色")
	fun delAction(id: Int) {
		Role.delete(Role::id EQ id)
		redirect(::listAction) {
			ok("已删除")
		}
	}

	@Label("删除", "删除角色")
	@FormConfirm("要删除这条记录吗?")
	fun delArrAction(@NotEmpty id: String) {
		val ls = id.split(',').map { it.toInt() }
		Role.delete(Role::id IN ls)
		resultSender.ok()
	}

	@Label("查看")
	fun viewAction(id: Int) {
		val r = Role.findByKey(id) ?: return
		cardPage {
			cardHeader("查看") {
				linkButton(::delAction) { param(r.id) }.btnDanger().confirm("要删除这条记录吗?")
				linkButton(::editAction) { param(r.id) }.btnPrimary()
			}
			cardBody {
				form {
					labelTextRow(r::id)
					labelTextRow(r::name)
					labelTextRow(r::status)

				}
			}
		}
	}

	fun saveAction() {
		val r = Role()
		r.fromRequest()
		r.updateByKey(r::name, r::status)
		redirect(::viewAction) {
			arg(r::id)
		}
	}

	@Label("编辑", "编辑角色")
	fun editAction(id: Int) {
		val r = Role.findByKey(id) ?: return
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
					labelSelectRowStatic(r::status) {}

					submitRow()
				}
			}
		}
	}

}