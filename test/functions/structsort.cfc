component extends="org.lucee.cfml.test.LuceeTestCase" labels="struct" {

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

		variables.animals = [
			cow: [
				noise: "moo",
				size: "large"
			],
			pig: [
				noise: "oink",
				size: "MEDIUM"
			],
			cat: [
				noise: "MEOW",
				Size: "small"
			],
			bat: [
				Noise:"kee",
				size:"SMALL"
			]
		];
	}

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for structsort", body = function() {

			it( title = 'Checking with structsort()',body = function( currentSpec ) {
				assertEquals('["CAT","BAT","COW","PIG"]',serialize(structsort(animals,"text","asc","noise")));
				assertEquals('["PIG","COW","BAT","CAT"]',serialize(structsort(animals,"text","desc","noise")));

				assertEquals('["BAT","CAT","COW","PIG"]',serialize(structsort(animals,"textnocase","asc","noise")));
				assertEquals('["PIG","COW","CAT","BAT"]',serialize(structsort(animals,"textnocase","desc","noise")));

				assertEquals('["PIG","BAT","COW","CAT"]',serialize(structsort(animals,"text","asc","size")));
				assertEquals('["CAT","COW","BAT","PIG"]',serialize(structsort(animals,"text","desc","size")));
				
				assertEquals('["COW","PIG","CAT","BAT"]',serialize(structsort(animals,"textnocase","asc","size")));
				assertEquals('["CAT","BAT","PIG","COW"]',serialize(structsort(animals,"textnocase","desc","size")));

				// with named args
				assertEquals('["CAT","BAT","PIG","COW"]',serialize(structsort(base=animals,sortType="textnocase",sortOrder="desc",pathToSubElement="size")));
				// path alias for pathToSubElement
				assertEquals('["CAT","BAT","PIG","COW"]',serialize(structsort(base=animals,sortType="textnocase",sortOrder="desc",path="size")));
			});

			it( title = 'Checking with struct.sort() member function',body = function( currentSpec ) {
				assertEquals('["CAT","BAT","COW","PIG"]',serialize(animals.sort("text","asc","noise")));
				assertEquals('["PIG","COW","BAT","CAT"]',serialize(animals.sort("text","desc","noise")));

				assertEquals('["BAT","CAT","COW","PIG"]',serialize(animals.sort("textnocase","asc","noise")));
				assertEquals('["PIG","COW","CAT","BAT"]',serialize(animals.sort("textnocase","desc","noise")));

				assertEquals('["PIG","BAT","COW","CAT"]',serialize(animals.sort("text","asc","size")));
				assertEquals('["CAT","COW","BAT","PIG"]',serialize(animals.sort("text","desc","size")));

				assertEquals('["COW","PIG","CAT","BAT"]',serialize(animals.sort("textnocase","asc","size")));
				assertEquals('["CAT","BAT","PIG","COW"]',serialize(animals.sort("textnocase","desc","size")));
			});
		});

		describe( "test case StructSort - with callback", function() {
			it(title = "structSort BIF Callback", body = function( currentSpec ) {
				var cb = function( key1, key2 ){
					if ( arguments.key1 < arguments.key2 ) // i.e. desc
						return 1;
					else
						return -1;
				};
				var res = StructSort( duplicate( myNumb ), cb );
				expect( res ).toBe( [ 4, 3, 2, 1 ] );
			});

			it(title = "structSort member function with callback", body = function( currentSpec ) {
				var cb = function( key1, key2 ){
					if ( arguments.key1 < arguments.key2 ) // i.e. desc
						return 1;
					else
						return -1;
				};
				var res = myNumb.sort( cb );
				expect( res ).toBe( [ 4, 3, 2, 1 ] );
			});
		});

	}
}