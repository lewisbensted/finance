import { setBalanceState } from "../balance/balance.js";

const logoutButton = document.querySelector(".logout-button");
if (logoutButton) {
	logoutButton.addEventListener("click", async (e) => {
		e.preventDefault();
		try {
			const res = await fetch("/api/logout", { method: "POST" });
			if (!res.ok) throw new Error(`Logout failed: ${res.status}`);
			setBalanceState(null);
			window.location.href = "/";
		} catch (error) {
			console.error(error);
		}
	});
}
