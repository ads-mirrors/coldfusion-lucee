component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "LDEV5256", function(){

			it( "test java.lang.System via createObject", function(){
				
				var curr=getDirectoryFromPath(getCurrentTemplatePath());
				var test=createObject("java", "lucee.runtime.test.Test5256",curr&"LDEV5256/jars/test5256.jar");
				this.javaObject=test;
				expect( test.test(this) ).toBe( "lucee.runtime.test.Test5256" );

			});

		} );
	}
}
