//替换location.href中的参数, 返回替换后的字符串
function searchReplaceArg(key: string, value: string): string {
    let oldHref: string = window.location.href;
    let a: Array<string> = oldHref.split("?");
    if (a.length <= 1) {
        return key + "=" + value;
    }
    let newList: Array<string> = [key + "=" + value];
    let argList: Array<string> = a[1].split("&");
    for (let i = 0; i < argList.length; ++i) {
        let kv: string = argList[i];
        if (kv.indexOf(key + "=") != 0) {
            newList.push(kv);
        }
    }
    return newList.join("&");
}

// noinspection JSUnusedGlobalSymbols
function searchBy(key: string, value: string) {
    let s = searchReplaceArg(key, value);
    window.location.search = "?" + s;
}

function searchReplaceArg2(key: string, value: string, key2: string, value2: string): string {
    let oldHref: string = window.location.href;
    let newList: Array<string> = [key + "=" + value, key2 + "=" + value2];
    let a: Array<string> = oldHref.split("?");
    if (a.length <= 1) {
        return newList.join("&");
    }

    let argList: Array<string> = a[1].split("&");
    for (let i = 0; i < argList.length; ++i) {
        let kv: string = argList[i];
        if ((kv.indexOf(key + "=") !== 0) && (kv.indexOf(key2 + "=") !== 0)) {
            newList.push(kv);
        }
    }
    return newList.join("&");
}

// noinspection JSUnusedGlobalSymbols
function searchBy2(key: string, value: string, key2: string, value2: string) {
    let s = searchReplaceArg2(key, value, key2, value2);
    window.location.search = "?" + s;
}

function reloadGet(reqUrl: string) {
    $.get(reqUrl, function () {
        window.location.reload();
    });
}

function reloadGet2(reqUrl: string, data: string) {
    $.get(reqUrl, data, function () {
        window.location.reload();
    });
}


function reloadLink(e: any): boolean {
    let cs = $(e).attr("data-confirm");
    if (cs && !confirm(cs)) {
        return false
    }
    let url = $(e).attr("data-url") || $(e).attr('href');
    $.get(url, function (resp) {
        if (resp.code === 0) {
            window.location.reload();
        } else {
            alert(resp.msg);
        }
    });
    return false;
}

function appendElement(ele: HTMLElement, tagName: string, cls: string = ""): HTMLElement {
    let e = document.createElement(tagName);
    ele.appendChild(e);
    if (cls.length > 0) {
        e.classList.add(cls);
    }
    return e
}

class BootDialog {
    title: string;
    message: string;
    closeText: string;
    rightButtonText: string;
    rightButtonClick: EventListenerOrEventListenerObject;

    constructor() {
        this.title = "提示";
        this.message = "";
        this.closeText = "关闭";
        this.rightButtonText = "";
        this.rightButtonClick = function (e) {
            e.preventDefault();
        }
    }

    show() {
        let dv = appendElement(document.body, "div");
        let modal = appendElement(dv, "div", "modal");
        let modalDialog = appendElement(modal, "div", "modal-dialog");
        modalDialog.classList.add("modal-dialog-centered");
        modalDialog.setAttribute("role", "document");
        let modalContent = appendElement(modalDialog, "div", "modal-content");
        if (this.title.length > 0) {
            let modalHeader = appendElement(modalContent, "div", "modal-header");
            let h5 = appendElement(modalHeader, "h5", "modal-title");
            h5.innerHTML = this.title;
            let btnClose = appendElement(modalHeader, "button", "close");
            btnClose.setAttribute("type", "button");
            btnClose.setAttribute("data-dismiss", "modal");
            btnClose.setAttribute("aria-label", "Close");
            let sp = appendElement(btnClose, "span")
            sp.innerHTML = "&times;"
        }
        let modalBody = appendElement(modalContent, "div", "modal-body");
        modalBody.innerHTML = this.message;
        let modalFooter = appendElement(modalContent, "div", "modal-footer");
        let leftBtn = appendElement(modalFooter, "button", "btn");
        leftBtn.classList.add("btn-secondary");
        leftBtn.setAttribute("data-dismiss", "modal");
        leftBtn.textContent = this.closeText;

        if (this.rightButtonText.length > 0) {
            let rb = appendElement(modalFooter, "button", "btn");
            rb.classList.add("btn-primary");
            rb.innerHTML = this.rightButtonText;
            rb.addEventListener("click", this.rightButtonClick);
            rb.addEventListener("click", function (e) {
                e.preventDefault();
                $(modal).modal('hide');
            });
        }

        let m = $(modal);
        m.on("hidden.bs.modal", function (e) {
            dv.remove();
        });
        m.modal('show');
    }
}

function showAlert(msg: string) {
    let dlg = new BootDialog();
    dlg.message = msg;
    dlg.show();
}

function showConfirm(msg: string, callback: EventListenerOrEventListenerObject) {
    let dlg = new BootDialog();
    dlg.message = msg;
    dlg.rightButtonClick = callback;
    dlg.rightButtonText = "确定";
    dlg.show();
}


