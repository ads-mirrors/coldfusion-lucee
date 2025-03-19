package lucee.runtime.ai.google;

import java.util.List;

import lucee.commons.io.CharsetUtil;
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

public final class GeminiResponse implements Response {

	private Struct raw;
	private String charset;
	private long tokens = -1L;

	public GeminiResponse(Struct raw, String charset) {
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
		Array arr = Caster.toArray(raw.get("candidates", null), null);

		if (arr == null) return null;
		Struct sct = Caster.toStruct(arr.get(1, null), null);
		if (sct == null) return null;
		sct = Caster.toStruct(sct.get("content", null), null);
		if (sct == null) return null;

		arr = Caster.toArray(sct.get("parts", null), null);
		if (arr == null) return null;

		sct = Caster.toStruct(arr.get(1, null), null);
		if (sct == null) return null;

		return Caster.toString(sct.get(KeyConstants._text, null), null);
	}

	public Struct getData() {
		return raw;
	}

	@Override
	public long getTotalTokenUsed() {
		if (tokens == -1L) {
			Struct sct = Caster.toStruct(raw.get("usageMetadata", null), null);
			if (sct == null) return tokens = 0L;
			return tokens = Caster.toLongValue(sct.get("totalTokenCount", null), 0L);
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
