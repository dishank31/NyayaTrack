package exception;
 
/**
 * Custom exception for invalid case operations.
 * Thrown when business rules are violated.
 */
public class InvalidCaseException extends Exception {
 
    private final int errorCode;
 
    // Error code constants
    public static final int ERR_NULL_TITLE      = 1001;
    public static final int ERR_INVALID_STATUS  = 1002;
    public static final int ERR_DUPLICATE_CASE  = 1003;
    public static final int ERR_NO_JUDGE        = 1004;
    public static final int ERR_PAST_DATE       = 1005;
 
    public InvalidCaseException(String message) {
        super(message);
        this.errorCode = 0;
    }
 
    public InvalidCaseException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
 
    public InvalidCaseException(String message, int errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
 
    public int getErrorCode() { return errorCode; }
 
    @Override
    public String toString() {
        return "InvalidCaseException[code=" + errorCode + "]: " + getMessage();
    }
}
