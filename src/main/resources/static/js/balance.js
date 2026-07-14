let balance = null;

export const fetchBalance = async () => {
    const stored = sessionStorage.getItem("balance");
    if (stored !== null) {
        balance = Number(stored);
        return balance;
    }


        const res = await fetch("/api/balance");
        const data = await res.json();

        if (!res.ok) {
            if (data?.code === "UNAUTHENTICATED") {
            sessionStorage.removeItem("balance");
            balance = null
            return null};

            throw new Error(data?.message || `Error ${res.status}: ${res.statusText}`);
        }

        if (!data || typeof data.balance !== "number" || Number.isNaN(data.balance)) {
            throw new Error("Invalid or missing balance");
        }

        balance = data.balance;
        sessionStorage.setItem("balance", balance);

        return balance;
};

export const setBalance = (value) => {
    if (typeof value === "number" && !Number.isNaN(value)) {
        balance = value;
        sessionStorage.setItem("balance", value);
    } else {
        console.warn("Skipping invalid balance:", value);
    }
};

export const getBalance = () => balance;