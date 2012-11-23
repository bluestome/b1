package android.skymobi.messenger.exception;

public class DbBussException  extends RuntimeException {

	private static final long serialVersionUID = 3583566093089790852L;

	public DbBussException() {
		super();
	}

	public DbBussException(String message) {
		super(message);
	}

	public DbBussException(Throwable cause) {
		super(cause);
	}

	public DbBussException(String message, Throwable cause) {
		super(message, cause);
	}
}


