component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		application action="update" preciseMath="false";
	}

	function afterAll(){
		application action="update" preciseMath="false";
	}

	function run( testResults , testBox ) {
		describe( title="LDEV-5793 large numbers should not be considered equal", body=function() {
			xit(title="preciseMath=false", body = function( data ) {
				expect(test()).toBeFalse(); // fails 6.2, passes 5.4
			});

			it(title="preciseMath=true", body = function( data ) {
				application action="update" preciseMath="true";
				expect(test()).toBeFalse();
				application action="update" preciseMath="false";
			});
		});
	};

	private function test (){
		var	left = '996012564777658757';
		var right = '996012564777658758';
		return (left eq right)
	}

}
