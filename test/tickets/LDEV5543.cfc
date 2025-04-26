component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.testUrl = "https://update.lucee.org/rest/update/provider/light/5.3.10.97"; // returns a 302 to lucee cdn
	}

	function run( testResults, testBox ){

		describe( "LDEV-5543", function(){

			it( "test VALID http tag in script syntax", function(){
				http
					username="zac"
					result="local.result"
					method = "get"
					url = "#testUrl#"
					redirect = "yes";
				expect ( local ).toHaveKey( "result" );
			});
			
			xit( "test BAD http tag in script syntax", function(){
				http
					usename="zac"  // typo
					method = "get"
					url = "#testUrl#"
					result="local.result"
					redirect = "yes";
				expect ( local ).toHaveKey( "result" );
			});

		});
	}
}