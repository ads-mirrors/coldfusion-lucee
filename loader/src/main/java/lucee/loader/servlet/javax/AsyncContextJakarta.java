package lucee.loader.servlet.javax;

import java.io.IOException;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import javax.servlet.AsyncListener;

public class AsyncContextJakarta implements AsyncContext, Javax {

	private javax.servlet.AsyncContext asyncContext;

	public AsyncContextJakarta(javax.servlet.AsyncContext asyncContext) {
		this.asyncContext = asyncContext;
	}

	@Override
	public ServletRequest getRequest() {
		return new ServletRequestJakarta(asyncContext.getRequest());
	}

	@Override
	public ServletResponse getResponse() {
		return new ServletResponseJakarta(asyncContext.getResponse());
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		return asyncContext.hasOriginalRequestAndResponse();
	}

	@Override
	public void dispatch() {
		asyncContext.dispatch();
	}

	@Override
	public void dispatch(String path) {
		asyncContext.dispatch(path);
	}

	@Override
	public void dispatch(ServletContext context, String path) {
		asyncContext.dispatch(((ServletContextJakarta) context).getJavaxContext(), path);
	}

	@Override
	public void complete() {
		asyncContext.complete();
	}

	@Override
	public void start(Runnable run) {
		asyncContext.start(run);
	}

	@Override
	public void addListener(jakarta.servlet.AsyncListener listener) {
		asyncContext.addListener(new javax.servlet.AsyncListener() {
			@Override
			public void onComplete(javax.servlet.AsyncEvent event) throws IOException {
				listener.onComplete(new jakarta.servlet.AsyncEvent(new AsyncContextJakarta(event.getAsyncContext()), new ServletRequestJakarta(event.getSuppliedRequest()),
						new ServletResponseJakarta(event.getSuppliedResponse())));
			}

			@Override
			public void onError(javax.servlet.AsyncEvent event) throws IOException {
				listener.onError(new jakarta.servlet.AsyncEvent(new AsyncContextJakarta(event.getAsyncContext()), new ServletRequestJakarta(event.getSuppliedRequest()),
						new ServletResponseJakarta(event.getSuppliedResponse()), event.getThrowable()));
			}

			@Override
			public void onStartAsync(javax.servlet.AsyncEvent event) throws IOException {
				listener.onStartAsync(new jakarta.servlet.AsyncEvent(new AsyncContextJakarta(event.getAsyncContext()), new ServletRequestJakarta(event.getSuppliedRequest()),
						new ServletResponseJakarta(event.getSuppliedResponse())));
			}

			@Override
			public void onTimeout(javax.servlet.AsyncEvent event) throws IOException {
				listener.onTimeout(new jakarta.servlet.AsyncEvent(new AsyncContextJakarta(event.getAsyncContext()), new ServletRequestJakarta(event.getSuppliedRequest()),
						new ServletResponseJakarta(event.getSuppliedResponse())));
			}
		});
	}

	@Override
	public void addListener(jakarta.servlet.AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
		asyncContext.addListener(

				(AsyncListener) ((AsyncListenerJakarta) listener).getJavaxInstance(),

				(javax.servlet.ServletRequest) ((ServletRequestJakarta) servletRequest).getJavaxInstance(),

				(javax.servlet.ServletResponse) ((ServletResponseJakarta) servletResponse).getJavaxInstance()

		);
	}

	@Override
	public <T extends jakarta.servlet.AsyncListener> T createListener(Class<T> clazz) throws ServletException {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		}
		catch (Exception e) {
			throw new ServletException("Could not create listener", e);
		}
	}

	@Override
	public void setTimeout(long timeout) {
		asyncContext.setTimeout(timeout);
	}

	@Override
	public long getTimeout() {
		return asyncContext.getTimeout();
	}

	@Override
	public Object getJavaxInstance() {
		return asyncContext;
	}
}