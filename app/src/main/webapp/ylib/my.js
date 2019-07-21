//清除两边空格
String.prototype.trim = function () {
    return this.replace(/(^\s*)|(\s*$)/g, '');
};

var Yet = {
    reqReload: function (e) {
        var cs = $(e).attr("data-confirm");
        var url = $(e).attr("data-url") || $(e).attr('href');
        console.info("Url: ", url)
        if (cs) {
            if (confirm(cs)) {
                $.get(url, function (resp) {
                    console.info("Resp: ", resp)
                    if (resp.code == 0) {
                        window.location.reload();
                    } else {
                        alert(resp.msg);
                    }
                });
            }
        } else {
            $.get(url, function (resp) {
                if (resp.code == 0) {
                    window.location.reload();
                } else {
                    alert(resp.msg);
                }
            });
        }
    },
    submitDialogPanel: function (e) {
        var dlg = $(e).closest('div.modal');
        var form = dlg.find('form').first();
        var args = form.serialize();
        $.post(form.attr('action'), args, function (resp) {
            if (resp.code == 0) {
                dlg.modal('hide');
                window.location.reload();
            } else {
                alert(resp.msg);
            }
        });
    },
    openDialogPanel: function (e) {
        var u = $(e).attr('data-url') || $(e).attr('href');
        $.get(u, function (resp) {
            var p = $('#dialogPanel');
            p.html(resp);
            p.find('div').first().modal('show');
        });
        return false;
    },
    openDialogPanelChecked: function (e) {
        var u = $(e).attr('data-url') || $(e).attr('href');
        var argName = $(e).attr('data-param-name');
        if (!argName) {
            argName = "id";
        }
        var s = Yet.findCheckedIds();
        if (s.length > 0) {
            u = u + '?' + argName + '=' + s;
        } else {
            return false;
        }
        $.get(u, function (resp) {
            var p = $('#dialogPanel');
            p.html(resp);
            p.find('div').first().modal('show');
        });
        return false;
    },
    uncheckAll: function () {
        var checkAll = $('#checkall');
        checkAll.prop('checked', false);
        checkAll.closest('table').find('tbody td input:checkbox').prop('checked', false);
    },
    //返回id列表, 用逗号分割, 或者空字符串""
    findCheckedIds: function () {
        var s = "";
        $('#checkall').closest('table').find('tbody td input:checkbox').each(function (n, ele) {
            if (ele.checked) {
                if (s.length == 0) {
                    s = ele.value;
                } else {
                    s = s + "," + ele.value;
                }

            }
        });
        return s;
    },
    showAlert: function (msg) {
        var dlg = $('#alertDlgPanel').find('div').first();
        dlg.find('.modal-body').first().html(msg);
        dlg.modal('show');
    },
    showConfirm: function (msg, okCallback) {
        var dlg = $('#confirmDlgPanel').find('div').first();
        dlg.find('.modal-body').first().html(msg);
        var okBtn = dlg.find('.modal-footer').find('button').last();
        okBtn.click(function () {
            dlg.modal('hide');
            okCallback();
        });
        dlg.modal('show');
    },
    appendArg: function (s, key, value) {
        if (key && value.length > 0) {
            if (s) {
                s = s + "&" + key + "=" + value;
            } else {
                s = key + "=" + value;
            }
        }
        return s;
    },
    //joinWith("&", "a=1","b=2","c=3") => a=1&b=2&c=3
    joinWith: function () {
        var sep = arguments[0];
        var r = "";
        for (var i = 1; i < arguments.length; i++) {
            var arg = arguments[i];
            if (arg.length > 0) {
                if (r) {
                    r = r + sep + arg;
                } else {
                    r = arg;
                }
            }
        }
        return r;
    },
    queryFormId: "queryForm",
    makeQueryCond: function () {
        var fid = Yet.queryFormId.trim();
        if (!fid) {
            return "";
        }
        var ls = $('#' + fid).find('input,select,textarea');
        var q = "";
        if (ls) {
            $.each(ls, function (i, v) {
                var a = $(v).val();
                a = a.trim();
                if (v.name && a.length > 0) {
                    q = Yet.appendArg(q, v.name, a);
                }
            });
        }
        return q;
    },
    sortCol: "",
    desc: false,
    makeSortCond: function () {
        if (Yet.sortCol.length > 0) {
            if (Yet.desc) {
                return "desc_key=" + Yet.sortCol
            } else {
                return "asc_key=" + Yet.sortCol
            }
        }
        return "";
    },
    pageN: 0,
    makePageCond: function () {
        if (Yet.pageN >= -1) {
            return "p=" + Yet.pageN;
        }
        return "";
    },
    listFilter: function () {
        var qc = Yet.makeQueryCond();
        var sc = Yet.makeSortCond();
        var pc = Yet.makePageCond();
        window.location.search = Yet.joinWith("&", qc, sc, pc);
    },
    reloadGet: function (reqUrl, data) {
        $.get(reqUrl, data, function () {
            window.location.reload();
        });
    },
    reloadPost: function (reqUrl, data) {
        $.post(reqUrl, data, function () {
            window.location.reload();
        });
    },
    clickPageLink: function (e) {
        var ls = $(this).closest('ul').find('a');
        $.each(ls, function (i, v) {
            var a = $(v);
            if (a.hasClass("active")) {
                alert("" + i + "--" + a.text);
            }
        });
        return false;
    },
    createFileURL: function (file) {
        var url = null;
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
    uplodaBindDivById: function (divId) {
        var div = document.getElementById(divId);
        Yet.uplodaBindDivElement(div);
    },
    uplodaBindDivElement: function (dropDivElement) {
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
        var imgEle = $(dropDivElement).find('img');
        if (!imgEle.attr("src")) {
            var hiddenInput = $(dropDivElement).find('input[type=hidden]').first();
            if (hiddenInput.val()) {
                if (Yet.viewUrl && Yet.viewUrlParam) {
                    imgEle.attr("src", Yet.viewUrl + "?" + Yet.viewUrlParam + "=" + hiddenInput.val());
                }
            }
        }
    },
    _uploadDropHandler: function (e) {
        e.preventDefault();
        var fileList = e.dataTransfer.files;
        if (fileList.length === 0) {
            return false;
        }
        var imgEle = $(this).find('img');
        var infoSpan = $(this).find('span').first();
        var resultSpan = $(this).find('span').last();
        var progressBar = $(this).find('div.progress-bar').first();
        var hiddenInput = $(this).find('input[type=hidden]').first();


        var firstFile = fileList[0];
        var filesize = Math.floor((firstFile.size) / 1024);
        if (filesize > Yet.uploadSizeLimitM * 1024) {
            resultSpan.html("上传大小不能超过" + Yet.uploadSizeLimitM.toString() + "M");
            return false;
        }

        if (firstFile.type.indexOf('image') >= 0) {
            var img = Yet.createFileURL(firstFile);
            imgEle.attr("src", img);
        } else {
            imgEle.attr("src", Yet.uploadDefaultFileImageUrl);
        }
        infoSpan.html("名称:" + firstFile.name + " 大小:" + filesize + "KB")

        var preTime = new Date().getTime();
        var fd = new FormData();
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
                var xh = $.ajaxSettings.xhr();
                if (xh.upload) {
                    xh.upload.addEventListener('progress', function (e) {
                        var nowDate = new Date().getTime();
                        if (nowDate - preTime >= 100 || e.loaded === e.total) {
                            var percent = e.loaded * 100 / e.total;
                            console.log(percent);
                            var ps = percent.toString() + "%";
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