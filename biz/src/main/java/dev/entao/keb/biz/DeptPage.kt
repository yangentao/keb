@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.keb.biz

import dev.entao.kava.base.Label
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.kava.sql.IN
import dev.entao.kava.sql.Where
import dev.entao.keb.biz.model.Dept
import dev.entao.keb.biz.model.ResAccess
import dev.entao.keb.core.*
import dev.entao.keb.page.*
import dev.entao.keb.page.widget.*

@Label("部门管理")
class DeptPage(context: HttpContext) : HttpGroup(context) {

	override fun indexAction() {
		listAction()
	}

	@Label("查询")
	fun listAction() {
		val w: Where? = EQ(Dept::id, Dept::status) AND LIKE(Dept::name)
		val od = OrderBy(Dept::id)

		val rowCount = Dept.countAll(w)
		val itemList = Dept.findAll(w) {
			orderBy(od)
			limitPage(context)
		}

		cardBodyPage("部门") {
			queryForm {
				divRow {
					divCol {
						labelEditRow(Dept::id)
					}
					divCol {
						labelEditRow(Dept::name)
					}
					divCol {
						labelSelectRowStatic(Dept::status) {
							optionAll()
						}
					}
				}
			}

			tableActionPanel {
				deleteChecked(::delArrAction)
//				dialog(::dlgAction)
			}
			tableX(itemList, od) {
				columnCheck(Dept::id)
				column(Dept::id).linkTo(::viewAction, Dept::id)
				column(Dept::name)
				column(Dept::parentId)
				column(Dept::status)
				columnActionGroup {
					actionLinkProp(::viewAction, Dept::id)
					actionLinkProp(::editAction, Dept::id)
					actionLinkProp(::delArrAction, Dept::id) {
						reloadPage()
					}
					actionLinkProp(::privAction, Dept::id)
				}
			}
			paginationByRowCount(rowCount)
		}

	}

	@Label("保存部门权限")
	fun savePrivAction() {
		this.saveAccess()
	}

	@Label("权限", "编辑部门权限")
	fun privAction(deptId: Int) {
		val d = Dept.findByKey(deptId) ?: return
		this.editAccess(::savePrivAction, deptId, ResAccess.TDept, "编辑 ${d.name} 的权限")
	}

//	@Label("Dialog")
//	fun dlgAction(id: String = "") {
//		val d = DialogBuild(context)
//		d.title("Title")
//		d.bodyBlock = {
//			it.textEscaped("Hello Yang: " + id)
//		}
//		val s = d.build().toString()
//		htmlSender.print(s)
//
//	}

	fun insertAction() {
		val r = Dept()
		r.fromRequest(context)
		r.insert()
		redirect(::viewAction, r.id)
	}

	@Label("添加", "添加部门")
	fun addAction() {
		cardPage {
			cardHeader("添加")
			cardBody {
				form(::insertAction) {
					labelEditRow(Dept::name) {
						required = true
					}
					labelSelectRowFromTable(Dept::parentId) {
						optionNone()
					}
					labelRadioRowStatic(Dept::status)
					submitRow()
				}
			}
		}

	}

	@ActionDanger
	@FormConfirm("要删除这条记录吗?")
	@Label("删除", "删除部门-查看页")
	fun delAction(id: Int) {
		Dept.delete(Dept::id EQ id)
		redirect(::listAction) {
			success("已删除")
		}
	}

	@ActionDanger
	@Label("删除", "删除部门")
	@FormConfirm("要删除这些记录吗?")
	fun delArrAction(@NotEmpty id: String) {
		Dept.delete(Dept::id IN id.intList)
		resultSender.ok()
	}

	@Label("查看")
	fun viewAction(id: Int) {
		val r = Dept.findByKey(id) ?: return
		cardPage {
			cardHeader("查看") {
				linkButton(::delAction) { param(r.id) }.btnDanger().confirm("要删除这条记录吗?")
				linkButton(::editAction) { param(r.id) }.btnPrimary()
			}
			cardBody {
				form {
					labelTextRow(r::id)
					labelTextRow(r::name)
					labelTextRow(r::parentId)
					labelTextRow(r::status)

				}
			}
		}
	}

	fun saveAction() {
		val r = Dept()
		r.fromRequest(context)
		r.updateByKey()
		redirect(::viewAction, r.id)
	}

	@Label("编辑", "编辑部门")
	fun editAction(id: Int) {
		val r = Dept.findByKey(id) ?: return
		cardPage {
			cardHeader("编辑")
			cardBody {
				form(::saveAction) {
					labelEditRow(r::id) {
						readonly = true
					}
					labelEditRow(r::name)
					labelSelectRowFromTable(r::parentId) {
						optionNone()
					}
					labelRadioRowStatic(r::status)
					submitRow()
				}
			}
		}
	}

}