package lucee.runtime.servlet.jsp;

import jakarta.servlet.jsp.tagext.Tag;

public interface TagPro extends Tag {

	public void setAppendix(String appendix);

	public void setMetaData(String name, Object value);
}