const messages = document.querySelector(".messages");

const displayMessage = (text) => {
    const div = document.createElement("div");
    div.className = "message";
    div.textContent = text;
    messages.appendChild(div);
};

const loginButton = document.querySelector(".login-button");
const loginSpinner = document.querySelector(".login-spinner");
const loginForm = document.querySelector(".login-form");

if (loginForm) {
loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    loginButton.style.display = "none";
    loginSpinner.style.setProperty("display", "flex", "important");

    document
        .querySelectorAll("input, button")
        .forEach((el) => (el.disabled = true));

    messages.textContent = "";

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
            const data = await res.json();

            if (
                [400, 401, 429].includes(res.status) &&
                data.code !== "MALFORMED_REQUEST"
            ) {
                console.error(data);

                if (data.code === "INVALID_REQUEST") {
                    Object.values(data.fields)
                        .flat()
                        .forEach(displayMessage);
                } else {
                    displayMessage(data.message);
                }
            } else {
                throw new Error(
                    data?.message ||
                    `Error ${res.status}: ${res.statusText}`
                );
            }

            return;
        }

        window.location.href = "/";
    } catch (error) {
        console.error(error);
        displayMessage("Unexpected error");
    } finally {
        loginButton.style.display = "";
        loginSpinner.style.setProperty("display", "none", "important");

        document
            .querySelectorAll("input, button")
            .forEach((el) => (el.disabled = false));
    }
});}


        const logoutButton = document.querySelector(".logout-button");
        if (logoutButton) {
            logoutButton.addEventListener("click", async (e) => {
                e.preventDefault();
                try {
                    const res = await fetch("api/logout", { method: "POST" });
                    if (!res.ok) throw new Error(`Logout failed: ${res.status}`);
                    sessionStorage.removeItem("balance");
                    console.log(sessionStorage.getItem("balance"));
                    window.location.href = "/";
                } catch (error) {
                    console.error(error);
                }
            });
        }

	const registerButton = document.querySelector(".register-button");
	const registerSpinner = document.querySelector(".register-spinner");
	const registerForm = document.querySelector(".register-form");
	if(registerForm){
	registerForm.addEventListener("submit", async (event) => {
		event.preventDefault();
		registerButton.style.display = "none";
		registerSpinner.style.setProperty("display", "flex", "important");
		document.querySelectorAll("input, button").forEach((el) => (el.disabled = true));
		messages.textContent = "";
		try {
			const res = await fetch("/api/register", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					username: registerForm.username.value,
					password: registerForm.password.value,
					confirmation: registerForm.confirmation.value,
				}),
			});
			if (!res.ok) {
				const data = await res.json();
				if ([400, 409, 429].includes(res.status) && data.error !== "MALFORMED_REQUEST") {
					if (data.code === "INVALID_REQUEST") {
                                        Object.values(data.fields)
                                            .flat()
                                            .forEach(displayMessage);
                                    } else {
                                        displayMessage(data.message);
                                    }
				} else {
					throw new Error(data?.error || `Error ${res.status}: ${res.statusText}`);
				}
				return;
			}
			message.textContent = "Registration successful, redirecting...";
			setTimeout(() => (window.location.href = "/login"), 1200);
		} catch (error) {
			console.error(error);
			displayMessage("Unexpected error");
		} finally {
			registerButton.style.display = "";
			registerSpinner.style.setProperty("display", "none", "important");
			document.querySelectorAll("input, button").forEach((el) => (el.disabled = false));
		}})}