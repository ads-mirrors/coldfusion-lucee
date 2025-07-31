component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="Test case LDEV-5743", body=function() {
			it(title="queryAddColumn with positional arguments", body = function( currentSpec ) {
				var q = queryNew( names="id,name" );
				queryAddColumn( q, "type" );
				expect( listLen( q.columnlist) ).toBe( 3 );
			});

			it(title="queryAddColumn with named arguments", body = function( currentSpec ) {
				var q = queryNew( names="id,name" );
				queryAddColumn( query=q ,column="type" );
				expect( listLen( q.columnlist ) ).toBe( 3 );
			});

			it(title="queryAddColumn with named arguments", body = function( currentSpec ) {
				var q = queryNew( names="id,name" );
				queryAddColumn( query=q ,column="type",datatype="varchar" );
				expect( listLen( q.columnlist ) ).toBe( 3 );
			});

		});
	}

}
