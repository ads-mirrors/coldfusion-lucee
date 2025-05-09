component extends="Layout" {
    fields=array(
        field("Environment Variables", "envnames", "", false, "Specify a comma-separated list of environment variable names to include in each log entry. These will appear as additional fields in the JSON output.", "textarea")
        ,field("Compact Format", "compact", "no", false, "When set to ""yes"", outputs the JSON in a compact single-line format without whitespace or indentation, optimizing for file size rather than readability.", "radio", "yes,no")
        ,field("Complete JSON Document", "complete", "no", false, "When set to ""yes"", wraps the output in a valid JSON array with ""["" at the beginning and ""]"" at the end, and adds commas between log entries. Set to ""no"" for streaming individual JSON objects.", "radio", "yes,no")
        ,field("Include Location Info", "locationInfo", "no", false, "When set to ""yes"", includes source code location information (file name and line number) for each log entry, helping with debugging but adding overhead to logging.", "radio", "yes,no")
        ,field("Include MDC Properties", "properties", "no", false, "When set to ""yes"", includes all Mapped Diagnostic Context (MDC) key-value pairs in the JSON output, allowing for additional contextual information in logs.", "radio", "yes,no")
        ,field("Include Epoch Time", "includeTimeMillis", "no", false, "When set to ""yes"", adds a ""timeMillis"" field containing the Unix epoch time (milliseconds since January 1, 1970 UTC) instead of using the default ISO-8601 timestamp format.", "radio", "yes,no")
        ,field("Character Encoding", "charset", "UTF-8", false, "Specifies the character encoding used when writing log files. UTF-8 is recommended for most applications to ensure proper handling of international characters.", "text")
        ,group("Stacktrace", "Java Exception Stacktrace Settings")
        ,field("Include Stacktrace", "includestacktrace", "yes", false, "When set to ""yes"", includes the full Java exception stacktrace in log entries that contain exceptions, providing detailed debugging information.", "radio", "yes,no")
        ,field("Stacktrace Format", "stacktraceAsString", "yes", false, "When set to ""yes"", formats the stacktrace as a single string value. When set to ""no"", formats it as a structured array of objects with detailed frame information.", "radio", "yes,no")
    );

    public string function getClass() {
        return "lucee.commons.io.log.log4j2.layout.JsonLayout";
    }

    public string function getLabel() {
        return "JSON";
    }

    public string function getDescription() {
        return "Outputs log entries in JSON format, providing structured data that's easily parseable by log analysis tools. 
		Each log entry becomes a JSON object containing standard fields like timestamp, level, message, and thread, plus any configured additional fields.
		 Note that this layout produces individual JSON objects by default - enable the [Complete JSON Document] option if you need valid array syntax.";
    }
}