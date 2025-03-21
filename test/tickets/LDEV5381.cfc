component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='getMetadata Return No Longer Returns name for structs and arrays' , body=function(){

			it( title='array metadata', body=function() {
				var myArray = [];
				var meta = getMetadata( myArray );
				expect( meta ).toHaveKey( "name" );
				expect( meta.name ).toBe( "lucee.runtime.type.ArrayImpl");
			});

			it( title='struct metadata', body=function() {
				var myStruct = {};
				var meta = getMetadata( myStruct );
				expect( meta ).toHaveKey( "name" );
				expect( meta.name ).toBe( "lucee.runtime.type.StructImpl");
			});

			it( title='cfc metadata', body=function() {
				var myCFc = new Component name="ldev5381" {
					function init(){

					}

					function report(){

					}
				};
				var meta = getMetadata( myCfc );
				debug(meta);
				expect( meta ).toHaveKey( "name" );
				expect( meta ).toHaveKey( "functions" );
				expect( meta.functions[1].returnFormat ).toBeString();
			});

		});
	}

}