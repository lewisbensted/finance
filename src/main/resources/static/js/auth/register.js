import { displayMessages } from "../modal.js";
import { CustomError } from "../types/CustomError.js";
const registerButton = document.querySelector(".register-button");
const registerSpinner = document.querySelector(".register-spinner");
const registerForm = document.querySelector(".register-form");
registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    registerButton.style.display = "none";
    registerSpinner.style.setProperty("display", "flex", "important");
    document.querySelectorAll("input, button").forEach((el) => (el.disabled = true));
    try {
        const res = await fetch("/api/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                firstName: registerForm.username.value,
                lastName: registerForm.lastName.value,
                email: registerForm.email.value,
                username: registerForm.username.value,
                password: registerForm.password.value,
                passwordRepeat: registerForm.passwordRepeat.value,
            }),
        });
        if (!res.ok) {
            const data = (await res.json());
            if ([400, 409, 429].includes(res.status) && data.code !== "MALFORMED_REQUEST") {
                if (data.code === "INVALID_REQUEST") {
                    const messages = Object.values(data.fields).flat();
                    displayMessages(messages);
                }
                else {
                    displayMessages([data.message]);
                }
            }
            else {
                throw new CustomError(data.message || `Register failed ${res.status}`, res.status, data.code);
            }
            return;
        }
        registerButton.textContent = "Registration successful, redirecting...";
        setTimeout(() => (window.location.href = "/login"), 1200);
    }
    catch (error) {
        console.error(error);
        displayMessages(["Unexpected error"]);
    }
    finally {
        registerButton.style.display = "";
        registerSpinner.style.setProperty("display", "none", "important");
        document.querySelectorAll("input, button").forEach((el) => (el.disabled = false));
    }
});
