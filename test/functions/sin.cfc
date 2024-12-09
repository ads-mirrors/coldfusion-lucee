component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
	};

	function afterAll(){
		application action="update" preciseMath=variables.preciseMath;
	};

	public function run( testResults, testBox ) {
		describe( title="Testcase for sin() function", body=function() {

			
			beforeEach( function(){
				application action="update" preciseMath=variables.preciseMath;
			});

			afterEach( function(){
				application action="update" preciseMath=variables.preciseMath;
			});

			it(title="Checking the sin() function", body=function( currentSpec ) {
				var a = 90;
				expect(sin(a)).toBe(0.8939966636005579);
			});

			it(title="Checking the sin() member function", body=function( currentSpec ) {
				var a = 90;
				expect(a.sin()).toBe(0.8939966636005579);
			});

			it(title="Checking the sin() function result is numeric", body=function( currentSpec ) {
				var a = 90;
				expect(isnumeric(sin(a))).toBeTrue();
			});

			it(title="Checking the sin() function to string", body=function( currentSpec ) {
				var a = 90;
				application action="update" preciseMath=true;
				expect("#tostring(sin(a))#").toBe("0.8939966636005579");
				application action="update" preciseMath=false;
				expect("#tostring(sin(a))#").toBe("0.893996663601");
				
			});
		});
	}
}