package dev.entao.kage.widget

import dev.entao.kage.Tag
import dev.entao.kava.base.userLabel
import dev.entao.kava.base.userName
import dev.entao.ken.anno.*
import dev.entao.ken.HttpAction
import dev.entao.kava.base.Prop
import dev.entao.kava.base.Prop0
import dev.entao.kage.S
import dev.entao.sql.Where
import dev.entao.kava.base.firstParamName
import dev.entao.kava.base.getValue
import dev.entao.kage.TagCallback
import dev.entao.kage.div
import dev.entao.kage.label
import dev.entao.kage.scriptBlock
import kotlin.reflect.full.findAnnotation

fun Tag.datalist(id: String, block: TagCallback): Tag {
	val t = addTag("datalist")
	t.id = id
	t.block()
	return t
}

fun Tag.listOption(label: String, value: String): Tag {
	val t = addTag("option")
	t.label = label
	t.value = value
	return t
}

fun Tag.select(block: TagCallback): Tag {
	val t = addTag("select")
	t.block()
	t.needId()
	return t
}

fun Tag.option(block: TagCallback): Tag {
	val t = addTag("option")
	t.block()
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
	return option {
		this.value = value
		+label
		if (selState) {
			this.selected = true
		}
	}
}

fun Tag.option(value: String, label: String): Tag {
	return option {
		this.value = value
		+label
		val v = parentTag?.dataSelectValue == value
		if (v) {
			this.selected = true
		}
	}
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
	val uri = httpContext.path.action(opt.action).uri
	val argName = opt.action.firstParamName ?: "id"
	scriptBlock {
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

fun Tag.labelSelectRowFromTable(p: Prop, w: Where? = null, dontRetrive: Boolean = false, selectBlock: Tag.() -> Unit = {}): Tag {
	var selTag: Tag? = null
	formGroupRow {
		val pname = p.userName
		val selVal: String = if (p is Prop0) {
			p.getValue()?.toString() ?: ""
		} else {
			httpContext.httpParams.str(p.userName) ?: ""
		}

		this.label { +p.userLabel }
		this.div {
			selTag = select {
				idName(pname)
				this.dataSelectValue = selVal
				if (!dontRetrive) {
					val ls = p.selectOptionsTable(w)
					for (kv in ls) {
						option(kv.key, kv.value)
					}
				}
				this.selectBlock()
			}
			val hb = p.findAnnotation<FormHelpBlock>()?.value
			if (hb != null && hb.isNotEmpty()) {
				formTextMuted(hb)
			}
		}
	}
	return selTag!!
}

fun Tag.labelSelectRowStatic(p: Prop, selectedValue: String? = null, selectBlock: Tag.() -> Unit = {}): Tag {
	var selTag: Tag? = null
	formGroupRow {
		val pname = p.userName
		val selVal = if (p is Prop0) {
			p.getValue()?.toString() ?: selectedValue
		} else {
			selectedValue
		} ?: httpContext.httpParams.str(p.userName)

		this.label { +p.userLabel }
		this.div {
			selTag = select {
				idName(pname)
				this.dataSelectValue = selVal ?: ""
				val ls = p.formOptionsMap
				for (kv in ls) {
					option(kv.key, kv.value)
				}
				this.selectBlock()
			}
			val hb = p.findAnnotation<FormHelpBlock>()?.value
			if (hb != null && hb.isNotEmpty()) {
				formTextMuted(hb)
			}
		}
	}
	return selTag!!
}

fun Tag.labelRadioRowStatic(p: Prop, selectedValue: String? = null) {
	formGroupRow {
		val pname = p.userName
		var selVal: String? = if (p is Prop0) {
			p.getValue()?.toString() ?: selectedValue
		} else {
			selectedValue
		} ?: httpContext.httpParams.str(p.userName)

		this.label { +p.userLabel }
		this.div {
			val ls = p.formOptionsMap
			ls.forEach { opt ->
				formCheck {
					addClass("form-check-inline")
					val r = radio {
						name = pname
						value = opt.key
						if (selVal == null) {
							checked = true
							selVal = this.value
						} else if (selVal == opt.key) {
							checked = true
						}
					}
					label(opt.value).forId = r.needId()
				}
			}

			val hb = p.findAnnotation<FormHelpBlock>()?.value
			if (hb != null && hb.isNotEmpty()) {
				formTextMuted(hb)
			}
		}
	}
}