component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title="Test case LDEV-5581, duplicates in extensionList", body=function() {

			it(title="checking for duplicates in extensionList", body = function( currentSpec ) {
				var q_ext = extensionList();
				var exts = {};
				var dups = 0;
				loop query="#q_ext#"{
					if ( structKeyExists(exts,q_ext.id ) ){
						dups++;
						exts[q_ext.id] ++;
					} else {
						exts[q_ext.id] = 1;
					}
				}

				expect( q_ext.recordcount>0 ).toBeTrue( );
				expect( dups ).toBe( 0, "found duplicate extensions in extensionList" );

			});

		});
	}

}
