package lucee.runtime.config;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lucee.runtime.CIPage;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class ComponentPathCache {
	private Map<String, SoftReference<PageSource>> componentPathCache = null;// new ArrayList<Page>();

	public CIPage getPage(PageContext pc, String pathWithCFC) throws TemplateException {
		if (componentPathCache == null) return null;
		SoftReference<PageSource> tmp = componentPathCache.get(pathWithCFC.toLowerCase());
		PageSource ps = tmp == null ? null : tmp.get();
		if (ps == null) return null;

		try {
			return (CIPage) ps.loadPageThrowTemplateException(pc, false, (Page) null);
		}
		catch (PageException pe) {
			throw (TemplateException) pe;
		}
	}

	public void put(String pathWithCFC, PageSource ps) {
		if (componentPathCache == null) componentPathCache = new ConcurrentHashMap<String, SoftReference<PageSource>>();// MUSTMUST new
		// ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);
		componentPathCache.put(pathWithCFC.toLowerCase(), new SoftReference<PageSource>(ps));
	}

	public void flush() {
		if (componentPathCache != null) componentPathCache.clear();
	}

	public void clear() {
		if (componentPathCache == null) return;
		componentPathCache.clear();
	}

	public Struct list() {
		Struct sct = new StructImpl();
		if (componentPathCache == null) return sct;
		Iterator<Entry<String, SoftReference<PageSource>>> it = componentPathCache.entrySet().iterator();

		Entry<String, SoftReference<PageSource>> entry;
		while (it.hasNext()) {
			entry = it.next();
			String k = entry.getKey();
			if (k == null) continue;
			SoftReference<PageSource> v = entry.getValue();
			if (v == null) continue;
			PageSource ps = v.get();
			if (ps == null) continue;
			sct.setEL(KeyImpl.init(k), ps.getDisplayPath());
		}
		return sct;
	}
}
