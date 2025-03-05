package lucee.loader.servlet.javax;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class RequestDispatcherJakarta implements RequestDispatcher {

	private javax.servlet.RequestDispatcher dispatcher;

	public RequestDispatcherJakarta(javax.servlet.RequestDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		javax.servlet.ServletRequest req = (javax.servlet.ServletRequest) ((ServletRequestJakarta) request).getJavaxInstance();
		javax.servlet.ServletResponse rsp = (javax.servlet.ServletResponse) ((ServletResponseJakarta) response).getJavaxInstance();
		try {
			dispatcher.forward(req, rsp);
		}
		catch (javax.servlet.ServletException se) {
			new ServletExceptionJakarta(se);
		}
	}

	@Override
	public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		javax.servlet.ServletRequest req = (javax.servlet.ServletRequest) ((ServletRequestJakarta) request).getJavaxInstance();
		javax.servlet.ServletResponse rsp = (javax.servlet.ServletResponse) ((ServletResponseJakarta) response).getJavaxInstance();
		try {
			dispatcher.include(req, rsp);
		}
		catch (javax.servlet.ServletException se) {
			new ServletExceptionJakarta(se);
		}
	}
}