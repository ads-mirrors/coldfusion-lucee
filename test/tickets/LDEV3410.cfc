component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults, testBox ){
		describe(title="Testcase for LDEV-3410", body=function( currentSpec ) {
			it(title="Check SerializeJSON(component) preserves boolean types for defaults", body=function( currentSpec )  {

				var test = new component accessors=true {
					property name="booleanValue" type=boolean default=true;
					property name="booleanValue1" type=boolean default=false;
				};

				local.result = serializeJSON(Test);
				expect(local.result).toBe("{""booleanValue1"":true,""booleanValue2"":false}");
			});
		});
	}
}
