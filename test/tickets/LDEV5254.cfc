component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "test query duplicate for LDEV-5254 ", function() {

			it( "test query duplicate, deepCopy true", function() {
				var qry1 = querynew("a,b", "integer,integer" );
				queryAddRow( qry1 );
				querySetCell( qry1, "a", 1 );
				querySetCell( qry1, "b", 2 );

				expect( queryColumnData( qry1, "a") ).toBe( [ 1 ] );

				var qry2 = duplicate( object=qry1, deepCopy=true );
				querySetCell( qry1, "a", 999, 1 );
				queryAddRow( qry2 );
				querySetCell( qry2, "a", 4 );
				querySetCell( qry2, "b", 5 );

				expect( queryColumnData( qry1, "a") ).toBe( [ 999 ] );
				expect( queryColumnData( qry2, "a") ).toBe( [ 1, 4 ] );

			} );

			it( "test query duplicate, deepCopy false, simple value", function() {
				var qry1 = queryNew( "a,b", "integer,integer" );
				queryAddRow( qry1 );
				querySetCell( qry1, "a", 1 );
				querySetCell( qry1, "b", 2 );

				expect( queryColumnData( qry1, "a") ).toBe( [ 1 ] );

				var qry2 = duplicate( object=qry1, deepCopy=false );
				querySetCell( qry1, "a", 999, 1 );
				queryAddRow( qry2 );
				querySetCell( qry2, "a", 4 );
				querySetCell( qry2, "b", 5 );

				expect( queryColumnData( qry1, "a") ).toBe( [ 999 ] );
				expect( queryColumnData( qry2, "a") ).toBe( [ 1, 4] ); // not 999, as only complex types are duplicated

			} );

			it( "test query duplicate, deepCopy false with complex value", function() {
				var qry1 = queryNew( "a,b", "integer,integer" );
				var st = { lucee: true }; // simple types aren't deep copied
				queryAddRow( qry1 );
				querySetCell( qry1, "a", st );
				querySetCell( qry1, "b", 2 );

				expect( queryColumnData( qry1, "a") ).toBe( [ st ] );

				var qry2 = duplicate( object=qry1, deepCopy=false );
				querySetCell( qry1, "a", 999, 1 );
				queryAddRow( qry2 );
				querySetCell( qry2, "a", 4 );
				querySetCell( qry2, "b", 5 );

				expect( queryColumnData( qry1, "a") ).toBe( [ 999 ] );
				expect( queryColumnData( qry2, "a") ).toBe( [ st, 4] );

				// check it's not a deep copy
				st.lucee = false;
				expect( queryColumnData( qry2, "a") ).toBe( [ st, 4] );

			} );

		} );
	}
}
