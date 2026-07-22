
const errorModal = document.querySelector(".error-modal");
const errorList = document.querySelector(".error-list");
const closeModal = document.querySelector(".close-modal");

closeModal.onclick = () => {
	errorList.innerHTML = "";
	errorModal.close();
};

export const displayMessages = (messages) => {
	messages.forEach((message) => {
		const li = document.createElement("li");
		li.textContent = message;
		errorList.appendChild(li);
	});

	errorModal.showModal();
};

document.addEventListener("DOMContentLoaded", async function () {
	const html =
		"<!DOCTYPE " +
        document.doctype.name +
        (document.doctype.publicId ? " PUBLIC \"" + document.doctype.publicId + "\"" : "") +
        (!document.doctype.publicId && document.doctype.systemId ? " SYSTEM" : "") +
        (document.doctype.systemId ? " \"" + document.doctype.systemId + "\"" : "") +
        ">\n" +
        document.documentElement.outerHTML;
	document.querySelector(
		"form[action=\"https://validator.w3.org/check\"] > input[name=\"fragment\"]"
	).value = html;
});
