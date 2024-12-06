component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "LDEV-5157 regressions", function(){

			it( "createObject java.lang.System", function(){
				expect(function(){
					var env = createObject("java", "java.lang.System").getenv();
				}).notToThrow();
			});

			it( "createobject java.nio.file.Paths", function(){
				expect(function(){
					var nioPath = createObject("java", "java.nio.file.Paths").get( getTempFile(getTempDirectory(),"ldev-5157"), [] );
				}).notToThrow();
			});
		} );
	}

}
