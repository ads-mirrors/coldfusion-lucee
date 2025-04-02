package lucee.runtime.mvn;

public class Repository {
	private String id;
	private String name;
	private String url;

	public Repository(String url) {
		this.url = url.endsWith("/") ? url : (url + "/");
	}

	public Repository(String id, String name, String url) {
		this(url);
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return url;
	}
}
