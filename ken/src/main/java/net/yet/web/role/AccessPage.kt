package net.yet.web.role

import dev.entao.kage.*
import dev.entao.kbase.Label
import dev.entao.kage.widget.*
import dev.entao.yog.logd
import yet.servlet.HttpAction
import dev.entao.ken.HttpPage
import dev.entao.sql.AND
import dev.entao.sql.EQ

enum class Access(val value: Int) {
	Page(-1), None(0), View(1), Edit(2), Create(3), Delete(4)
}

@Label("保存", "保存权限")
fun HttpPage.saveAccess() {
	val objId: Int = httpParams.int("_objId") ?: 0
	val objType: Int = httpParams.int("_objType") ?: 0
	val allResItem = ResItem.fromContext(context)
	allResItem.forEach { p ->
		var pn = if (httpParams.bool(p.data.identity) == true) 1 else 0
		p.children.forEach {
			pn += if (httpParams.bool(it.identity) == true) 1 else 0
		}
		if (pn > 0) {
			pn = 1
		}
		p.children.forEach {
			val n = if (httpParams.bool(it.identity) == true) 1 else 0
			val ra = ResAccess()
			ra.uri = it.uri
			ra.objId = objId
			ra.objType = objType
			logd("ResAccess: ", it.uri)
			if (it.accessLevel == Access.Page.value) {
				ra.judge = pn
			} else {
				ra.judge = n
			}
			ra.condition = ""
			ra.replace()
		}
		val ra = ResAccess()
		ra.uri = p.data.uri
		ra.objId = objId
		ra.objType = objType
		ra.judge = pn
		ra.condition = ""
		ra.replace()
	}
	resultSender.ok()
}

@Label("权限", "编辑权限")
fun HttpPage.editAccess(saveAction: HttpAction, objId: Int, objType: Int, title: String) {
	val allResItem = ResItem.fromContext(context)
	val w = (ResAccess::objId EQ objId) AND (ResAccess::objType EQ objType)
	val resList: List<ResAccess> = ResAccess.findAll(w)
	val resMap = HashMap<String, ResAccess>()
	resList.forEach {
		resMap[it.uri] = it
	}
	sidebarPage {
		form(saveAction) {
			hidden("_objId", objId)
			hidden("_objType", objType)
			card {
				cardHeader(title) {
					submit("保存").submitAsync("""
							if(resp.code == 0){
							    Yet.showAlert('<center><font size="5" color="green">保存成功</font></center>');
							}else{
								Yet.showAlert('<center><font size="5" color="red">保存失败</font></center>');
							}
						""".trimIndent())
				}
				cardBody {

					table {
						tbody {
							allResItem.forEach { resItem ->
								val actionList = resItem.children
								tr {
									td {
										formCheck {
											style = "white-space: nowrap;"
											val cb = checkbox {
												idName(resItem.data.identity)
											}
											label {
												+resItem.data.name
												forId = cb.needId()
											}
											val checkId = cb.needId()
											scriptBlock {
												"""
													$('#$checkId').click(function (e) {
														$(this).closest('tr').children('td:eq(1)').find('input:checkbox').prop('checked', this.checked);
													});

													"""
											}
										}
									}
									td {
										var checkCount = 0
										div {
											addClass("d-flex", "flex-wrap", "my-3")
											actionList.forEach { ai ->
												div {
													addClass("mr-3")
													formCheck {
														style = "white-space: nowrap;"
														val cb = checkbox {
															idName(ai.identity)
															val b = resMap[ai.uri]?.judge == ResAccess.Allow
															checked = b
															if (b) {
																checkCount += 1
															}
														}
														label {
															+ai.name
															forId = cb.needId()
														}
													}
												}
											}
										}
										val pNode = this.findParent { it.tagName == "tr" }?.findChild {
											it.tagName == "td"
										}?.findChildDeep {
											it.tagName == "input" && it.type == "checkbox"
										}
										if (pNode != null) {
											if (checkCount > 0) {
												pNode.checked = true
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}