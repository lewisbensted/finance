import { displayMessages } from "../modal.js";
import { CustomError } from "../types/CustomError.js";
const loginButton = document.querySelector(".login-button");
const loginSpinner = document.querySelector(".login-spinner");
const loginForm = document.querySelector(".login-form");
loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    loginButton.style.display = "none";
    loginSpinner.style.setProperty("display", "flex", "important");
    document.querySelectorAll("input, button").forEach((el) => (el.disabled = true));
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
            const data = (await res.json());
            if ([400, 401, 429].includes(res.status) && data.code !== "MALFORMED_REQUEST") {
                console.error(data);
                if (data.code === "INVALID_REQUEST") {
                    const messages = Object.values(data.fields).flat();
                    displayMessages(messages);
                }
                else {
                    displayMessages([data.message]);
                }
            }
            else {
                throw new CustomError(data.message || `Login failed ${res.status}`, res.status, data.code);
            }
            return;
        }
        window.location.href = "/";
    }
    catch (error) {
        console.error(error);
        displayMessages(["Unexpected error"]);
    }
    finally {
        loginButton.style.display = "";
        loginSpinner.style.setProperty("display", "none", "important");
        document.querySelectorAll("input, button").forEach((el) => (el.disabled = false));
    }
});
