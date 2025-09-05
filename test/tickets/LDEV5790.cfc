component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" skip=true {
	function run( testResults , testBox ) {

		describe( title="LDEV-5790 qoq with varchar col", body=function() {

			it(title="checking with VARCHAR param", body = function( currentSpec ) {
				test( "varchar", "varchar" );
			});

			it(title="checking with INT param", body = function( currentSpec ) {
				test( "varchar", "int" );
			});

			it(title="checking with BIGINT param", body = function( currentSpec ) {
				test( "varchar", "bigint" );
			});

			it(title="checking with FLOAT param", body = function( currentSpec ) {
				test( "varchar", "float" );
			});
		});

		describe( title="LDEV-5790 qoq with BIGINT col", body=function() {

			it(title="checking with VARCHAR param", body = function( currentSpec ) {
				test( "bigint", "varchar" );
			});

			it(title="checking with INT param", body = function( currentSpec ) {
				test( "bigint", "int" );
			});

			it(title="checking with BIGINT param", body = function( currentSpec ) {
				test( "bigint", "bigint" );
			});

			it(title="checking with FLOAT param", body = function( currentSpec ) {
				test( "bigint", "float" );
			});
		});
	}

	private function test ( colDataType, clauseDataType ){
		var q = getQuery( colDataType );
		expect( getMetadata( q )[ 1 ].typeName ) .toBe( arguments.colDataType ) ;
		var val = q.num[ 1 ];
		var r1 = testWithQueryParam( q, clauseDataType, val );
		var r2 = testWithoutQueryParam( q, val );
		expect( r1.q.recordCount ).toBe(1);
		expect( r2.q.recordCount ).toBe(1);
		expect( r1.q.num[ 1 ] ).toBe( val );
		expect( r2.q.num[ 1 ] ).toBe( val );
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
