package lucee.runtime.process;

import lucee.runtime.exp.PageException;

public interface ProcessListener {

	public Object listen(String output, Process process) throws PageException;

}