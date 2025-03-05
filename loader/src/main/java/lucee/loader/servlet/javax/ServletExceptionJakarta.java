package lucee.loader.servlet.javax;

import jakarta.servlet.ServletException;

public class ServletExceptionJakarta extends ServletException implements Javax {
	private static final long serialVersionUID = -109825224091435598L;
	private javax.servlet.ServletException exp;

	public ServletExceptionJakarta(javax.servlet.ServletException exp) {
		super(exp);
		if (exp == null) throw new NullPointerException();
		this.exp = exp;
	}

	@Override
	public Object getJavaxInstance() {
		return exp;
	}

}
