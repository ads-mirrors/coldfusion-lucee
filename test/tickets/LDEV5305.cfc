component extends="org.lucee.cfml.test.LuceeTestCase" displayname="LDEV5305" {

	function beforeAll(){
		variables.randomTestFiles = [];
	}

	function afterAll(){
		for (var file in variables.randomTestFiles) {
			if (fileExists(file)) {
				fileDelete(file);
			}
		}
	}

	function run(testResults, textbox) {
		describe(title="LDEV-5305 Duplicate Class Definition", body=function(){
			it(title="concurrent include does not throw LinkageError", body=function(currentSpec){
				// hard to reproduce, so run multiple times
				loop times=10 {
					var randomTestFile = createRandomTestFile();
					var result = runExample("ldev5305_duplicate_class_stress.cfm", randomTestFile);
					// Should see LDEV-5305 marker in output
					expect(result).toInclude("LDEV-5305: passed");
				}
				
			});
		});
	}

	private string function runExample(string fileName, string randomTestFile) {
		var result = _InternalRequest(
			template: createURI("LDEV5305/" & fileName),
			url: {
				randomTestFile: randomTestFile
			}
		);
		return result.filecontent;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}

	private string function createRandomTestFile(){
		var testDir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV5305/";
		var randomTestFile = getTempFile(testdir, "test", ".cfm");
		fileCopy(expandPath(testDir & "ldev5305.cfm"), randomTestFile);
		arrayAppend(variables.randomTestFiles, randomTestFile);
		return randomTestFile;
	}

}
