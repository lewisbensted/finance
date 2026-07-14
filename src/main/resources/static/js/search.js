import { fetchBalance, getBalance } from "./balance.js";

const message = document.querySelector(".message");
const INTERVAL = JSON.parse(localStorage.getItem("INTERVAL"));

const balanceCell = document.querySelector(".balance");
const tradingFooter = document.querySelector(".table-footer");

		const allButtons = document.querySelectorAll("button");

        		const quoteSpinner = document.querySelector(".quote-spinner");
        		const quoteButton = document.querySelector(".quote-button");
        		const quoteTable = document.querySelector("table");
        		const quoteInput = document.querySelector(".quote-input");

        		const nameCell = document.querySelector(".name");
        		const symbolCell = document.querySelector(".symbol");
        		const priceCell = document.querySelector(".price");
        		const sharesCell = document.querySelector(".current-shares");
        		const valueCell = document.querySelector(".value");

        		const buySpinner = document.querySelector(".buy-spinner");
        		const buyButton = document.querySelector(".buy-button");

        		const sellSpinner = document.querySelector(".sell-spinner");
        		const sellButton = document.querySelector(".sell-button");

        		const buyForm = document.querySelector("#buy-form");
        		const sellForm = document.querySelector("#sell-form");

        		const buyInput = document.querySelector(".buy-input");
        		const sellInput = document.querySelector(".sell-input");

        		const buyTotal = document.querySelector(".buy-total");
        		const sellTotal = document.querySelector(".sell-total");

        		let priceIntervalId;
        		let transactionInProgress;

let balance
        		try{
                    balance = await fetchBalance();
                    if (balance === null){
                        tradingFooter.style.display = "none";
                    } else {
                            tradingFooter.style.display = "";
                            balanceCell.style.color = "";
                            balanceCell.textContent = `$${balance.toFixed(2)}`;
                    }
                } catch (error) {
                    console.error(error)
                    balanceCell.textContent = `$--`;
                    			balanceCell.style.color = "red";
                    			    tradingFooter.style.display = "";
                }

const fetchShares = async (symbol) => {
			const res = await fetch(`/api/holding?symbol=${encodeURIComponent(symbol)}`);
			const data = await res.json()
			if (!res.ok) {
			    if (data.code==="UNAUTHENTICATED") return null
			    throw new Error(data?.message || `Error ${res.status}: ${res.statusText}`);
			}

			if (data.shares == undefined) throw new Error("Missing data");
			return data.shares;
		};

		const updateShares = async (symbol) => {
			try {
				const shares = await fetchShares(symbol);
				if (shares===null) {
                    sharesCell.dataset.value = sharesCell.textContent = "";
                    				buyForm.current_shares.value = sellForm.current_shares.value = "";
                    				sellInput.max = 0;
                    					sellInput.disabled = true;
                    					sellButton.disabled = true;
                    					sellInput.min = 1;
                    					valueCell.dataset.value = valueCell.textContent = ""

                    					return null

				}
				sharesCell.dataset.value = sharesCell.textContent = shares;
				buyForm.current_shares.value = sellForm.current_shares.value = shares;
				sellInput.max = shares;
				if (shares > 0) {
					sellInput.disabled = false;
					sellButton.disabled = false;
					sellInput.min = 1;
				}
				return shares;
			} catch (error) {
				console.error(error);
			}
		};

		const fetchPrice = async (symbol) => {
			const res = await fetch(`/api/prices?symbolsStr=${encodeURIComponent(symbol)}`);
			const data = await res.json().catch(() => null)
			if (!res.ok) {
				const error = new Error(data.message  || `Error ${res.status}: ${res.statusText}`);
				error.status = res.status;
				throw error;
			}
	        const stock = data?.stocks[0]
			if (!stock || stock.symbol !== symbol || !stock.companyName)
				throw new Error("Missing or invalid data");
			const price = stock.latestPrice;
			if (typeof price !== "number" || Number.isNaN(price) || price <= 0) {
				throw new Error(`Invalid price received for holding: ${symbol}`);
			}
			return stock;
		};

		const updatePrice = async (symbol, isFirstLoad = false) => {
			try {
				const { latestPrice, companyName } = await fetchPrice(symbol);

				if (isFirstLoad) {
					nameCell.textContent = companyName;
					buyTotal.textContent = "";
					sellTotal.textContent = "";
				}
				priceCell.style.color = "";
				priceCell.dataset.value = latestPrice;
				priceCell.textContent = `$${latestPrice.toFixed(2)}`;
				sellForm.price.value = buyForm.price.value = latestPrice;
				if (balance!==null){
				const availableShares = Math.floor(balance / latestPrice);
                				buyInput.max = availableShares;

                				const available = availableShares > 0;
                				if (!transactionInProgress) buyInput.disabled = buyButton.disabled = available ? false : true;
                				buyInput.min = available ? 1 : 0;
				}

				return latestPrice;
			} catch (error) {
				console.error(error);
				if (isFirstLoad) throw error;
				priceCell.style.color = valueCell.style.color = "red";
				return null;
			}
		};

		const quoteForm = document.getElementById("quote-form");
		quoteForm.addEventListener("submit", async (e) => {
			e.preventDefault();
			const shareSymbol = document.querySelector(".quote-input").value.trim().toUpperCase();

			if (!shareSymbol) {
				message.textContent = "Invalid input - please enter a symbol.";
				return;
			}

			quoteButton.style.display = "none";
			quoteSpinner.style.setProperty("display", "flex", "important");
			quoteTable.style.display = "none";
			message.textContent = "";
			sellInput.value = buyInput.value = "";

			try {
				const [shares, sharePrice] = await Promise.all([
					updateShares(shareSymbol),
					updatePrice(shareSymbol, true),
				]);


				buyForm.symbol.value = sellForm.symbol.value = shareSymbol;
				symbolCell.textContent = shareSymbol;


                if (shares===null) {
                 valueCell.textContent = "";

                } else if (shares===undefined){
                valueCell.textContent = sharesCell.textContent = "$--";
                				valueCell.style.color = sharesCell.style.color = "red";
                } else {
                valueCell.style.color = ""
                valueCell.textContent = `$${(shares * sharePrice).toFixed(2)}`;
                }

				quoteTable.style.display = "";
				if (priceIntervalId) clearInterval(priceIntervalId);

				priceIntervalId = setInterval(() => {
					updatePrice(shareSymbol).then(() => {
						if (!transactionInProgress) {
							if (buyTotal.textContent) buyInput.dispatchEvent(new Event("input"));
							if (sellTotal.textContent) sellInput.dispatchEvent(new Event("input"));
						}
					});
				}, INTERVAL);
			} catch (error) {
			    console.error(error)
				message.textContent = error?.status == 422 ? "Symbol not found" : "Unexpected error";
			} finally {
				quoteButton.style.display = "";
				quoteSpinner.style.setProperty("display", "none", "important");
			}
		});
