package lucee.loader.servlet.javax;

import java.io.IOException;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class AsyncListenerJakarta implements AsyncListener, Javax {

	private final javax.servlet.AsyncListener listener;

	public AsyncListenerJakarta(javax.servlet.AsyncListener listener) {
		this.listener = listener;
	}

	@Override
	public void onComplete(AsyncEvent event) throws IOException {
		listener.onComplete((javax.servlet.AsyncEvent) ((AsyncEventJakarta) event).getJavaxInstance());
	}

	@Override
	public void onTimeout(AsyncEvent event) throws IOException {
		listener.onTimeout((javax.servlet.AsyncEvent) ((AsyncEventJakarta) event).getJavaxInstance());
	}

	@Override
	public void onError(AsyncEvent event) throws IOException {
		listener.onError((javax.servlet.AsyncEvent) ((AsyncEventJakarta) event).getJavaxInstance());
	}

	@Override
	public void onStartAsync(AsyncEvent event) throws IOException {
		listener.onStartAsync((javax.servlet.AsyncEvent) ((AsyncEventJakarta) event).getJavaxInstance());
	}

	@Override
	public Object getJavaxInstance() {
		return listener;
	}

	private static class AsyncEventJakarta extends AsyncEvent implements Javax {

		private final javax.servlet.AsyncEvent event;

		public AsyncEventJakarta(javax.servlet.AsyncEvent event) {
			super(null); // The superclass requires a valid constructor argument.
			this.event = event;
		}

		@Override
		public AsyncContext getAsyncContext() {
			return new AsyncContextJakarta(event.getAsyncContext());
		}

		@Override
		public ServletRequest getSuppliedRequest() {
			return new ServletRequestJakarta(event.getSuppliedRequest());
		}

		@Override
		public ServletResponse getSuppliedResponse() {
			return new ServletResponseJakarta(event.getSuppliedResponse());
		}

		@Override
		public Throwable getThrowable() {
			return event.getThrowable();
		}

		@Override
		public Object getJavaxInstance() {
			return event;
		}
	}

}
