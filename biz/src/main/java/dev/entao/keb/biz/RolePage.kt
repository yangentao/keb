@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.keb.biz

import dev.entao.kava.base.Label
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.kava.sql.IN
import dev.entao.kava.sql.Where
import dev.entao.keb.biz.model.Role
import dev.entao.keb.core.HttpContext
import dev.entao.keb.core.HttpGroup
import dev.entao.keb.core.NotEmpty
import dev.entao.keb.core.success
import dev.entao.keb.page.*
import dev.entao.keb.page.widget.*

@Label("角色管理")
class RolePage(context: HttpContext) : HttpGroup(context) {

	override fun indexAction() {
		listAction()
	}

	@Label("查询")
	fun listAction() {
		val w: Where? = EQ(Role::id, Role::status) AND LIKE(Role::name)
		val od = OrderBy(Role::id, false)
		val rowCount = Role.countAll(w)
		val itemList = Role.findAll(w) {
			orderBy(od)
			limitPage(context)
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
		r.fromRequest(context)
		r.insert()
		redirect(::viewAction, r.id)
	}

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
			success("已删除")
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
		r.fromRequest(context)
		r.updateByKey(r::name, r::status)
		redirect(::viewAction, r.id)
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