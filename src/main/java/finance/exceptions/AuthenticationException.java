package finance.exceptions;

import finance.dtos.ErrorCode;

public class AuthenticationException extends RuntimeException {
    private final ErrorCode code;
    public AuthenticationException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }
    public ErrorCode getCode() {
        return code;
    }
}
