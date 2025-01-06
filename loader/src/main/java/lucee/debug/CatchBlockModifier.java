package lucee.debug;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CatchBlockModifier {

	public static void main(String[] args) throws IOException {
		processDirectory(Paths.get("/Users/mic/Projects/Lucee/Lucee6/loader"));
		processDirectory(Paths.get("/Users/mic/Projects/Lucee/Lucee6/core"));
	}

	private static int totalCount = 0;

	public static void processDirectory(Path directory) throws IOException {
		Files.walk(directory).filter(path -> path.toString().endsWith(".java")).forEach(file -> {
			try {
				processFile(file);
			}
			catch (IOException e) { lucee.debug.CatchBlockModifier.log(e);
				System.err.println("Error processing file:" + file);
				e.printStackTrace();
			}
		});
		System.out.println("Total catch blocks found: " + totalCount);
	}

	public static void processFile(Path file) throws IOException {
		String content = new String(Files.readAllBytes(file));

		// More inclusive pattern for catch blocks
		Pattern pattern = Pattern.compile("catch\\s*" + // catch keyword with optional whitespace
				"\\(" + // opening parenthesis
				"\\s*" + // optional whitespace
				"(?:[\\w.$]+)" + // exception type (including nested classes with dots)
				"\\s+" + // whitespace between type and variable
				"(\\w+)" + // capture the exception variable name
				"\\s*" + // optional whitespace
				"\\)" + // closing parenthesis
				"\\s*" + // optional whitespace
				"\\{", // opening brace
				Pattern.MULTILINE);

		Matcher matcher = pattern.matcher(content);

		StringBuffer newContent = new StringBuffer();
		int fileCount = 0;

		while (matcher.find()) {
			fileCount++;
			String exceptionVar = matcher.group(1);
			matcher.appendReplacement(newContent, matcher.group(0) + " lucee.debug.CatchBlockModifier.log(" + exceptionVar + ");");
		}
		matcher.appendTail(newContent);

		totalCount += fileCount;

		if (fileCount > 0) {
			System.out.println(file + ": " + fileCount + " catch blocks found");
			Files.write(file, newContent.toString().getBytes()); // Commented out for safety
		}
	}

	private static long count = 0;

	public static void logConsole(Throwable t) {
		if (t == null) return;
		System.err.println((count++) + " -> " + t.getMessage());
		if (t != null) t.printStackTrace();
	}

	public static void log(Throwable t) {
		if (t == null) return;

		try (PrintWriter writer = new PrintWriter(new FileWriter("/Users/mic/tmp9/errors.log", true))) {
			writer.println((count++) + " -> " + t.getMessage());
			t.printStackTrace(writer);
		}
		catch (IOException e) { lucee.debug.CatchBlockModifier.log(e);
			// Fallback to console if file writing fails
			System.err.println("Error writing to log file:");
			e.printStackTrace();
			System.err.println("Original error:");
			t.printStackTrace();
		}
	}
}