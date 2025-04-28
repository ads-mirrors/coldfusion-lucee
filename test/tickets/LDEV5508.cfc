component extends="org.lucee.cfml.test.LuceeTestCase"  {

	function run( testResults , testBox ) {
		describe( title='LDEV-5508', body=function(){

			it( title='calling isJson() crashes', body=function() {
				var json = '.' & chr(10) & 'b';
				expect( isJson( json ) ).toBeFalse();
			});

		});
	}

}