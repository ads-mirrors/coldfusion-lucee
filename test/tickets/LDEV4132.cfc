component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults, testBox ) {
		describe( title = "Testcase for LDEV-4132", body = function() {

			it( title = "Checking listFirst() member function", body = function( currentSpec ) {
				var normalPrio = createObject("java", "edu.umd.cs.findbugs.Priorities","findbugsAnnotations","3.0.1").NORMAL_PRIORITY;
				expect(normalPrio).toBe(2);
			});
		});
	}
}
