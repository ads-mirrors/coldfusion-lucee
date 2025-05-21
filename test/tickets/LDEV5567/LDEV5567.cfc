component extends="org.lucee.cfml.test.LuceeTestCase" {
	/* 
		To run only this test, add the following command line argument:

		mvn test -DtestFilter='LDEV5567'

		or

		mvn test -DtestFilter='LDEV5567' -DtestDebugger='true' -DtestDebug='true'
	*/
	function run( testResults , testBox ) {
		
		describe( title="[LDEV-5567] dateTimeFormat() returns wrong value for 'WW' mask", body=function() {

			it( title="Should return 2 digits for WW mask",skip=true body=function( currentSpec ) {
				var timestamp = createDateTime(2018, 1, 21, 10, 0, 0);

				var actual = dateTimeFormat(timestamp, "'['WW']'", 'GMT');

				expect(actual).toBe("[04]");
			});

			it( title="Should return 1 digits for W mask", body=function( currentSpec ) {
				var timestamp = createDateTime(2018, 1, 21, 10, 0, 0);

				var actual = dateTimeFormat(timestamp, "'['W']'", 'GMT');

				expect(actual).toBe("[4]");
			});

			it( title="Should return 2 digits for FF mask", body=function( currentSpec ) {
				var timestamp = createDateTime(2018, 1, 21, 10, 0, 0);

				var actual = dateTimeFormat(timestamp, "'['FF']'", 'GMT');

				expect(actual).toBe("[03]");
			});

			it( title="Should return 1 digits for F mask", body=function( currentSpec ) {
				var timestamp = createDateTime(2018, 1, 21, 10, 0, 0);

				var actual = dateTimeFormat(timestamp, "'['F']'", 'GMT');

				expect(actual).toBe("[3]");
			});

		});
	}
}
