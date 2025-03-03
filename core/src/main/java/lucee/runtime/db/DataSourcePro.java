package lucee.runtime.db;

import lucee.runtime.tag.listener.TagListener;

// FUTURE move content to loader
public interface DataSourcePro extends DataSource {

	public TagListener getListener();
}
