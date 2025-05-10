component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	/* 
		To run only this test, add the following command line argument:

		mvn test -DtestFilter='LDEV5568'

		or

		mvn test -DtestFilter='LDEV5568' -DtestDebugger='true' -DtestDebug='true'
	*/
	function run( testResults , testBox ) {
		
		describe( title="[LDEV-5568] isDate() should return false for invalid day ranges", body=function() {

			it( title="Should not accept Feb 30, 2008", body=function( currentSpec ) {
				var input = "Feb 30, 2008";

				var actual = isDate(input);

				expect(isDate(input)).toBeFalse();
			});

			it( title="Should not accept Mar 32, 2008", body=function( currentSpec ) {
				var input = "Mar 32, 2008";

				var actual = isDate(input);

				expect(isDate(input)).toBeFalse();
			});

			it( title="Should not accept 2008-02-30 00:00:00", body=function( currentSpec ) {
				var input = "2008-02-30 00:00:00";

				var actual = isDate(input);

				expect(isDate(input)).toBeFalse();
			});

		});
	}
}
