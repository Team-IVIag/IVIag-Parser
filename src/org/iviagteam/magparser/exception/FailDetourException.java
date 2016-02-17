package org.iviagteam.magparser.exception;

public class FailDetourException extends Exception{
	
	private static final long serialVersionUID = -3854111688048655115L;

	public FailDetourException() {
	}
	
	public FailDetourException(String str) {
		super(str);
	}
	
	public FailDetourException(Throwable cause) {
		super(cause);
	}
	
	public FailDetourException(String str, Throwable cause) {
		super(str, cause);
	}
}
