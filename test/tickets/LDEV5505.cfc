component extends="org.lucee.cfml.test.LuceeTestCase"  {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-5505", function() {

			it( title="checking ObjectEquals() simple string, different case", body=function( currentSpec ) {
				expect(ObjectEquals(
					left="PHONe",
					right="PHONE",
					caseSensitive=true
				)).toBeFalse();

				expect(ObjectEquals(
					left="PHONe",
					right="PHONE",
					caseSensitive=false
				)).toBeTrue();
			});

			it( title="checking ObjectEquals() simple struct same, different case", body=function( currentSpec ) {
				expect(ObjectEquals(
					left={ id: 1, name: 'lucee' },
					right={ id: 1, name: 'Lucee' },
					caseSensitive=true
				)).toBeFalse();

				expect(ObjectEquals(
					left={ id: 1, name: 'lucee' },
					right={ id: 1, name: 'Lucee' },
					caseSensitive=false
				)).toBeTrue();
			});

		});
	}
}