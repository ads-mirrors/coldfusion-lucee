component extends="org.lucee.cfml.test.LuceeTestCase" {
	/* 
		To run only this test, add the following command line argument:

		mvn test -DtestFilter='LDEV5566'

		or

		mvn test -DtestFilter='LDEV5566' -DtestDebugger='true' -DtestDebug='true'
	*/

	function run( testResults , testBox ) {

		describe( title="[LDEV-5566] Incorrect parsing of milliseconds in string date", body=function() {

			it( title="ISO-8601 dates as string should correct parse milliseconds with leading zeros", body=function( currentSpec ) {
				expect(dateTimeFormat("2010-04-30T22:26:00.003", "yyyy-mm-dd HH:nn:ss.lll")).toBe("2010-04-30 22:26:00.003");
				expect(dateTimeFormat("2010-04-30T22:26:00.083", "yyyy-mm-dd HH:nn:ss.lll")).toBe("2010-04-30 22:26:00.083");
				expect(dateTimeFormat("2010-04-30T22:26:00.983", "yyyy-mm-dd HH:nn:ss.lll")).toBe("2010-04-30 22:26:00.983");
				expect( millisecond(parseDateTime("2010-04-30T22:26:00.083")) ).toBe(83);
				expect( millisecond(parseDateTime("2010-04-30T22:26:00.008")) ).toBe(8);
			});

			it( title="ISO-8601-like dates as string should correct parse milliseconds with leading zeros", body=function( currentSpec ) {
				expect(dateTimeFormat("2010-04-30 22:26:00.003", "yyyy-mm-dd HH:nn:ss.lll")).toBe("2010-04-30 22:26:00.003");
				expect(dateTimeFormat("2010-04-30 22:26:00.083", "yyyy-mm-dd HH:nn:ss.lll")).toBe("2010-04-30 22:26:00.083");
				expect(dateTimeFormat("2010-04-30 22:26:00.983", "yyyy-mm-dd HH:nn:ss.lll")).toBe("2010-04-30 22:26:00.983");
			});

			it( title='test nanoseconds', body=function( currentSpec ) {
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.0002")) ).toBe(000);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.0005")) ).toBe(001);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.00002")) ).toBe(000);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.000002")) ).toBe(000);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.0000002")) ).toBe(000);
			});

			it( title='From LDEV-4825: test millisecond fraction (with parseDateTime)', body=function( currentSpec ) {
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.6")) ).toBe(600);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.60")) ).toBe(600);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.600")) ).toBe(600);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.6000")) ).toBe(600);
			});

			it( title='From LDEV-4825: test millisecond fraction (with parseDateTime)', body=function( currentSpec ) {
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.6")) ).toBe(600);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.60")) ).toBe(600);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.600")) ).toBe(600);
				expect( millisecond(parseDateTime("2024-04-04 00:00:00.6000")) ).toBe(600);
			});

			it( title='From LDEV-4825: test millisecond fraction (with lsParseDateTime)', body=function( currentSpec ) {
				expect( millisecond(lsParseDateTime("2024-04-04 00:00:00.6")) ).toBe(600);
				expect( millisecond(lsParseDateTime("2024-04-04 00:00:00.60")) ).toBe(600);
				expect( millisecond(lsParseDateTime("2024-04-04 00:00:00.600")) ).toBe(600);
				expect( millisecond(lsParseDateTime("2024-04-04 00:00:00.6000")) ).toBe(600);
			});

			it( title='From LDEV-4825: test millisecond fraction (with lsParseDateTime)', body=function( currentSpec ) {
				expect( millisecond(lsParseDateTime("2024-04-04 00:00:00.6")) ).toBe(600);
				expect( millisecond(lsParseDateTime("2024-04-04 00:00:00.60")) ).toBe(600);
				expect( millisecond(lsParseDateTime("2024-04-04 00:00:00.600")) ).toBe(600);
				expect( millisecond(lsParseDateTime("2024-04-04 00:00:00.6000")) ).toBe(600);
			});
		});
	}
}
