package lucee.runtime.ai.anthropic;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.ai.AIResponseListener;
import lucee.runtime.ai.Response;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class ClaudeStreamResponse implements Response {
	private Struct raw = null;
	private String charset;
	private StringBuilder answer = new StringBuilder();
	private AIResponseListener listener;

	public ClaudeStreamResponse(String charset, AIResponseListener listener) {
		this.charset = charset;
		this.listener = listener;
	}

	@Override
	public String toString() {
		try {
			JSONConverter json = new JSONConverter(false, CharsetUtil.toCharset(charset), JSONDateFormat.PATTERN_CF, false);
			return json.serialize(null, raw, SerializationSettings.SERIALIZE_AS_UNDEFINED, true);
		}
		catch (ConverterException e) {
			return raw.toString();
		}
	}

	@Override
	public String getAnswer() {
		return answer.toString();
	}

	/*
	 * public Struct getData() { return raw; }
	 */

	public void addPart(Struct part) throws PageException {

		if (raw == null) raw = part;

		String type = Caster.toString(part.get(KeyConstants._type, null), null);
		if (StringUtil.isEmpty(type) || !type.startsWith("content_block")) return;

		Struct delta = Caster.toStruct(part.get("delta", null), null);
		if (delta == null) return;

		type = Caster.toString(delta.get(KeyConstants._type, null), null);
		if (StringUtil.isEmpty(type) || !type.startsWith("text")) return;

		String text = Caster.toString(delta.get(KeyConstants._text, null), null);
		if (StringUtil.isEmpty(text)) return;

		answer.append(text);
		if (listener != null) listener.listen(text);
	}

	@Override
	public long getTotalTokenUsed() {
		// Claude's streaming response doesn't provide token counts in the stream
		return 0;
	}
}