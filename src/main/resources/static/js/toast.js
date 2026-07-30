export const displayToast = (successes, failures, operation) => {
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
    const toast = bootstrap.Toast.getOrCreateInstance(document.getElementById("transactionToast"), {
        autohide: false,
    });
    toast.show();
    console.log("This will be converted into a Toast");
    if (successes.size > 0)
        console.log(`Successesful ${operation}:`);
    for (const transaction of successes.values())
        console.log(`${transaction.quantity} of ${transaction.symbol}`);
    if (failures && Object.keys(failures).length > 0) {
        console.log(`Unuccessesful ${operation}:`);
        for (const [symbol, itemError] of Object.entries(failures)) {
            console.warn(`Failed transaction ${symbol}: ${itemError.message}`);
            console.log(`${symbol}: ${itemError.message}`);
        }
    }
};
