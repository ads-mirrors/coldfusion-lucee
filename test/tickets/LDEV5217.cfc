component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "test esapi for LDEV-5217 ", function() {
			it( "test decodeFromURL", function() {
				decodeFromURL("You%20have%20no%20new%20messages%20in%20the%20inbox%2E");

				var originalString = "You have no new messages in the inbox.";
				var urlEncodedString = URLEncodedFormat(originalString);
				decodeFromURL(urlEncodedString);

				expect( decodeFromURL(urlEncodedString) ).toBe( originalString );
			} );
		} );
	}
}
