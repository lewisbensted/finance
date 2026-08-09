import { displayMessages } from "../modal.js";
import { ErrorDTO, ValidationErrorDTO } from "../types/ApiResponse.js";
import { CustomError } from "../types/CustomError.js";
import { loginForm } from "../types/htmlForms.js";

const loginButton = document.querySelector<HTMLButtonElement>(".login-button")!;
const loginSpinner = document.querySelector<HTMLElement>(".login-spinner")!;
const loginForm = document.querySelector<loginForm>(".login-form")!;

loginForm.addEventListener("submit", async (event) => {
	event.preventDefault();
	loginButton.style.display = "none";
	loginSpinner.style.setProperty("display", "flex", "important");

	document.querySelectorAll<HTMLInputElement>("input, button").forEach((el) => (el.disabled = true));

	try {
		const res = await fetch("/api/login", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({
				username: loginForm.username.value,
				password: loginForm.password.value,
			}),
		});

		if (!res.ok) {
			const data = await res.json() as ErrorDTO;

			if ([400, 401, 429].includes(res.status) && data.code !== "MALFORMED_REQUEST") {
				if (data.code === "INVALID_REQUEST") {
					const validationData = data as ValidationErrorDTO;
					const messages = Object.values(validationData.fields).flat();
					displayMessages(messages);
				} else {
					displayMessages([data.message]);
				}
			} else {
				throw new CustomError(
					data.message || `Login failed ${res.status}`,
					res.status,
					data.code,
				);
			}

			return;
		}

		window.location.href = "/";
	} catch (error) {
		console.error(error);
		displayMessages(["An unexpected error occured - please try again"]);
	} finally {
		loginButton.style.display = "";
		loginSpinner.style.setProperty("display", "none", "important");

		document.querySelectorAll<HTMLInputElement>("input, button").forEach((el) => (el.disabled = false));
	}
});
