//清除两边空格
String.prototype.trim = function () {
	return this.replace(/(^\s*)|(\s*$)/g, '');
};

var Yet = {
	createFileURL: function (file) {
		let url = null;
		if (window.createObjectURL !== undefined) { // basic
			url = window.createObjectURL(file);
		} else if (window.URL !== undefined) { // mozilla(firefox)
			url = window.URL.createObjectURL(file);
		} else if (window.webkitURL !== undefined) { // webkit or chrome
			url = window.webkitURL.createObjectURL(file);
		}
		return url;
	},
	uploadUrl: "",
	viewUrl: "",
	viewUrlParam: "id",
	uploadSizeLimitM: 20,
	uploadDefaultFileImageUrl: "http://app800.cn/i/file.png",
	onUploadOK: function (data) {

	},
	uploadBindDivById: function (divId) {
		let div = document.getElementById(divId);
		Yet.uploadBindDivElement(div);
	},
	uploadBindDivElement: function (dropDivElement) {
		$(document).on({
			dragleave: function (e) {    //拖离
				e.preventDefault();
			},
			drop: function (e) {  //拖后放
				e.preventDefault();
			},
			dragenter: function (e) {    //拖进
				e.preventDefault();
			},
			dragover: function (e) {    //拖来拖去
				e.preventDefault();
			}
		});
		dropDivElement.addEventListener("drop", Yet._uploadDropHandler, false);
		let imgEle = $(dropDivElement).find('img');
		if (!imgEle.attr("src")) {
			let hiddenInput = $(dropDivElement).find('input[type=hidden]').first();
			if (hiddenInput.val()) {
				if (Yet.viewUrl && Yet.viewUrlParam) {
					imgEle.attr("src", Yet.viewUrl + "?" + Yet.viewUrlParam + "=" + hiddenInput.val());
				}
			}
		}
	},
	_uploadDropHandler: function (e) {
		e.preventDefault();
		let fileList = e.dataTransfer.files;
		if (fileList.length === 0) {
			return false;
		}
		let imgEle = $(this).find('img');
		let infoSpan = $(this).find('span').first();
		let resultSpan = $(this).find('span').last();
		let progressBar = $(this).find('div.progress-bar').first();
		let hiddenInput = $(this).find('input[type=hidden]').first();


		let firstFile = fileList[0];
		let filesize = Math.floor((firstFile.size) / 1024);
		if (filesize > Yet.uploadSizeLimitM * 1024) {
			resultSpan.html("上传大小不能超过" + Yet.uploadSizeLimitM.toString() + "M");
			return false;
		}

		if (firstFile.type.indexOf('image') >= 0) {
			let img = Yet.createFileURL(firstFile);
			imgEle.attr("src", img);
		} else {
			imgEle.attr("src", Yet.uploadDefaultFileImageUrl);
		}
		infoSpan.html("名称:" + firstFile.name + " 大小:" + filesize + "KB")

		let preTime = new Date().getTime();
		let fd = new FormData();
		fd.append('file', firstFile);
		$.ajax({
			url: Yet.uploadUrl,
			type: "POST",
			data: fd,
			processData: false,
			contentType: false,
			headers: {
				Cookie: document.cookie,
				Accept: "application/json; charset=utf-8"
			},
			xhr: function xhr() {
				let xh = $.ajaxSettings.xhr();
				if (xh.upload) {
					xh.upload.addEventListener('progress', function (e) {
						let nowDate = new Date().getTime();
						if (nowDate - preTime >= 100 || e.loaded === e.total) {
							let percent = e.loaded * 100 / e.total;
							console.log(percent);
							let ps = percent.toString() + "%";
							progressBar.css("width", ps);
						}
						preTime = nowDate;
					});
				}
				return xh;
			},
			success: function (data) {
				if (data.code === 0) {
					resultSpan.html("上传成功");
					resultSpan.removeClass('text-danger');
					resultSpan.addClass('text-success');
					hiddenInput.val(data.data);
					if (Yet.viewUrl && Yet.viewUrlParam) {
						imgEle.attr("src", Yet.viewUrl + "?" + Yet.viewUrlParam + "=" + data.data);
					}
					Yet.onUploadOK(data.data);
				} else {
					resultSpan.html("上传失败: " + data.msg);
					resultSpan.removeClass('text-success');
					resultSpan.addClass('text-danger');
				}
			},
			error: function (err) {
				resultSpan.html("上传失败");
				resultSpan.removeClass('text-success');
				resultSpan.addClass('text-danger');
			}
		});
	}
};

window.yet = Yet;

function tableCheckedIds(tableId) {
	let ls = [];
	$('#' + tableId).find('tbody td input:checkbox').each(function (n, ele) {
		if (ele.checked) {
			ls.push(ele.value);
		}
	});
	return ls.join(",");
}



function tableUncheckAll(tableId) {
	let tb = $('#' + tableId);
	tb.find('thead td input:checkbox').prop('checked', false);
	tb.find('tbody td input:checkbox').prop('checked', false);
}


function tableCheckReload(btnEle, tableId) {
	let s = tableCheckedIds(tableId);
	let btn = $(btnEle);
	if (s.length > 0) {
		let cs = btn.attr("data-confirm");
		let url = btn.attr("data-url") || btn.attr("href");
		let argName = btn.attr("data-param-name");
		if (!cs || confirm(cs)) {
			tableUncheckAll(tableId);
			$.get(url, argName + '=' + s, function () {
				window.location.reload();
			});
		}
	}
	return false;
}

function tableCheckOpenDialog(btnEle, tableId) {
	let s = tableCheckedIds(tableId);
	let btn = $(btnEle);
	if (s.length > 0) {
		let cs = btn.attr("data-confirm");
		let url = btn.attr("data-url") || btn.attr("href");
		let argName = btn.attr("data-param-name");
		if (!cs || confirm(cs)) {
			tableUncheckAll(tableId);
			$.get(url, argName + '=' + s, function (resp) {
				__showModal(resp);
			});
		}
	}
	return false;
}


function dialogSubmitReload(e) {
	let dlg = $(e).closest('div.modal');
	let form = dlg.find('form').first();
	let args = form.serialize();
	if (form.attr('method').toUpperCase() === 'GET') {
		$.get(form.attr('action'), args, function (resp) {
			if (resp.code === 0) {
				dlg.modal('hide');
				window.location.reload();
			} else {
				alert(resp.msg);
			}
		});
	} else {
		$.post(form.attr('action'), args, function (resp) {
			if (resp.code === 0) {
				dlg.modal('hide');
				window.location.reload();
			} else {
				alert(resp.msg);
			}
		});
	}
}


function openDialogPanel(e) {
	let btn = $(e);
	let u = btn.attr('data-url') || btn.attr('href');
	let cs = btn.attr("data-confirm");
	if (!cs || confirm(cs)) {
		$.get(u, function (resp) {
			__showModal(resp);
		});
	}
	return false;
}

function __showModal(resp) {
	let dv = document.createElement("div");
	document.body.appendChild(dv);
	let p = $(dv);
	p.html(resp);
	let md = p.find('div.modal');
	md.on('hidden.bs.modal', function (e) {
		dv.remove();
	});
	md.modal('show');
}


