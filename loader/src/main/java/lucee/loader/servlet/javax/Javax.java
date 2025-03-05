package lucee.loader.servlet.javax;

public interface Javax {
	public Object getJavaxInstance();

	public static javax.servlet.http.Cookie toCookie(jakarta.servlet.http.Cookie cookie) {

		if (cookie instanceof CookieJakarta) {
			return (javax.servlet.http.Cookie) ((CookieJakarta) cookie).getJavaxInstance();
		}

		javax.servlet.http.Cookie javaxCookie = new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
		javaxCookie.setMaxAge(cookie.getMaxAge());
		javaxCookie.setSecure(cookie.getSecure());
		javaxCookie.setPath(cookie.getPath());
		javaxCookie.setHttpOnly(cookie.isHttpOnly());
		if (cookie.getDomain() != null) {
			javaxCookie.setDomain(cookie.getDomain());
		}
		return javaxCookie;
	}
}
