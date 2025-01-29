component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults , testBox ) {

		describe( "test case for ImageGetEXIFTag", function() {

			it(title = "Checking with ImageGetEXIFTag", body = function( currentSpec ) {
				var img=imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg");
				expect( ImageGetEXIFTag( img, 'Compression') ).toBe( "6" );
				expect( ImageGetEXIFTag( img, 'Flash') ).toBe( "16" );
			});

		});

	}
}