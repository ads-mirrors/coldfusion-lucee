package lucee.runtime.tag.javax.proxy;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTag;
import lucee.print;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;

public class BodyTagProxy extends TagProxy implements BodyTag {
	BodyTagProxy(Object javaxTag) {
		super(javaxTag);
	}

	public static BodyTag getInstance(Object obj) throws ApplicationException {
		if (obj instanceof BodyTag) return (BodyTag) obj;
		if (obj == null) return null;
		// BodyTag
		if (Reflector.isInstaneOf(obj.getClass().getName(), "javax.servlet.jsp.tagext.BodyTag")) {
			print.ds("BodyTagProxy:" + obj.getClass().getName());
			return new BodyTagProxy(obj);
		}

		throw new ApplicationException("class [" + obj.getClass().getName() + "] cannot be loaded as a BodyTag ");
	}

	@Override
	public int doAfterBody() throws JspException {
		print.e("doAfterBody:" + getJavaxTag().getClass().getName());
		return Caster.toIntValue(Reflector.callMethod(getJavaxTag(), "doAfterBody", new Object[] {}));
	}

	@Override
	public void setBodyContent(BodyContent b) {
		Reflector.callMethodRE(getJavaxTag(), "setBodyContent", new Object[] { BodyContentProxy.getInstance(b) });
	}

	@Override
	public void doInitBody() throws JspException {
		Reflector.callMethod(getJavaxTag(), "doInitBody", new Object[] {});
	}

	public void hasBody(boolean hasBody) {
		try {
			Reflector.callMethod(getJavaxTag(), "hasBody", new Object[] { hasBody });
		}
		catch (PageException e) {

		}
	}

}
