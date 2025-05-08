package lucee.runtime.engine;

import java.util.Map;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.util.ResourceSnippet;
import lucee.commons.io.res.util.ResourceSnippetsMap;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.op.Caster;

public class LogExecutionLog extends ExecutionLogSupport {

	private PageContext pc;
	private ResourceSnippetsMap snippetsMap = new ResourceSnippetsMap(767, 191);
	private boolean snippet = false;
	private int logLevel;
	private String logName;

	@Override
	protected void _init(PageContext pc, Map<String, String> arguments) {
		this.pc = pc;
		if (Caster.toBooleanValue(arguments.get("snippet"), false)) snippet = true;
		String type = arguments.get("log-level");
		logLevel = LogUtil.toLevel(type, Log.LEVEL_TRACE);
		logName = StringUtil.toString(arguments.get("log-name"), Controler.class.getName());
	}

	@Override
	protected void _log(int startPos, int endPos, long startTime, long endTime) {
		PageSource ps = pc.getCurrentPageSource(null);
		if (ps == null) return;
		long diff = endTime - startTime;
		String log = pc.getId() + ":" + ps.getDisplayPath() + ":";
		if (snippet) {
			ResourceSnippet snippet = snippetsMap.getSnippet(ps, startPos, endPos, ((PageContextImpl) pc).getResourceCharset().name());
			log += positions(snippet.getEndLine(), snippet.getEndLine()) + " > " + timeLongToString(diff) + " [" + snippet.getContent() + "]";
		}
		else {
			log += positions(startPos, endPos) + " > " + timeLongToString(diff);
		}
		LogUtil.log(pc, logLevel, logName, Controler.class.getName(), log);
	}

	@Override
	protected void _release() {
		snippetsMap = null;
	}

	private static String positions(int startPos, int endPos) {
		if (startPos == endPos) return startPos + "";
		return startPos + ":" + endPos;
	}

}