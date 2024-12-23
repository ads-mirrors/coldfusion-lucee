package lucee.commons.i18n;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FormatterWrapper {
	public final DateTimeFormatter formatter;
	public int successCount;
	public final String pattern;
	public final boolean custom;
	public final short type;
	public final ZoneId zone;

	private final boolean hasComma;
	private final boolean hasSlash;
	private final boolean hasColon;
	private final boolean hasSpace;
	private final boolean hasHyphen;

	FormatterWrapper(DateTimeFormatter formatter, String pattern, short type, ZoneId zone) {
		this.formatter = formatter;
		this.successCount = 0;
		this.pattern = pattern;
		this.type = type;
		this.zone = zone;
		this.custom = false;

		this.hasComma = pattern.indexOf(',') != -1;
		this.hasSlash = pattern.indexOf('/') != -1;
		this.hasHyphen = pattern.indexOf('-') != -1;
		this.hasColon = pattern.indexOf(':') != -1;
		this.hasSpace = pattern.indexOf(' ') != -1;
	}

	FormatterWrapper(DateTimeFormatter formatter, String pattern, short type, ZoneId zone, boolean custom) {
		this.formatter = formatter;
		this.successCount = 0;
		this.pattern = pattern;
		this.type = type;
		this.zone = zone;
		this.custom = custom;

		this.hasComma = pattern.indexOf(',') != -1;
		this.hasSlash = pattern.indexOf('/') != -1;
		this.hasHyphen = pattern.indexOf('-') != -1;
		this.hasColon = pattern.indexOf(':') != -1;
		this.hasSpace = pattern.indexOf(' ') != -1;
	}

	public boolean valid(String str) {
		if (pattern.length() > str.length()) return false;

		if (hasComma) {
			if (str.indexOf(',') == -1) return false;
		}
		else {
			if (str.indexOf(',') != -1) return false;
		}

		if (hasHyphen) {
			if (str.indexOf('-') == -1) return false;
		}
		else {
			if (str.indexOf('-') != -1) return false;
		}

		if (hasSlash) {
			if (str.indexOf('/') == -1) return false;
		}
		else {
			if (str.indexOf('/') != -1) return false;
		}

		if (hasColon) {
			if (str.indexOf(':') == -1) return false;
		}
		else {
			if (str.indexOf(':') != -1) return false;
		}

		if (hasSpace) {
			if (str.indexOf(' ') == -1) return false;
		}
		else {
			if (str.indexOf(' ') != -1) return false;
		}
		return true;
	}

}