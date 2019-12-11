//替换location.href中的参数, 返回替换后的字符串
function searchReplaceArg(key, value) {
    var oldHref = window.location.href;
    var a = oldHref.split("?");
    if (a.length <= 1) {
        return key + "=" + value;
    }
    var newList = [key + "=" + value];
    var argList = a[1].split("&");
    for (var i = 0; i < argList.length; ++i) {
        var kv = argList[i];
        if (kv.indexOf(key + "=") != 0) {
            newList.push(kv);
        }
    }
    return newList.join("&");
}
// noinspection JSUnusedGlobalSymbols
function searchBy(key, value) {
    var s = searchReplaceArg(key, value);
    window.location.search = "?" + s;
}
function searchReplaceArg2(key, value, key2, value2) {
    var oldHref = window.location.href;
    var newList = [key + "=" + value, key2 + "=" + value2];
    var a = oldHref.split("?");
    if (a.length <= 1) {
        return newList.join("&");
    }
    var argList = a[1].split("&");
    for (var i = 0; i < argList.length; ++i) {
        var kv = argList[i];
        if ((kv.indexOf(key + "=") !== 0) && (kv.indexOf(key2 + "=") !== 0)) {
            newList.push(kv);
        }
    }
    return newList.join("&");
}
// noinspection JSUnusedGlobalSymbols
function searchBy2(key, value, key2, value2) {
    var s = searchReplaceArg2(key, value, key2, value2);
    window.location.search = "?" + s;
}
function reloadGet(reqUrl) {
    $.get(reqUrl, function () {
        window.location.reload();
    });
}
function reloadGet2(reqUrl, data) {
    $.get(reqUrl, data, function () {
        window.location.reload();
    });
}
function reloadLink(e) {
    var cs = $(e).attr("data-confirm");
    if (cs && !confirm(cs)) {
        return false;
    }
    var url = $(e).attr("data-url") || $(e).attr('href');
    $.get(url, function (resp) {
        if (resp.code === 0) {
            window.location.reload();
        }
        else {
            alert(resp.msg);
        }
    });
    return false;
}
function appendElement(ele, tagName, cls) {
    if (cls === void 0) { cls = ""; }
    var e = document.createElement(tagName);
    ele.appendChild(e);
    if (cls.length > 0) {
        e.classList.add(cls);
    }
    return e;
}
var BootDialog = /** @class */ (function () {
    function BootDialog() {
        this.title = "提示";
        this.message = "";
        this.closeText = "关闭";
        this.rightButtonText = "";
        this.rightButtonClick = function (e) {
            e.preventDefault();
        };
    }
    BootDialog.prototype.show = function () {
        var dv = appendElement(document.body, "div");
        var modal = appendElement(dv, "div", "modal");
        var modalDialog = appendElement(modal, "div", "modal-dialog");
        modalDialog.classList.add("modal-dialog-centered");
        modalDialog.setAttribute("role", "document");
        var modalContent = appendElement(modalDialog, "div", "modal-content");
        if (this.title.length > 0) {
            var modalHeader = appendElement(modalContent, "div", "modal-header");
            var h5 = appendElement(modalHeader, "h5", "modal-title");
            h5.innerHTML = this.title;
            var btnClose = appendElement(modalHeader, "button", "close");
            btnClose.setAttribute("type", "button");
            btnClose.setAttribute("data-dismiss", "modal");
            btnClose.setAttribute("aria-label", "Close");
            var sp = appendElement(btnClose, "span");
            sp.innerHTML = "&times;";
        }
        var modalBody = appendElement(modalContent, "div", "modal-body");
        modalBody.innerHTML = this.message;
        var modalFooter = appendElement(modalContent, "div", "modal-footer");
        var leftBtn = appendElement(modalFooter, "button", "btn");
        leftBtn.classList.add("btn-secondary");
        leftBtn.setAttribute("data-dismiss", "modal");
        leftBtn.textContent = this.closeText;
        if (this.rightButtonText.length > 0) {
            var rb = appendElement(modalFooter, "button", "btn");
            rb.classList.add("btn-primary");
            rb.innerHTML = this.rightButtonText;
            rb.addEventListener("click", this.rightButtonClick);
            rb.addEventListener("click", function (e) {
                e.preventDefault();
                $(modal).modal('hide');
            });
        }
        var m = $(modal);
        m.on("hidden.bs.modal", function (e) {
            dv.remove();
        });
        m.modal('show');
    };
    return BootDialog;
}());
function showAlert(msg) {
    var dlg = new BootDialog();
    dlg.message = msg;
    dlg.show();
}
function showConfirm(msg, callback) {
    var dlg = new BootDialog();
    dlg.message = msg;
    dlg.rightButtonClick = callback;
    dlg.rightButtonText = "确定";
    dlg.show();
}
