component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults, testBox ) {
		describe( "test case for LDEV-5457", function() {

			it( "test AWS library", function() {
				
				aws = new Component javasettings='{maven:["software.amazon.awssdk:auth:2.31.9", "software.amazon.awssdk:identity-spi:2.31.9"]}'{
					import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
					import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
					import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
			
					function test(){
						return AwsBasicCredentials::create("test", "test").getClass().getName();
					}
				};
				expect(aws.test()).toBe("software.amazon.awssdk.auth.credentials.AwsBasicCredentials");
			});

			it("test AWS library with createObject", function() {
                var uri = createURI("/LDEV5457");
                var result =_InternalRequest(template:"#uri#\ldev5457.cfm");
                expect( result.filecontent ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );
		    } );

			it("test AWS library using createObject with a component", function() {
                var uri = createURI("/LDEV5457");
                var result = createObject("component", "#uri#/testAwsBasicCredentials.cfc");
                expect(result.test()).toBe("software.amazon.awssdk.auth.credentials.AwsBasicCredentials");
            });

		} );
	}
	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
