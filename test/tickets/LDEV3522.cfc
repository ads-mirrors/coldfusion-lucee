component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, textbox ) {

		describe("LDEV-3522 cast/convert as date", function(){

			it(title="Can cast as date", body=function( currentSpec ){
				var qry = QueryNew('foo','integer',[[40]]);
				var actual = queryExecute(
					"SELECT cast( foo as date ) as asDate FROM qry",
					[],
					{dbtype="query"}
				);
				expect( actual.asDate ).toBeDate();
				expect( actual.asDate ).toBeInstanceOf( 'java.util.Date' );
			});

			it(title="Can convert as date", body=function( currentSpec ){
				var qry = QueryNew('foo','integer',[[40]]);
				var actual = queryExecute(
					"SELECT convert( foo, date ) as asDate FROM qry",
					[],
					{dbtype="query"}
				);
				expect( actual.asDate ).toBeDate();
				expect( actual.asDate ).toBeInstanceOf( 'java.util.Date' );;
			});

			it(title="Can convert as date - quoted", body=function( currentSpec ){
				var qry = QueryNew('foo','integer',[[40]]);
				var actual = queryExecute(
					"SELECT convert( foo, 'date' ) as asDate FROM qry",
					[],
					{dbtype="query"}
				);
				expect( actual.asDate ).toBeDate();
				expect( actual.asDate ).toBeInstanceOf( 'java.util.Date' );
			});
		});

		describe("LDEV-3522 cast/convert as string", function(){

			it(title="Can cast as string", body=function( currentSpec ){
				var qry = QueryNew('foo','date',[[now()]]);
				var actual = queryExecute(
					"SELECT foo,
							cast( foo as string ) as asString
					FROM qry",
					[],
					{dbtype="query"}
				);
				expect( actual.foo ).toBeDate();
				expect( actual.foo ).toBeInstanceOf( 'java.util.Date' );
				expect( actual.asString ).toBeString();
				expect( actual.asString ).toBeInstanceOf( 'java.lang.String' );
			});

			it(title="Can convert as string", body=function( currentSpec ){
				var qry = QueryNew('foo','date',[[now()]]);
				var actual = queryExecute(
					"SELECT foo,
							convert( foo, string ) as asString
					FROM qry",
					[],
					{dbtype="query"}
				);
				expect( actual.foo ).toBeDate();
				expect( actual.foo ).toBeInstanceOf( 'java.util.Date' );
				expect( actual.asString ).toBeString();
				expect( actual.asString ).toBeInstanceOf( 'java.lang.String' );
			});

			it(title="Can convert as string - quoted", body=function( currentSpec ){
				var qry = QueryNew('foo','date',[[now()]]);
				var actual = queryExecute(
					"SELECT foo,
							convert( foo, 'string' ) as asString
					FROM qry",
					[],
					{dbtype="query"}
				);
				expect( actual.foo ).toBeDate();
				expect( actual.foo ).toBeInstanceOf( 'java.util.Date' );
				expect( actual.asString ).toBeString();
				expect( actual.asString ).toBeInstanceOf( 'java.lang.String' );
			});
		});

		describe("LDEV-3522 cast/convert as number", function(){

			it(title="Can cast as number", body=function( currentSpec ){
				var qry = QueryNew('foo','string',[['40']]);
				var actual = queryExecute(
					"SELECT foo,
							cast( foo as number ) as asNumber,
							convert( foo, number ) as asNumber2,
							convert( foo, 'number' ) as asNumber3
					FROM qry",
					[],
					{dbtype="query"}
				);

				expect( actual.foo ).toBeString();
				expect( actual.foo ).toBeInstanceOf( 'java.lang.String' );
				expect( actual.asNumber ).toBeNumeric();
				expect( actual.asNumber ).toBeInstanceOf( 'java.lang.Number' ); // can be Double or BigDecimal
			});

			it(title="Can convert as number", body=function( currentSpec ){
				var qry = QueryNew('foo','string',[['40']]);
				var actual = queryExecute(
					"SELECT foo,
							convert( foo, number ) as asNumber
					FROM qry",
					[],
					{dbtype="query"}
				);

				expect( actual.foo ).toBeString();
				expect( actual.foo ).toBeInstanceOf( 'java.lang.String' );
				expect( actual.asNumber ).toBeNumeric();
				expect( actual.asNumber ).toBeInstanceOf( 'java.lang.Number' ); // can be Double or BigDecimal
			});

			it(title="Can convert as number - quoted", body=function( currentSpec ){
				var qry = QueryNew('foo','string',[['40']]);
				var actual = queryExecute(
					"SELECT foo,
							convert( foo, 'number' ) as asNumber
					FROM qry",
					[],
					{dbtype="query"}
				);

				expect( actual.foo ).toBeString();
				expect( actual.foo ).toBeInstanceOf( 'java.lang.String' );
				expect( actual.asNumber ).toBeNumeric();
				expect( actual.asNumber ).toBeInstanceOf( 'java.lang.Number' ); // can be Double or BigDecimal
			});


		});

		describe("LDEV-3522 cast/convert as boolean", function(){

			it(title="Can cast as boolean", body=function( currentSpec ){
				var qry = QueryNew('foo','string',[['true']]);
				var actual = queryExecute(
					"SELECT foo,
							cast( foo as boolean ) as asBoolean,
							convert( foo, boolean ) as asBoolean2,
							convert( foo, 'bool' ) as asBool3
					FROM qry",
					[],
					{dbtype="query"}
				);
				expect( actual.foo ).toBeString();
				expect( actual.foo ).toBeInstanceOf( 'java.lang.String' );
				expect( actual.asBoolean ).toBeBoolean();
				expect( actual.asBoolean ).toBeInstanceOf( 'java.lang.Boolean' );
			});

			
			it(title="Can convert as boolean", body=function( currentSpec ){
				var qry = QueryNew('foo','string',[['true']]);
				var actual = queryExecute(
					"SELECT foo,
							convert( foo, boolean ) as asBoolean
					FROM qry",
					[],
					{dbtype="query"}
				);
				expect( actual.foo ).toBeString();
				expect( actual.foo ).toBeInstanceOf( 'java.lang.String' );
				expect( actual.asBoolean ).toBeBoolean();
				expect( actual.asBoolean ).toBeInstanceOf( 'java.lang.Boolean' );
			});

			
			it(title="Can convert as boolean - quoted", body=function( currentSpec ){
				var qry = QueryNew('foo','string',[['true']]);
				var actual = queryExecute(
					"SELECT foo,
							convert( foo, 'bool' ) as asBoolean
					FROM qry",
					[],
					{dbtype="query"}
				);
				expect( actual.foo ).toBeString();
				expect( actual.foo ).toBeInstanceOf( 'java.lang.String' );
				expect( actual.asBoolean ).toBeBoolean();
				expect( actual.asBoolean ).toBeInstanceOf( 'java.lang.Boolean' );
			});

		});

		describe("LDEV-3522 cast/convert as xml", function(){

			it(title="Can cast as xml", body=function( currentSpec ){
				var qry = QueryNew('foo','string',[['<root brad="wood" />']]);
				var actual = queryExecute(
					"SELECT foo,
							cast( foo as xml ) as asXML
					FROM qry",
					[],
					{dbtype="query"}
				);
				expect( isXML( actual.foo ) ).toBeTrue();
				expect( isXMLDoc( actual.foo ) ).toBeFalse();
				expect( isXML( actual.asXML ) ).toBeTrue();
				expect( isXMLDoc( actual.asXML ) ).toBeTrue();
			});

			it(title="Can convert as xml", body=function( currentSpec ){
				var qry = QueryNew('foo','string',[['<root brad="wood" />']]);
				var actual = queryExecute(
					"SELECT foo,
							convert( foo, xml ) as asXML
					FROM qry",
					[],
					{dbtype="query"}
				);
				expect( isXML( actual.foo ) ).toBeTrue();
				expect( isXMLDoc( actual.foo ) ).toBeFalse();
				expect( isXML( actual.asXML ) ).toBeTrue();
				expect( isXMLDoc( actual.asXML ) ).toBeTrue();
			});

			it(title="Can convert as xml - quoted", body=function( currentSpec ){
				var qry = QueryNew('foo','string',[['<root brad="wood" />']]);
				var actual = queryExecute(
					"SELECT foo,
							convert( foo, 'xml' ) as asXML
					FROM qry",
					[],
					{dbtype="query"}
				);
				expect( isXML( actual.foo ) ).toBeTrue();
				expect( isXMLDoc( actual.foo ) ).toBeFalse();
				expect( isXML( actual.asXML ) ).toBeTrue();
				expect( isXMLDoc( actual.asXML ) ).toBeTrue();
			});
		});

		describe("Testcase for LDEV-3522", function() {
			variables.datas = queryNew("id,value","integer,double",[[0,19.22]]);

			xit( title="QOQ cast function, cast to INT", body=function( currentSpec ){
				var QOQInt = queryExecute( "SELECT CAST(datas.value AS INT) AS valueint FROM datas",  {}, { dbtype="query" } );
				expect( QOQInt.valueint ).toBe( 19 );
			});
			xit( title="QOQ cast function, cast to BIT", body=function( currentSpec ){
				var QOQBit = queryExecute( "SELECT CAST(datas.id AS BIT) AS valueBit, CAST(datas.value AS BIT) AS valueBit1 FROM datas", {}, { dbtype="query" } );
				expect( QOQBit.valueBit ).toBeTypeOf( "integer" );
				expect( QOQBit.valueBit1 ).toBeTypeOf( "integer" );
			});
			it( title="QOQ cast function, cast to DATE", body=function( currentSpec ){
				var QOQDate = queryExecute( "SELECT CAST('2222222' AS DATE) AS valueDate FROM datas", {}, { dbtype="query" } );
				expect( isDate(QOQDate.valueDate) ).toBe(true);
			});
			xit( title="QOQ convert function, convert to INT", body=function( currentSpec ){
				var QOQConvInt = queryExecute( "SELECT Convert(datas.value, INT) AS valueint FROM datas",  {}, { dbtype="query" } );
				expect( QOQConvInt.valueint ).toBe( 19 );
			});
			xit( title="QOQ convert function, convert to BIT", body=function( currentSpec ){
				var QOQConvBit = queryExecute( "SELECT CONVERT(datas.id, BIT) AS valueBit, CONVERT(datas.value,  BIT) AS valueBit1 FROM datas", {}, { dbtype="query" } );
				expect( QOQConvBit.valueBit ).toBeTypeOf( "integer" );
				expect( QOQConvBit.valueBit1 ).toBeTypeOf( "integer" );
			});
			it( title="QOQ convert function, convert to DATE", body=function( currentSpec ){
				var QOQConvDate = queryExecute( "SELECT CONVERT('2017-08-29', DATE) AS valueDate FROM datas", {}, { dbtype="query" } );
				expect(isDate(QOQConvDate.valueDate)).toBe(true);
			});
		});

	}

}