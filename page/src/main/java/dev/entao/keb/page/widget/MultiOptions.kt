@file:Suppress("unused")

package dev.entao.keb.page.widget

import dev.entao.kava.base.*
import dev.entao.kava.log.loge
import dev.entao.kava.sql.*
import dev.entao.keb.core.HttpAction
import dev.entao.keb.core.HttpContext
import dev.entao.keb.page.FormOptions
import dev.entao.keb.page.FormSelectFromTable
import dev.entao.keb.page.S
import dev.entao.keb.page.bootstrap.formGroup
import dev.entao.keb.page.bootstrap.processHelpText
import dev.entao.keb.page.bootstrap.propValue
import dev.entao.keb.page.tag.*
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/4/6.
 */

class OptionTag(context: HttpContext) : Tag(context, "option") {

	var label: String by attrs
	var value: String by attrs
}

val KProperty<*>.formOptionsMap: Map<String, String>
	get() {
		val fs = this.findAnnotation<FormOptions>() ?: return emptyMap()
		val arr = fs.options
		val map = LinkedHashMap<String, String>()
		arr.forEach {
			val kv = it.split(":")
			if (kv.size == 2) {
				map[kv[0]] = kv[1]
			} else if (kv.size == 1) {
				map[kv[0]] = kv[0]
			}
		}
		return map
	}

private fun keyValueMapByTable(tableName: String, keyCol: String, labelCol: String, w: Where? = null): Map<String, String> {
	val map = LinkedHashMap<String, String>()
	if (keyCol == labelCol) {
		val q = SQLQuery().from(tableName).select(keyCol).asc(keyCol).DISTINCT.where(w)
		val ls = ConnLook.defaultConnection.query(q).allRows()
		ls.forEach {
			val k = it[keyCol]
			if (k != null) {
				map.put(k.toString(), k.toString())
			}
		}
	} else {
		val q = SQLQuery().from(tableName).select(keyCol, labelCol).asc(labelCol).DISTINCT.where(w)
		val ls = ConnLook.defaultConnection.query(q).allRows()
		ls.forEach {
			val k = it[keyCol]
			val v = it[labelCol]
			if (k != null && v != null) {
				map[k.toString()] = v.toString()
			}
		}
	}
	return map
}

fun KProperty<*>.selectOptionsTable(w: Where? = null): Map<String, String> {
	val map = HashMap<String, String>()
	val fk = this.findAnnotation<ForeignKey>()
	val fcol = this.findAnnotation<ForeignLabel>()

	if (fk != null && fcol != null) {
		if (fk.cls.modelPrimaryKeys.size != 1) {
			loge("主键必须只有一列")
			return emptyMap()
		}
		return keyValueMapByTable(fk.cls.sqlName, fk.cls.modelPrimaryKeys.first().userName, fcol.labelCol, w)
	}
	val ft = this.findAnnotation<FormSelectFromTable>() ?: return map
	if (ft.keyCol.isEmpty()) {
		return map
	}
	return keyValueMapByTable(ft.tableName, ft.keyCol, ft.labelCol, w)
}

fun KProperty<*>.singleSelectDisplay(v: Any): String? {
	val a = this.formOptionsMap[v.toString()]
	if (a != null) {
		return a
	}
	val fk = this.findAnnotation<ForeignKey>()
	val fcol = this.findAnnotation<ForeignLabel>()
	if (fk != null && fcol != null) {
		if (fk.cls.modelPrimaryKeys.size != 1) {
			loge("主键必须只有一列")
			return null
		}
		return findLableOfKey(fk.cls.sqlName, fk.cls.modelPrimaryKeys.first().userName, fcol.labelCol, v)
	}

	val ft = this.findAnnotation<FormSelectFromTable>() ?: return null
	return findLableOfKey(ft.tableName, ft.keyCol, ft.labelCol, v)
}

private fun findLableOfKey(tableName: String, keyCol: String, labelCol: String, keyValue: Any): String? {
	val q = SQLQuery().from(tableName).select(labelCol).limit(1).where(keyCol EQ keyValue)
	val a: Any? = ConnLook.defaultConnection.query(q).anyValue
	return a?.toString() ?: ""
}

fun Tag.datalist(id: String, block: TagCallback): Tag {
	val t = tag("datalist")
	t.id = id
	t.block()
	return t
}

fun Tag.listOption(label: String, value: String): Tag {
	return option(label, value, false)
}

fun Tag.select(block: TagCallback): Tag {
	val t = tag("select")
	t.block()
	t.needId()
	return t
}


fun Tag.optionAll(label: String = "全部"): Tag {
	val h = option("", label, false)
	h.bringToFirst()
	return h
}

fun Tag.optionNone(label: String = "无"): Tag {
	val h = option("", label, false)
	h.bringToFirst()
	return h
}

fun Tag.option(value: String, label: String, selState: Boolean): Tag {
	return if (selState) {
		option(value_ to value, selected_ to "true ") { +label }
	} else {
		option(value_ to value) { +label }
	}
}

fun Tag.option(value: String, label: String): Tag {
	val t = option(value_ to value) { +label }
	if (parent?.get(data_select_value_) == value) {
		this[selected_] = "true"
	}
	return t
}

class LinkageOption(val fromId: String, val targetId: String, val action: HttpAction) {
	var codeName: String = "code"
	var labelName: String = "label"

	var firstOptionKey: String? = ""
	var firstOptionValue: String = S.ALL
}

fun Tag.selectLinkage(fromSelect: Tag, toSelect: Tag, action: HttpAction, block: LinkageOption.() -> Unit = {}) {
	this.selectLinkage(fromSelect.needId(), toSelect.needId(), action, block)
}

fun Tag.selectLinkage(fromSelectId: String, toSelectId: String, action: HttpAction, block: LinkageOption.() -> Unit) {
	val opt = LinkageOption(fromSelectId, toSelectId, action)
	opt.block()
	selectLinkage(opt)
}

fun Tag.selectLinkage(opt: LinkageOption) {
	val updateFunName = "updateSelect_${opt.targetId}"
	val firstOption: String = if (opt.firstOptionKey != null) {
		"<option value='${opt.firstOptionKey!!}'>${opt.firstOptionValue}</option>"
	} else {
		""
	}
	val uri = httpContext.filter.actionUri(opt.action)
	val argName = opt.action.firstParamName ?: "id"
	script {
		"""
		function $updateFunName(){
			var aVal = $("#${opt.fromId}").val();
			if(!aVal){
				return ;
			}
			$.get("$uri",{$argName:aVal},function(jarr){
				var bSelect = $("#${opt.targetId}");
				var bVal = bSelect.attr("data-select-value");
				bSelect.empty();
				var firstOp = "$firstOption";
				if(firstOp.length>0){
					bSelect.append(firstOp);
				}

				for(i in jarr){
					var item = jarr[i];
					var v = item['${opt.codeName}'];
					var lb = item['${opt.labelName}'];
					var selFlag = "";
					if(v.toString() === bVal){
						selFlag = "selected";
					}
					var s = "<option value='" + v + "' " + selFlag + ">" + lb + "</option>";
					bSelect.append(s);
				}
			});
		};
		$("#${opt.fromId}").change(function(){
			$updateFunName();
		});

		$updateFunName();
		"""
	}
}

fun Tag.formGroupSelectTable(p: Prop, w: Where? = null, dontRetrive: Boolean = false, selectBlock: Tag.() -> Unit = {}): Tag {
	return formGroup {
		this.label { +p.userLabel }
		select {
			idName(p.userName)
			this[data_select_value_] = propValue(p, null) ?: ""
			if (!dontRetrive) {
				val ls = p.selectOptionsTable(w)
				for (kv in ls) {
					option(kv.key, kv.value)
				}
			}
			this.selectBlock()
		}
		this.processHelpText(p)
	}
}

fun Tag.formGroupSelectStatic(p: Prop, selectedValue: String? = null, firstLabel: String? = null): Tag {
	return formGroup {
		this.label { +p.userLabel }
		select {
			idName(p.userName)
			this[data_select_value_] = propValue(p, null) ?: ""
			if (firstLabel != null) {
				option("", firstLabel)
			}
			val ls = p.formOptionsMap
			for (kv in ls) {
				option(kv.key, kv.value)
			}
		}
		this.processHelpText(p)
	}
}


