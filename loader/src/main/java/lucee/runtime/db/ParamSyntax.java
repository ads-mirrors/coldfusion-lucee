package lucee.runtime.db;

import java.io.Serializable;

public interface ParamSyntax extends Serializable {

	public String getLeadingDelimiter();

	public String getDelimiter();

	public String getSeparator();

}
