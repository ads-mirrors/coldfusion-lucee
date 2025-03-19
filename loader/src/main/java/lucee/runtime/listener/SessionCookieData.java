package lucee.runtime.listener;

public interface SessionCookieData extends CookieData {

	public abstract boolean isHttpOnly();

	public abstract boolean isSecure();

	public abstract String getDomain();

	public abstract short getSamesite();

	public abstract String getPath();

	public abstract boolean isPartitioned();
}