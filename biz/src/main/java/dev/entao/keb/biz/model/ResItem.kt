package dev.entao.keb.biz.model

import dev.entao.kava.base.DefaultValue
import dev.entao.kava.base.Label
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass
import dev.entao.keb.biz.Parent
import dev.entao.keb.core.HttpContext
import java.util.*

enum class Access(val value: Int) {
	Page(-1), None(0), View(1), Edit(2), Create(3), Delete(4)
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AccessLevel(val level: Access)

@Label("组")
class ResItem : Model() {

	@dev.entao.kava.sql.PrimaryKey
	@Label("资源")
	var uri: String by model

	@Label("名称")
	var name: String by model

	var identity: String by model

	@DefaultValue("0")
	var accessLevel: Int by model

	override fun equals(other: Any?): Boolean {
		if (other is ResItem) {
			return this.uri.equals(other.uri)
		}
		return false
	}

	override fun hashCode(): Int {
		return uri.hashCode()
	}

	companion object : ModelClass<ResItem>() {


		@Suppress("UNUSED_PARAMETER")
		fun fromContext(context: HttpContext): List<Parent<ResItem>> {
			val nodeList = ArrayList<Parent<ResItem>>()
//			val ps = context.filter.allPages.filter { it.hasAnnotation<NavItem>() }.sortedBy {
//				it.findAnnotation<NavItem>()!!.order
//			}
//			ps.forEach { pageClass ->
//				val resItem = ResItem()
//				resItem.uri = WebPath.buildPath(context.filter.contextPath, pageClass.pageName)
//				resItem.name = pageClass.userDesc
//				resItem.identity = pageClass.pageName
//				resItem.accessLevel = 0
//				val node = Parent(resItem)
//				nodeList += node
//				pageClass.actionList.filter { it.hasAnnotation<Label>() || it.hasAnnotation<NavItem>() }.sortedBy {
//					it.findAnnotation<NavItem>()?.order ?: 99
//				}.forEach { ac ->
//					val acItem = ResItem()
//					acItem.name = ac.userDesc
//					acItem.uri = context.path.action(ac).uri
//					acItem.identity = pageClass.pageName + "." + ac.userName
//					resItem.accessLevel = ac.findAnnotation<AccessLevel>()?.level?.value ?: 0
//					node.add(acItem)
//				}
//			}
			return nodeList
		}
	}
}