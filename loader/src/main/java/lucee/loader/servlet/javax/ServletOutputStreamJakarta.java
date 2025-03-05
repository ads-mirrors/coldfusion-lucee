package lucee.loader.servlet.javax;

import java.io.IOException;

import jakarta.servlet.ServletOutputStream;

public class ServletOutputStreamJakarta extends ServletOutputStream {

	private javax.servlet.ServletOutputStream outputStream;

	public ServletOutputStreamJakarta(javax.servlet.ServletOutputStream outputStream) {
		if (outputStream == null) throw new NullPointerException();
		this.outputStream = outputStream;
	}

	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);
	}

	@Override
	public boolean isReady() {
		return outputStream.isReady();
	}

	@Override
	public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
		outputStream.setWriteListener(new javax.servlet.WriteListener() {
			@Override
			public void onWritePossible() throws IOException {
				writeListener.onWritePossible();
			}

			@Override
			public void onError(Throwable t) {
				writeListener.onError(t);
			}
		});
	}
}