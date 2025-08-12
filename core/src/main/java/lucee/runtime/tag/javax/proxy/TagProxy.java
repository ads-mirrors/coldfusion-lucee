package lucee.runtime.tag.javax.proxy;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.DynamicAttributes;
import jakarta.servlet.jsp.tagext.Tag;
import lucee.print;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection.Key;

public class TagProxy implements Tag, DynamicAttributes, lucee.runtime.ext.tag.DynamicAttributes {

	private Object javaxTag;
	private Tag parent;

	TagProxy(Object javaxTag) {
		this.javaxTag = javaxTag;
	}

	public static Tag getInstance(Object obj) throws ApplicationException {
		if (obj instanceof Tag) return (Tag) obj;
		if (obj == null) return null;

		// BodyTag
		if (Reflector.isInstaneOf(obj.getClass().getName(), "javax.servlet.jsp.tagext.BodyTag")) {
			print.ds("BodyTagProxy2:" + obj.getClass().getName());
			return new BodyTagProxy(obj);
		}
		// Tag
		if (Reflector.isInstaneOf(obj.getClass().getName(), "javax.servlet.jsp.tagext.Tag")) {
			print.ds("TagProxy:" + obj.getClass().getName());
			return new TagProxy(obj);
		}
		throw new ApplicationException("class [" + obj.getClass().getName() + "] cannot be loaded as a Tag ");
	}

	public static Tag getInstanceRE(Object obj) {
		try {
			return TagProxy.getInstance(obj);
		}
		catch (ApplicationException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public void setPageContext(PageContext pc) {
		Reflector.callMethodRE(getJavaxTag(), "setPageContext", new Object[] { pc });
	}

	@Override
	public void setParent(Tag tag) {
		if (tag instanceof TagProxy) {
			Reflector.callMethodRE(getJavaxTag(), "setParent", new Object[] { ((TagProxy) tag).getJavaxTag() });
		}
		this.parent = tag;
	}

	@Override
	public Tag getParent() {
		if (parent != null) return parent;

		Object objParent = Reflector.callMethodRE(getJavaxTag(), "getParent", new Object[] {});
		if (objParent == null) return null;
		return getInstanceRE(objParent);
	}

	@Override
	public int doStartTag() throws JspException {
		return Caster.toIntValue(Reflector.callMethod(getJavaxTag(), "doStartTag", new Object[] {}));
	}

	@Override
	public int doEndTag() throws JspException {
		return Caster.toIntValue(Reflector.callMethod(getJavaxTag(), "doEndTag", new Object[] {}));
	}

	@Override
	public void setDynamicAttribute(String uri, String localName, Object value) {
		print.e("------ setDynamicAttribute(" + uri + "," + localName + "," + value + ") ------");
		try {
			Reflector.callMethod(getJavaxTag(), "set" + StringUtil.ucFirst(localName), new Object[] { value });
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public void setDynamicAttribute(String uri, Key localName, Object value) {
		setDynamicAttribute(uri, localName.getString(), value);
	}

	@Override
	public void release() {
		Reflector.callMethodRE(getJavaxTag(), "release", new Object[] {});
	}

	public Object getJavaxTag() {
		return javaxTag;
	}

}
