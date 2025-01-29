component extends="org.lucee.cfml.test.LuceeTestCase" labels="image"{

	function run( testResults , testBox ) {

		describe( "test case for imageGetEXIFMetadata", function() {
			it(title = "Checking with imageGetEXIFMetadata", body = function( currentSpec ) {
				var img=imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg");
				var meta = imageGetEXIFMetadata(img);
				expect( meta.Compression ).toBe( "6" );
				expect( meta.Flash ).toBe( "16" );
			});
		});

	}

}