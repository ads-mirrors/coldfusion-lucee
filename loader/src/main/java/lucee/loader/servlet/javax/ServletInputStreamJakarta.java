package lucee.loader.servlet.javax;

import java.io.IOException;

import jakarta.servlet.ServletInputStream;

public class ServletInputStreamJakarta extends ServletInputStream {

	private javax.servlet.ServletInputStream inputStream;

	public ServletInputStreamJakarta(javax.servlet.ServletInputStream inputStream) {
		if (inputStream == null) throw new NullPointerException();
		this.inputStream = inputStream;
	}

	@Override
	public int read() throws IOException {
		return inputStream.read();
	}

	@Override
	public boolean isFinished() {
		return inputStream.isFinished();
	}

	@Override
	public boolean isReady() {
		return inputStream.isReady();
	}

	@Override
	public void setReadListener(jakarta.servlet.ReadListener readListener) {
		inputStream.setReadListener(new javax.servlet.ReadListener() {
			@Override
			public void onDataAvailable() throws IOException {
				readListener.onDataAvailable();
			}

			@Override
			public void onAllDataRead() throws IOException {
				readListener.onAllDataRead();
			}

			@Override
			public void onError(Throwable t) {
				readListener.onError(t);
			}
		});
	}
}