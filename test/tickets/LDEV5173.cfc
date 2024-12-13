component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults, testBox ){
		describe( "Test for LDEV-5173", function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("IST");
			});
			afterEach( function(){
				setTimeZone(variables.startingTZ?:"UTC");
			});
			it( title="round trip DateTimeFormat/ParseDateTime with format 'epoch'", body=function( currentSpec ) {
				var date = DateTimeFormat( datetime="2024/12/11 16:19:54", mask="epoch" );
				expect(toString(parseDateTime(date=date, format="epoch"))).toBe("{ts '2024-12-11 16:19:54'}");
				expect( parseDateTime( date=date, format="epoch" ) ).toBe( date );
			});

			it( title="round trip DateTimeFormat/ParseDateTime with format 'epochms'", body=function( currentSpec ) {
				var date = DateTimeFormat( datetime="2024/12/11 16:19:54", mask="epochms" );
				expect(toString(parseDateTime(date=date, format="epochms"))).toBe("{ts '2024-12-11 16:19:54'}");
				expect( parseDateTime( date=date, format="epochms" ) ).toBe( date );
			});
		} );
	}
}
