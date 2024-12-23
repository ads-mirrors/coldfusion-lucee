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

    private final int characterFlags;

    private static final int COMMA_FLAG = 1;
    private static final int SLASH_FLAG = 1 << 1;
    private static final int HYPHEN_FLAG = 1 << 2;
    private static final int COLON_FLAG = 1 << 3;
    private static final int SPACE_FLAG = 1 << 4;

    FormatterWrapper(DateTimeFormatter formatter, String pattern, short type, ZoneId zone) {
        this(formatter, pattern, type, zone, false);
    }

    FormatterWrapper(DateTimeFormatter formatter, String pattern, short type, ZoneId zone, boolean custom) {
        this.formatter = formatter;
        this.successCount = 0;
        this.pattern = pattern;
        this.type = type;
        this.zone = zone;
        this.custom = custom;

        this.characterFlags = calculateCharacterFlags(pattern);
    }

    private static int calculateCharacterFlags(String pattern) {
        int flags = 0;
        if (pattern.contains(",")) flags |= COMMA_FLAG;
        if (pattern.contains("/")) flags |= SLASH_FLAG;
        if (pattern.contains("-")) flags |= HYPHEN_FLAG;
        if (pattern.contains(":")) flags |= COLON_FLAG;
        if (pattern.contains(" ")) flags |= SPACE_FLAG;
        return flags;
    }

    public boolean valid(String str) {
        if (pattern.length() > str.length()) return false;

        int strFlags = calculateCharacterFlags(str);
        return characterFlags == strFlags;
    }
}
