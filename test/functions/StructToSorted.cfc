component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.myStr = StructNew();
		myStr.dd="dd";
		myStr.cc="cc";
		myStr.aa="aa";
		myStr.bb="bb";

		variables.myNumb = StructNew();
		myNumb.4="4";
		myNumb.3="3";
		myNumb.2="2";
		myNumb.1="1";
	}

	function run( testResults , testBox ) {

		describe( "test case for StructToSorted BIF", function() {
			it(title = "Checking string type with StructToSorted() in ASC order", body = function( currentSpec ) {
				var res = StructToSorted( duplicate( myStr ),"text", "asc" );
				expect( res.keyList() ).toBe( 'AA,BB,CC,DD', res.toJson() );
				expect( res.valueArray() ).toBe( [ 'AA', 'BB', 'CC', 'DD' ] );
			});
			it(title = "Checking string type with StructToSorted() in DESC order", body = function( currentSpec ) {
				var res = myStr=StructToSorted(duplicate(myStr),"text","desc");
				expect( res.keyList() ).toBe( 'DD,CC,BB,AA', res.toJson() );
				expect( res.valueArray() ).toBe( [ 'DD','CC','BB','AA' ] );
			});

			it(title = "Checking Numeric type with StructToSorted() in ASC order", body = function( currentSpec ) {
				var res = myNumb=StructToSorted( duplicate( myNumb ), "numeric", "asc" );
				expect( res.keyList() ).toBe( '1,2,3,4', res.toJson());
				expect( res.valueArray() ).toBe( [ 1, 2, 3, 4 ] );
			});
			it(title = "Checking Numeric type with StructToSorted() in DESC order", body = function( currentSpec ) {
				var res = myNumb=StructToSorted( duplicate( myNumb ), "numeric", "desc" );
				expect( res.keyList() ).toBe( '4,3,2,1', res.toJson() );
				expect( res.valueArray() ).toBe( [ 4, 3, 2, 1 ] );
			});
		});

		describe( "test case StructToSorted - member functions", function() {
			it(title = "Checking string type with StructToSorted() in ASC order", body = function( currentSpec ) {
				var res = mystr.ToSorted("text","asc");
				expect( res.keyList() ).toBe( 'AA,BB,CC,DD', res.toJson() );
				expect( res.valueArray() ).toBe( [ 'AA', 'BB', 'CC', 'DD' ] );
			});
			it(title = "Checking string type with StructToSorted() in DESC order", body = function( currentSpec ) {
				var res = mystr.ToSorted("text","desc");
				expect( res.keyList() ).toBe( 'DD,CC,BB,AA', res.toJson() );
				expect( res.valueArray() ).toBe( [ 'DD','CC','BB','AA' ] );
			});

			it(title = "Checking Numeric type with StructToSorted() in ASC order", body = function( currentSpec ) {
				var res = myNumb.ToSorted("numeric","asc");
				expect( res.keyList() ).toBe( '1,2,3,4', res.toJson());
				expect( res.valueArray() ).toBe( [ 1, 2, 3, 4 ] );
			});
			it(title = "Checking Numeric type with StructToSorted() in DESC order", body = function( currentSpec ) {
				var res = myNumb.ToSorted("numeric","desc");
				expect( res.keyList() ).toBe( '4,3,2,1', res.toJson() );
				expect( res.valueArray() ).toBe( [ 4, 3, 2, 1 ] );
			});
		});
	}
}

