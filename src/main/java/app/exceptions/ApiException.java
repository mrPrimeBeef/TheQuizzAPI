package app.exceptions;

public class ApiException extends RuntimeException {
    private int code;

    public ApiException(int code, String msg, Exception e) {
        super(msg, e);
        this.code = code;
    }

    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public ApiException(String msg, Exception e) {
        super(msg);
    }

    public int getCode() {
        return code;
    }
}
