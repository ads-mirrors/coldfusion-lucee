package lucee.loader.servlet.javax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponse;

public class ServletResponseJakarta implements ServletResponse, Javax {

	private javax.servlet.ServletResponse rsp;

	public ServletResponseJakarta(javax.servlet.ServletResponse rsp) {
		if (rsp == null) throw new NullPointerException();
		this.rsp = rsp;
	}

	@Override
	public String getCharacterEncoding() {
		return rsp.getCharacterEncoding();
	}

	@Override
	public String getContentType() {
		return rsp.getContentType();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new ServletOutputStreamJakarta(rsp.getOutputStream());
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return rsp.getWriter();
	}

	@Override
	public void setCharacterEncoding(String charset) {
		rsp.setCharacterEncoding(charset);
	}

	@Override
	public void setContentLength(int len) {
		rsp.setContentLength(len);
	}

	@Override
	public void setContentLengthLong(long len) {
		rsp.setContentLengthLong(len);
	}

	@Override
	public void setContentType(String type) {
		rsp.setContentType(type);
	}

	@Override
	public void setBufferSize(int size) {
		rsp.setBufferSize(size);
	}

	@Override
	public int getBufferSize() {
		return rsp.getBufferSize();
	}

	@Override
	public void flushBuffer() throws IOException {
		rsp.flushBuffer();
	}

	@Override
	public void resetBuffer() {
		rsp.resetBuffer();
	}

	@Override
	public boolean isCommitted() {
		return rsp.isCommitted();
	}

	@Override
	public void reset() {
		rsp.reset();
	}

	@Override
	public void setLocale(Locale loc) {
		rsp.setLocale(loc);
	}

	@Override
	public Locale getLocale() {
		return rsp.getLocale();
	}

	@Override
	public Object getJavaxInstance() {
		return rsp;
	}
}