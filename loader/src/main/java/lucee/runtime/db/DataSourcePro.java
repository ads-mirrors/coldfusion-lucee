package lucee.runtime.db;

import lucee.runtime.tag.listener.TagListener;

public interface DataSourcePro extends DataSource {

	public TagListener getListener();
}
