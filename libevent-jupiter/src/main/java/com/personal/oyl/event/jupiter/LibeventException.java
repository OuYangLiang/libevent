package com.personal.oyl.event.jupiter;

/**
 * @author OuYang Liang
 */
public class LibeventException extends Exception {
    public LibeventException() {
    }

    public LibeventException(String message) {
        super(message);
    }

    public LibeventException(String message, Throwable cause) {
        super(message, cause);
    }

    public LibeventException(Throwable cause) {
        super(cause);
    }

    public LibeventException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
