package lucee.runtime.tag.javax.proxy;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.jsp.JspWriter;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;

public class JSPWriterProxy extends JspWriter {

	private Object javaxJspWriter;

	private JSPWriterProxy(Object javaxJspWriter) {
		super(0, false);
		this.javaxJspWriter = javaxJspWriter;
	}

	public static JspWriter getInstance(Object obj) {
		if (obj instanceof JspWriter) return (JspWriter) obj;
		if (obj == null) return null;
		// is BodyContent
		if (Reflector.isInstaneOf(obj.getClass().getName(), "jakarta.servlet.jsp.tagext.BodyContent")) {
			return new BodyContentProxy(obj);
		}
		return new JSPWriterProxy(obj);
	}

	@Override
	public int getBufferSize() {
		try {
			return Caster.toIntValue(Reflector.callMethodRE(getJavaxJspWriter(), "getBufferSize", new Object[] {}));
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public boolean isAutoFlush() {
		try {
			return Caster.toBooleanValue(Reflector.callMethodRE(getJavaxJspWriter(), "isAutoFlush", new Object[] {}));
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public void write(int c) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "write", new Object[] { c });
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "write", new Object[] { cbuf });
	}

	@Override
	public void write(String str) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "write", new Object[] { str });
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "write", new Object[] { str, off, len });
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		return (Writer) Reflector.callMethodRE(getJavaxJspWriter(), "append", new Object[] { csq });
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		return (Writer) Reflector.callMethodRE(getJavaxJspWriter(), "append", new Object[] { csq, start, end });
	}

	@Override
	public Writer append(char c) throws IOException {
		return (Writer) Reflector.callMethodRE(getJavaxJspWriter(), "append", new Object[] { c });
	}

	@Override
	public void newLine() throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "newLine", new Object[] {});
	}

	@Override
	public void print(boolean b) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "print", new Object[] { b });
	}

	@Override
	public void print(char c) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "print", new Object[] { c });
	}

	@Override
	public void print(int i) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "print", new Object[] { i });
	}

	@Override
	public void print(long l) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "print", new Object[] { l });
	}

	@Override
	public void print(float f) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "print", new Object[] { f });
	}

	@Override
	public void print(double d) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "print", new Object[] { d });
	}

	@Override
	public void print(char[] s) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "print", new Object[] { s });
	}

	@Override
	public void print(String s) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "print", new Object[] { s });
	}

	@Override
	public void print(Object obj) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "print", new Object[] { obj });
	}

	@Override
	public void println() throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] {});
	}

	@Override
	public void println(boolean x) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] { x });
	}

	@Override
	public void println(char x) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] { x });
	}

	@Override
	public void println(int x) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] { x });
	}

	@Override
	public void println(long x) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] { x });
	}

	@Override
	public void println(float x) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] { x });
	}

	@Override
	public void println(double x) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] { x });
	}

	@Override
	public void println(char[] x) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] { x });
	}

	@Override
	public void println(String x) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] { x });
	}

	@Override
	public void println(Object x) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "println", new Object[] { x });
	}

	@Override
	public void clear() throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "clear", new Object[] {});
	}

	@Override
	public void clearBuffer() throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "clearBuffer", new Object[] {});
	}

	@Override
	public void flush() throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "flush", new Object[] {});
	}

	@Override
	public void close() throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "close", new Object[] {});
	}

	@Override
	public int getRemaining() {
		try {
			return Caster.toIntValue(Reflector.callMethodRE(getJavaxJspWriter(), "getRemaining", new Object[] {}));
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}

	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		Reflector.callMethodRE(getJavaxJspWriter(), "write", new Object[] { cbuf, off, len });
	}

	public Object getJavaxJspWriter() {
		return javaxJspWriter;
	}

}
