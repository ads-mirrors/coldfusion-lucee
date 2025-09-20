package lucee.runtime.spooler.listener;

import java.io.Serializable;

import lucee.runtime.config.Config;

public interface SpoolerTaskListener extends Serializable {

	public void listen(Config config, Exception e, boolean before);
}