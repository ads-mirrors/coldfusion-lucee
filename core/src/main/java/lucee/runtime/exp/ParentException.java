package lucee.runtime.exp;

public final class ParentException extends Exception {
	private static final long serialVersionUID = 3949948965230342458L;

	public ParentException() {
		super("parent thread stacktrace");
	}
}
