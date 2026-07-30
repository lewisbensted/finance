import type { ErrorDTO } from "./types/ApiResponse.js";
import type { Transaction, OperationType } from "./types/Transaction.js";

export const displayToast = (
	successes: Map<string, Transaction>,
	failures: Record<string, ErrorDTO> | undefined,
	operation: OperationType,
) => {
	const title = document.getElementById("toastTitle");
	const list = document.getElementById("toastList");

	title.textContent = `${operation === "BUY" ? "Purchase" : "Sale"} Results`;
	list.replaceChildren();

	for (const transaction of successes.values()) {
		const li = document.createElement("li");
		li.className = "text-success";
		li.textContent = `✔ ${transaction.quantity} of ${transaction.symbol}`;
		list.appendChild(li);
	}

	if (failures) {
		for (const [symbol, error] of Object.entries(failures)) {
			const li = document.createElement("li");
			li.className = "text-danger";
			li.textContent = `✖ ${symbol}: ${error.message}`;
			list.appendChild(li);
		}
	}

	const toast = bootstrap.Toast.getOrCreateInstance(
		document.getElementById("transactionToast")!,
		{
			autohide: false,
		},
	);

	toast.show();
};
