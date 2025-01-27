component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		describe("getFileFromPath Function", function() {
			it("extracts filename from Windows-style path", function() {
				var result = getFileFromPath("C:\temp\file.txt");
				expect(result).toBe("file.txt");
			});

			it("extracts filename from Unix-style path", function() {
				var result = getFileFromPath("/var/www/html/index.cfm");
				expect(result).toBe("index.cfm");
			});

			it("returns empty string for directory path", function() {
				var result = getFileFromPath("/var/www/html/");
				expect(result).toBe("");
			});

			it("handles relative paths", function() {
				var result = getFileFromPath("../images/logo.png");
				expect(result).toBe("logo.png");
			});

			it("handles . path ending", function() {
				var result = getFileFromPath(".");
				expect(result).toBe("");

				var result = getFileFromPath("/tmp.");
				expect(result).toBe("");
			});

			it("handles .. path ending", function() {
				var result = getFileFromPath("/tmp/..");
				expect(result).toBe(".");
			});

		});
	}

}