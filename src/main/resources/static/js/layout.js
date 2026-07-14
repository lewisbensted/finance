
        localStorage.setItem("INTERVAL", 3000);

        window.balance = null;

        window.fetchBalance = async () => {
            const stored = sessionStorage.getItem("balance");
            if (stored !== null) return Number(stored);

            try {
                const res = await fetch("/balance");
                const data = await res.json();
                if (!res.ok) throw new Error(`Error ${res.status}: ${res.statusText}`);
                if (!data || typeof data?.balance != "number" || Number.isNaN(data.balance))
                    throw new Error("Invalid or missing balance");

                sessionStorage.setItem("balance", data.balance);
                return data.balance;
            } catch (error) {
                console.error(error);
                return null;
            }
        };

        window.setBalance = (balance) => {
            if (typeof balance === "number" && !Number.isNaN(balance)) {
                sessionStorage.setItem("balance", balance);
            } else {
                console.warn("Skipping invalid balance:", balance);
            }
        };

        document.addEventListener("DOMContentLoaded", async function () {
            const html =
                "<!DOCTYPE " +
                document.doctype.name +
                (document.doctype.publicId ? ' PUBLIC "' + document.doctype.publicId + '"' : "") +
                (!document.doctype.publicId && document.doctype.systemId ? " SYSTEM" : "") +
                (document.doctype.systemId ? ' "' + document.doctype.systemId + '"' : "") +
                ">\n" +
                document.documentElement.outerHTML;
            document.querySelector(
                'form[action="https://validator.w3.org/check"] > input[name="fragment"]'
            ).value = html;
        });
