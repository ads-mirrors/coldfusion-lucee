package lucee.runtime.config.maven;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lucee.commons.lang.StringUtil;

public class HtmlDirectoryScraper {

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	private static final Pattern LINK_PATTERN = Pattern.compile("<a\\s+href=\"([^\"]+)\"");

	/**
	 * Scrapes HTML directory listing and returns all subfolder links
	 * 
	 * @param url The URL to scrape
	 * @return List of href values that point to subfolders
	 * @throws IOException if network request fails
	 * @throws InterruptedException if request is interrupted
	 */
	public void getSubfolderLinks(String url, Set<String> set) throws IOException, InterruptedException {
		String html = fetchUrl(url);
		if (html == null) return;
		extractSubfolderLinks(html, set);
	}

	/**
	 * Extracts subfolder links from HTML content Links must: not contain "://", not be "..", and end
	 * with "/"
	 * 
	 * @param html The HTML content to parse
	 * @return List of subfolder href values
	 */
	public void extractSubfolderLinks(String html, Set<String> set) {

		Matcher matcher = LINK_PATTERN.matcher(html);
		while (matcher.find()) {
			String href = matcher.group(1);

			if (isSubfolderLink(href)) {
				if (href.endsWith("/")) set.add(href.substring(0, href.length() - 1));
				else set.add(href);
			}
		}
	}

	/**
	 * Checks if a link href points to a subfolder
	 * 
	 * @param href The href value to check
	 * @return true if it's a subfolder link
	 */
	private boolean isSubfolderLink(String href) {
		return !href.contains("://") && // Not an absolute URL
				!href.contains("../") && // Not parent directory
				href.endsWith("/"); // Ends with slash (directory)
	}

	/**
	 * Fetches HTML content from URL
	 * 
	 * @param url The URL to fetch
	 * @return HTML content as string
	 * @throws IOException if request fails
	 * @throws InterruptedException if request is interrupted
	 */
	private String fetchUrl(String url) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(30)).header("User-Agent", "HTML-Directory-Scraper/1.0").build();
		int statusCode = 0;
		HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			if (response.statusCode() != 404) statusCode = response.statusCode();
		}

		// if has /index.html
		String body = response != null ? response.body() : null;
		if (StringUtil.isEmpty(body, true) && url.endsWith("/")) {
			request = HttpRequest.newBuilder().uri(URI.create(url + "index.html")).timeout(Duration.ofSeconds(30)).header("User-Agent", "HTML-Directory-Scraper/1.0").build();

			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				if (response.statusCode() == 404) return null;
				throw new IOException("HTTP " + response.statusCode() + " for URL: " + url);
			}
			body = response != null ? response.body() : null;
			return StringUtil.isEmpty(body, true) ? null : body;
		}

		if (statusCode > 0) {
			throw new IOException("HTTP " + statusCode + " for URL: " + url);
		}
		return body;
	}

	// Example usage
	/*
	 * public static void main(String[] args) { HtmlDirectoryScraper scraper = new
	 * HtmlDirectoryScraper(); String url = "https://repo1.maven.org/maven2/org/lucee/";
	 * 
	 * try { Set<String> subfolders = new HashSet<>(); scraper.getSubfolderLinks(url, subfolders);
	 * 
	 * System.out.println("Found " + subfolders.size() + " subfolders:"); for (String folder:
	 * subfolders) { System.out.println("  " + folder); }
	 * 
	 * } catch (Exception e) { System.err.println("Error: " + e.getMessage()); } }
	 */
}