package net.yet.web.role

import dev.entao.kbase.*
import dev.entao.ken.HttpContext
import dev.entao.ken.WebPath
import dev.entao.ken.anno.*
import yet.servlet.*
import dev.entao.sql.Model
import dev.entao.sql.ModelClass
import dev.entao.sql.PrimaryKey
import yet.util.Parent
import java.util.*
import kotlin.reflect.full.findAnnotation

@Label("组")
class ResItem : Model() {

	@PrimaryKey
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


		fun fromContext(context: HttpContext): List<Parent<ResItem>> {
			val nodeList = ArrayList<Parent<ResItem>>()
			val ps = context.filter.allPages.filter { it.hasAnnotation<NavItem>() }.sortedBy {
				it.findAnnotation<NavItem>()!!.order
			}
			ps.forEach { pageClass ->
				val resItem = ResItem()
				resItem.uri = WebPath.buildPath(context.filter.contextPath, context.filter.patternPath, pageClass.pageName)
				resItem.name = pageClass.userDesc
				resItem.identity = pageClass.pageName
				resItem.accessLevel = 0
				val node = Parent(resItem)
				nodeList += node
				pageClass.actionList.filter { it.hasAnnotation<Label>() || it.hasAnnotation<NavItem>() }.sortedBy {
					it.findAnnotation<NavItem>()?.order ?: 99
				}.forEach { ac ->
					val acItem = ResItem()
					acItem.name = ac.userDesc
					acItem.uri = context.path.action(ac).uri
					acItem.identity = pageClass.pageName + "." + ac.userName
					resItem.accessLevel = ac.findAnnotation<AccessLevel>()?.level?.value ?: 0
					node.add(acItem)
				}
			}
			return nodeList
		}
	}
}