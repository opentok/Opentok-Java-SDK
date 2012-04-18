package com.opentok.exception;

public class OpenTokException extends Exception {
	private static final long serialVersionUID = 6059658348908505724L;

	public OpenTokException(String err) {
		super(err);
	}
}