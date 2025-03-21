component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for decodeForHTML", function() {
			it(title = "Checking with decodeForHTML", body = function( currentSpec ) {
				var dec=decodeForHTML('&lt;script&gt;');
				assertEquals('<script>',dec);
			});
			it(title = "Checking with decodeForHTMLMember", body = function( currentSpec ) {
				var dec='&lt;script&gt;'.decodeForHTML();
				assertEquals('<script>',dec);
	
			});
		});	
	}
}