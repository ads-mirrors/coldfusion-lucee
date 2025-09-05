component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" skip=true {
	function run( testResults , testBox ) {

		describe( title="LDEV-5790 qoq with varchar col - WITH query params", body=function() {

			it(title="checking with VARCHAR param", body = function( currentSpec ) {
				testWith( "varchar", "varchar" );
			});

			xit(title="checking with INT param", body = function( currentSpec ) {
				testWith( "varchar", "int" );
			});

			xit(title="checking with BIGINT param", body = function( currentSpec ) {
				testWith( "varchar", "bigint" );
			});

			xit(title="checking with FLOAT param", body = function( currentSpec ) {
				testWith( "varchar", "float" );
			});
		});

		describe( title="LDEV-5790 qoq with varchar col - WITHOUT query params", body=function() {

			it(title="checking with VARCHAR param", body = function( currentSpec ) {
				testWithout( "varchar", "varchar" );
			});

			it(title="checking with INT param", body = function( currentSpec ) {
				testWithout( "varchar", "int" );
			});

			it(title="checking with BIGINT param", body = function( currentSpec ) {
				testWithout( "varchar", "bigint" );
			});

			it(title="checking with FLOAT param", body = function( currentSpec ) {
				testWithout( "varchar", "float" );
			});
		});

		describe( title="LDEV-5790 qoq with BIGINT col - WITH query params", body=function() {

			it(title="checking with VARCHAR param", body = function( currentSpec ) {
				testWith( "bigint", "varchar" );
			});

			xit(title="checking with INT param", body = function( currentSpec ) {
				testWith( "bigint", "int" );
			});

			xit(title="checking with BIGINT param", body = function( currentSpec ) {
				testWith( "bigint", "bigint" );
			});

			xit(title="checking with FLOAT param", body = function( currentSpec ) {
				testWith( "bigint", "float" );
			});
		});

		describe( title="LDEV-5790 qoq with BIGINT col - WITHOUT query params", body=function() {

			it(title="checking with VARCHAR param", body = function( currentSpec ) {
				testWithout( "bigint", "varchar" );
			});

			it(title="checking with INT param", body = function( currentSpec ) {
				testWithout( "bigint", "int" );
			});

			it(title="checking with BIGINT param", body = function( currentSpec ) {
				testWithout( "bigint", "bigint" );
			});

			it(title="checking with FLOAT param", body = function( currentSpec ) {
				testWithout( "bigint", "float" );
			});
		});
	}

	private function testWith ( colDataType, clauseDataType ){
		var q = getQuery( colDataType );
		expect( getMetadata( q )[ 1 ].typeName ) .toBe( arguments.colDataType ) ;
		var val = q.num[ 1 ];
		var q_with = testWithQueryParam( q, clauseDataType, val );

		expect( q_with.q.num[ 1 ] ).toBe( val );
		expect( q_with.q.recordCount ).toBe(1);
	}

	private function testWithOut ( colDataType, clauseDataType ){
		var q = getQuery( colDataType );
		expect( getMetadata( q )[ 1 ].typeName ) .toBe( arguments.colDataType ) ;
		var val = q.num[ 1 ];
		var q_without = testWithoutQueryParam( q, val );

		expect( q_without.q.recordCount ).toBe(1);
		expect( q_without.q.num[ 1 ] ).toBe( val );

	}

	private function testWithQueryParam(q, dataType, val){
		var q = queryExecute(
			"SELECT * from q where num = ?",
			[{value=val, sqltype=arguments.dataType}],
			{dbtype="query", result="r"}
		);
		return {q:q,r:r};
	}

	private function testWithoutQueryParam( q, val ){
		var q = queryExecute(
			"SELECT * from q where num = '#val#'",
			[],
			{dbtype="query", result="r"}
		);
		return {q:q,r:r};
	}


	private function getQuery(type){
		var testQ = queryNew('num', type);
		queryAddRow(testQ);
		testQ['num'][1] = '996012564777658757';
		queryAddRow(testQ);
		testQ['num'][2] = '996012564777658758';
		queryAddRow(testQ);
		/*
		testQ['num'][3] = '996012564777658759';
		queryAddRow(testQ);
		testQ['num'][4] = '996012564777658760';
		queryAddRow(testQ);
		testQ['num'][5] = '996012564777658761';
		*/
		return testQ
	}

}
