export class CustomError extends Error {
    httpCode;
    code;
    constructor(message, httpCode, code) {
        super(message);
        this.httpCode = httpCode;
        this.code = code;
        this.httpCode = httpCode;
        this.code = code;
    }
}
