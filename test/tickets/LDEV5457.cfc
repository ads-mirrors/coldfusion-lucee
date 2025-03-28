component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "test case for LDEV-5457", function() {

			it( "test AWS library", function() {
				
				software.amazon.awssdk.identity.spi.internal.DefaultAwsCredentialsIdentity


				aws = new Component javasettings='{maven:["software.amazon.awssdk:auth:2.31.9", "software.amazon.awssdk:identity-spi:2.31.9"]}'{
					import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
					import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
					import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
			
					function test(){
						return AwsBasicCredentials::create("test", "test").getClass().getName();
					}
				}
				expect( aws.test() ).toBe( "software.amazon.awssdk.auth.credentials.AwsBasicCredentials" );
			} );

		} );
	}
}
