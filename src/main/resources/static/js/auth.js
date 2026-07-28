import { setBalance } from "./balance/balance.js";
import { displayMessages } from "./layout.js";
import { CustomError } from "./types/CustomError.js";
const loginButton = document.querySelector(".login-button");
const loginSpinner = document.querySelector(".login-spinner");
const loginForm = document.querySelector(".login-form");
if (loginForm) {
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
                if ([400, 401, 429].includes(res.status) &&
                    data.error?.code !== "MALFORMED_REQUEST") {
                    console.error(data);
                    if (data.error.code === "INVALID_REQUEST") {
                        const messages = Object.values(data.error?.fields).flat();
                        displayMessages(messages);
                    }
                    else {
                        displayMessages([data.error?.message]);
                    }
                }
                else {
                    throw new CustomError(data.error.message || `Login failed ${res.status}`, res.status, data.error?.code);
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
}
const logoutButton = document.querySelector(".logout-button");
console.log(logoutButton);
if (logoutButton) {
    logoutButton.addEventListener("click", async (e) => {
        e.preventDefault();
        try {
            const res = await fetch("/api/logout", { method: "POST" });
            if (!res.ok)
                throw new Error(`Logout failed: ${res.status}`);
            setBalance(null);
            window.location.href = "/";
        }
        catch (error) {
            console.error(error);
        }
    });
}
const registerButton = document.querySelector(".register-button");
const registerSpinner = document.querySelector(".register-spinner");
const registerForm = document.querySelector(".register-form");
if (registerForm) {
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
                const data = await res.json();
                if ([400, 409, 429].includes(res.status) && data.error !== "MALFORMED_REQUEST") {
                    if (data.code === "INVALID_REQUEST") {
                        const messages = Object.values(data.fields).flat();
                        displayMessages(messages);
                    }
                    else {
                        displayMessages([data.message]);
                    }
                }
                else {
                    throw new Error(data?.error ?? `Error ${res.status}: ${res.statusText}`);
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
}
