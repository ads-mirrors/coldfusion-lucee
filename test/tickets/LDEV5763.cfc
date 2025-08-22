component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, textbox ) {

		describe(title="LDEV-5763 cfml struct for javasettings hides all functions", body=function(){

			xit(title = "java settings cfml", body = function ( currentSpec ){
				var obj = new LDEV5763.LDEV5763_cfml();
				var meta = getMetadata(obj);
				expect(meta.functions).toHaveLength( 2 );
			});

			it(title = "java settings json", body = function ( currentSpec ){
				var obj = new LDEV5763.LDEV5763_json();
				var meta = getMetadata(obj);
				expect(meta.functions).toHaveLength( 2 );
			});
		});

	}

}