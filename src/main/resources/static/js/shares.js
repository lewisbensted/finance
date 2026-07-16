const sellButton = document.querySelector(".sell-button");

const buyForm = document.querySelector("#buy-form");
const sellForm = document.querySelector("#sell-form");




const fetchShares = async (symbol) => {
	const res = await fetch(`/api/holding?symbol=${encodeURIComponent(symbol)}`);
	const data = await res.json()
	if (!res.ok) {
		if (data.code === "UNAUTHENTICATED") return null
		if (data.code === "NOT_FOUND") return 0;
		throw new Error(data?.message || `Error ${res.status}: ${res.statusText}`);
	}

	if (data.shares == undefined) throw new Error("Missing data");
	return data.shares;
};

export const updateShares = async (holding) => {
	try {
		const symbol = holding.symbol;
		const shares = await fetchShares(symbol);
		if (shares === null) {
			holding.shares = 0;
			holding.sharesCell.textContent = "";
			buyForm.current_shares.value = sellForm.current_shares.value = "";
			holding.sellInput.max = 0;
			holding.sellInput.disabled = sellButton.disabled = true;
			holding.sellInput.min = 1;
			holding.valueCell.dataset.value = holding.valueCell.textContent = ""
			holding.value = 0;

			return null
		}
		holding.sharesCell.dataset.value = holding.sharesCell.textContent = shares;
		buyForm.current_shares.value = sellForm.current_shares.value = shares;
		holding.sellInput.max = shares;
		holding.shares = shares
		if (shares > 0) {
			holding.sellInput.disabled = false;
			sellButton.disabled = false;
			holding.sellInput.min = 1;
		}
		return shares;
	} catch (error) {
		console.error(error);
	}
};