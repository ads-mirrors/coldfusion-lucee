package lucee.runtime.ai.anthropic;

import java.util.Iterator;
import java.util.List;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.Response;
import lucee.runtime.ai.Part;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class ClaudeResponse implements Response {
	private Struct raw;
	private String charset;
	private long tokens = -1L;

	public ClaudeResponse(Struct raw, String charset) {
		this.raw = raw;
		this.charset = charset;
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
		// Claude's response structure is different from OpenAI
		Array arr = Caster.toArray(raw.get(KeyConstants._content, null), null);
		if (arr == null || arr.size() == 0) return null;
		Iterator<Object> it = arr.valueIterator();
		Struct sct;
		String type, text;
		StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			sct = Caster.toStruct(it.next(), null);
			type = Caster.toString(sct.get(KeyConstants._type, null), null);
			if ("text".equals(type) || "code".equals(type)) {
				text = Caster.toString(sct.get(KeyConstants._text, null), null);
				if (!StringUtil.isEmpty(text, true)) {
					if (sb.length() > 0) sb.append('\n');
					sb.append(text);
				}
			}
		}
		if (sb.length() > 0) return sb.toString();
		return null;
		// TODO support image?

	}

	public Struct getData() {
		return raw;
	}

	@Override
	public long getTotalTokenUsed() {
		if (tokens == -1L) {
			// Claude's usage structure is different from OpenAI
			Struct usage = Caster.toStruct(raw.get("usage", null), null);
			if (usage == null) return tokens = 0L;

			// Claude reports input_tokens and output_tokens separately
			long inputTokens = Caster.toLongValue(usage.get("input_tokens", null), 0L);
			long outputTokens = Caster.toLongValue(usage.get("output_tokens", null), 0L);

			return tokens = inputTokens + outputTokens;
		}
		return tokens;
	}

	@Override
	public List<Part> getAnswers() {
		// TODO add support for multipart
		return AIUtil.getAnswersFromAnswer(this);
	}

	@Override
	public boolean isMultiPart() {
		// TODO add support for multipart
		return false;
	}
}