package lucee.commons.io.res.type.ftp;

import lucee.runtime.net.proxy.ProxyData;

public interface IFTPConnectionData {

	public String key();

	public boolean hasProxyData();

	public ProxyData getProxyData();

	public String getUsername();

	public String getPassword();

	public String getHost();

	public int getPort();

}
