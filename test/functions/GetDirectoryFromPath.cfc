component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		describe("getDirectoryFromPath Function", function() {
			it("extracts directory from Windows-style path", function() {
				var result = getDirectoryFromPath("C:\temp\file.txt");
				expect(result).toBe("C:\temp\");
			});

			it("extracts directory from Unix-style path", function() {
				var result = getDirectoryFromPath("/var/www/html/index.cfm");
				expect(result).toBe("/var/www/html/");
			});

			it("returns the same path for directory input", function() {
				var result = getDirectoryFromPath("/var/www/html/");
				expect(result).toBe("/var/www/html/");
			});

			it("handles relative paths", function() {
				var result = getDirectoryFromPath("../images/logo.png");
				expect(result).toBe("../images/");
			});

			it("returns empty string for file in root directory", function() {
				var result = getDirectoryFromPath("file.txt");
				expect(result).toBe(server.separator.file);
			});

			it("handles ..", function() {
				var result = getDirectoryFromPath("..");
				expect(result).toBe(server.separator.file);
			});

			it("handles .", function() {
				var result = getDirectoryFromPath(".");
				expect(result).toBe(server.separator.file);
			});

			it("handles ./", function() {
				var result = getDirectoryFromPath("./");
				expect(result).toBe("./");
			});

			it("handles .\", function() {
				var result = getDirectoryFromPath(".\");
				expect(result).toBe(".\");
			});

			it("handles ..\", function() {
				var result = getDirectoryFromPath("..\");
				expect(result).toBe("..\");
			});

			it("handles ../", function() {
				var result = getDirectoryFromPath("../");
				expect(result).toBe("../");
			});
		});
	}

}
