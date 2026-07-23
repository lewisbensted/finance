export class CustomError extends Error {
    httpCode;
    code;
    fields;
    constructor(message, httpCode, code, fields) {
        super(message);
        this.httpCode = httpCode;
        this.code = code;
        this.fields = fields;
    }
}
