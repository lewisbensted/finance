
const errorModalElement = document.querySelector(".error-modal");
const errorList = document.querySelector(".error-list");
const closeModal = document.querySelector(".close-modal");

const errorModal = new bootstrap.Modal(errorModalElement);

closeModal.onclick = () => {
	errorList.innerHTML = "";
	errorModal.hide();
};

export const displayMessages = (messages) => {
	messages.forEach((message) => {
		const li = document.createElement("li");
		li.textContent = message;
		errorList.appendChild(li);
	});

	errorModal.show();
};