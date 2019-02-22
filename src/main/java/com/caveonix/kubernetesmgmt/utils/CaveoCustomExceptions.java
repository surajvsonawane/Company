package com.caveonix.kubernetesmgmt.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CaveoCustomExceptions extends Exception {

	private static final long serialVersionUID = 8884492874699615488L;
	public static final Logger logger = LoggerFactory.getLogger(CaveoCustomExceptions.class);
	private final String code;

	public CaveoCustomExceptions(String message, Throwable cause, String code) {
		super(message, cause);
		this.code = code;
		logger.error(code + " : " + message, cause);
	}

	public CaveoCustomExceptions(String message, String code) {
		super(message);
		this.code = code;
		logger.error(code + " : " + message);
	}

	public CaveoCustomExceptions(Throwable cause, String code) {
		super(cause);
		this.code = code;
		logger.error(code, cause);
	}

	public String getCode() {
		return this.code;
	}
}
