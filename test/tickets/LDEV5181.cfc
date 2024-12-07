component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function run( testResults, testBox ){
		describe( "LDEV5181", function(){

			it( "test java.lang.System via createObject", function(){
				stEnvVars = createObject("java", "java.lang.System").getenv();
			});

			it( "test java.lang.System natively", function(){
				stEnvVars = java.lang.System::getenv();
			});
		} );
	}

}
