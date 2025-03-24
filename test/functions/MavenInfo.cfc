component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run() {
		
		describe("MavenInfo Function Tests", function() {
			
			/* FUTURE
			it("should retrieve artifact info with no version specified (defaults to latest)", function() {
				var mavenData = MavenInfo("org.apache.httpcomponents", "httpclient");
				
				// Verify the query structure
				expect(mavenData).toBeTypeOf("query");
				expect(mavenData.recordCount).toBeGT(0);
				
				// Verify first row is the requested artifact
				expect(mavenData.groupId[1]).toBe("org.apache.httpcomponents");
				expect(mavenData.artifactId[1]).toBe("httpclient");
				
				// Verify required columns exist
				expect(mavenData).toHaveKey("groupId");
				expect(mavenData).toHaveKey("artifactId");
				expect(mavenData).toHaveKey("version");
				expect(mavenData).toHaveKey("scope");
				expect(mavenData).toHaveKey("optional");
				expect(mavenData).toHaveKey("url");
				expect(mavenData).toHaveKey("path");
			});*/
			
			it("should retrieve artifact info with specific version", function() {
				var version = "4.5.14";
				var mavenData = MavenInfo("org.apache.httpcomponents", "httpclient", version);
				systemOutput("------ 33 ------",1,1);
				systemOutput(mavenData,1,1);
				
				expect(mavenData.recordCount).toBeGT(0);
				expect(mavenData.version[1]).toBe(version);
				
				// Verify scopes (default should include all except test)
				var scopesFound = {};
				for (var i = 1; i <= mavenData.recordCount; i++) {
					scopesFound[mavenData.scope[i]] = true;
				}
				
				// Should have compile and possibly other scopes except test
				expect(scopesFound).toHaveKey("compile");
				expect(scopesFound).notToHaveKey("test");
			});
			
			it("should filter by selected scopes", function() {
				var scopes = ["compile", "provided"];
				var mavenData = MavenInfo(
					"org.apache.httpcomponents", 
					"httpclient", 
					"4.5.14", 
					scopes
				);
				
				expect(mavenData.recordCount).toBeGT(0);
				
				// Verify only requested scopes are included
				var scopesFound = {};
				for (var i = 1; i <= mavenData.recordCount; i++) {
					scopesFound[mavenData.scope[i]] = true;
				}
				
				expect(scopesFound).toHaveKey("compile");
				if (structKeyExists(scopesFound, "provided")) {
					expect(scopesFound).toHaveKey("provided");
				}
				expect(scopesFound).notToHaveKey("runtime");
				expect(scopesFound).notToHaveKey("test");
				expect(scopesFound).notToHaveKey("system");
			});
			
			it("should handle includeOptional parameter", function() {
				// With optional dependencies included
				var withOptional = MavenInfo(
					"org.apache.httpcomponents", 
					"httpclient", 
					"4.5.14",
					["compile"],
					true
				);
				
				// Without optional dependencies
				var withoutOptional = MavenInfo(
					"org.apache.httpcomponents", 
					"httpclient", 
					"4.5.14",
					["compile"],
					false
				);
				
				// Should have more dependencies with optional included
				expect(withOptional.recordCount).toBeGTE(withoutOptional.recordCount);
				
				// Check if there are optional dependencies
				var hasOptionalDeps = false;
				for (var i = 1; i <= withOptional.recordCount; i++) {
					if (withOptional.optional[i] == true) {
						hasOptionalDeps = true;
						break;
					}
				}
				
				// If the artifact has optional dependencies, verify they're excluded when includeOptional=false
				if (hasOptionalDeps) {
					var optionalInWithoutOptional = false;
					for (var i = 1; i <= withoutOptional.recordCount; i++) {
						if (withoutOptional.optional[i] == true) {
							optionalInWithoutOptional = true;
							break;
						}
					}
					expect(optionalInWithoutOptional).toBeFalse();
				}
			});
			
			it("should handle artifact with many dependencies", function() {
				var mavenData = MavenInfo("org.springframework", "spring-core", "5.3.29");
				systemOutput("------ 122 ------",1,1);
				systemOutput(mavenData,1,1);
				
				expect(mavenData.recordCount).toBeGT(5);
				expect(mavenData.groupId[1]).toBe("org.springframework");
				expect(mavenData.artifactId[1]).toBe("spring-core");
			});
			
			it("should have valid paths for all dependencies", function() {
				var mavenData = MavenInfo("commons-io", "commons-io", "2.11.0");
				systemOutput("------ 132 ------",1,1);
				systemOutput(mavenData,1,1);
				for (var i = 1; i <= mavenData.recordCount; i++) {
					var path = mavenData.path[i];
					expect(path).toBeTypeOf("string");
					expect(len(path)).toBeGT(0);
					expect(fileExists(path)).toBeTrue();
				}
			});
		});	
	}
}