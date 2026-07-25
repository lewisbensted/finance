import { ErrorDTO } from "../types/ApiResponse.js";

export const isErrorDTO = (value: unknown): value is ErrorDTO => typeof value === "object" && value !== null && "code" in value && "message" in value;
