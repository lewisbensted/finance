export const isErrorDTO = (value) => typeof value === "object" && value !== null && "code" in value && "message" in value;
