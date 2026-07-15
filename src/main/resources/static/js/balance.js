let balance = null;

export const fetchBalance = async () => {
    const res = await fetch("/api/balance");
    const data = await res.json();

    if (!res.ok) {
        if (data?.code === "UNAUTHENTICATED") {
            sessionStorage.removeItem("USER_SESSION");
            balance = null
            return null
        };

        throw new Error(data?.message || `Error ${res.status}: ${res.statusText}`);
    }

    if (typeof data !== "number" || Number.isNaN(data)) {
        throw new Error("Invalid or missing balance");
    }

    balance = data;
    return balance;
};

export const setBalance = (value) => {
    if (typeof value === "number" && !Number.isNaN(value)) {
        balance = value;
    } else {
        console.warn("Skipping invalid balance:", value);
    }
};

export const getBalance = () => balance;