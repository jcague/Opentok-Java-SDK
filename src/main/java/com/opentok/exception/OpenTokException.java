package com.opentok.exception;

/**
* Defines exceptions in the OpenTok SDK.
*/
public class OpenTokException extends Exception {
	private static final long serialVersionUID = 6059658348908505724L;

    public OpenTokException(String message) {
        super(message);
    }

    public OpenTokException(String message, Throwable cause) {
        super(message, cause);
    }
}