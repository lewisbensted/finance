"use strict";
document.addEventListener("DOMContentLoaded", async function () {
    const html = "<!DOCTYPE " +
        document.doctype.name +
        (document.doctype.publicId ? " PUBLIC \"" + document.doctype.publicId + "\"" : "") +
        (!document.doctype.publicId && document.doctype.systemId ? " SYSTEM" : "") +
        (document.doctype.systemId ? " \"" + document.doctype.systemId + "\"" : "") +
        ">\n" +
        document.documentElement.outerHTML;
    document.querySelector("form[action=\"https://validator.w3.org/check\"] > input[name=\"fragment\"]").value = html;
});
