package com.hims.exception;

public class SlotAlreadyBookedException extends RuntimeException {

    public SlotAlreadyBookedException() {
        super();
    }

    public SlotAlreadyBookedException(String message) {
        super(message);
    }

    public SlotAlreadyBookedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SlotAlreadyBookedException(Throwable cause) {
        super(cause);
    }
}
