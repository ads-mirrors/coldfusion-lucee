component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		application action="update" preciseMath="false";
	}

	function afterAll(){
		application action="update" preciseMath="false";
	}

	function run( testResults , testBox ) {
		describe( title="LDEV-5793 large numbers comparison - preciseMath=false", body=function() {
			
			beforeEach(function() {
				application action="update" preciseMath="false";
			});

			describe( title="left < right", body=function() {
				xit(title="EQ", body = function( data ) {
					expect(testEQ_LT()).toBeFalse(); // fails 6.2, passes 5.4
				});

				xit(title="LT", body = function( data ) {
					expect(testLT_LT()).toBeTrue(); // left should be less than right
				});

				it(title="LTE", body = function( data ) {
					expect(testLTE_LT()).toBeTrue(); // left should be less than or equal to right
				});

				it(title="GT", body = function( data ) {
					expect(testGT_LT()).toBeFalse(); // left should not be greater than right
				});

				xit(title="GTE", body = function( data ) {
					expect(testGTE_LT()).toBeFalse(); // left should not be greater than or equal to right
				});
			});

			describe( title="left = right", body=function() {
				it(title="EQ", body = function( data ) {
					expect(testEQ_EQ()).toBeTrue(); // should be true when equal
				});

				it(title="LT", body = function( data ) {
					expect(testLT_EQ()).toBeFalse(); // should be false when equal
				});

				it(title="LTE", body = function( data ) {
					expect(testLTE_EQ()).toBeTrue(); // should be true when equal
				});

				it(title="GT", body = function( data ) {
					expect(testGT_EQ()).toBeFalse(); // should be false when equal
				});

				it(title="GTE", body = function( data ) {
					expect(testGTE_EQ()).toBeTrue(); // should be true when equal
				});
			});

			describe( title="left > right", body=function() {
				xit(title="EQ", body = function( data ) {
					expect(testEQ_GT()).toBeFalse(); // should be false when left > right
				});

				it(title="LT", body = function( data ) {
					expect(testLT_GT()).toBeFalse(); // should be false when left > right
				});

				xit(title="LTE", body = function( data ) {
					expect(testLTE_GT()).toBeFalse(); // should be false when left > right
				});

				xit(title="GT", body = function( data ) {
					expect(testGT_GT()).toBeTrue(); // should be true when left > right
				});

				it(title="GTE", body = function( data ) {
					expect(testGTE_GT()).toBeTrue(); // should be true when left > right
				});
			});
		});

		describe( title="LDEV-5793 large numbers comparison - preciseMath=true", body=function() {
			
			beforeEach(function() {
				application action="update" preciseMath="true";
			});

			afterEach(function() {
				application action="update" preciseMath="false";
			});

			describe( title="left < right", body=function() {
				it(title="EQ", body = function( data ) {
					expect(testEQ_LT()).toBeFalse();
				});

				it(title="LT", body = function( data ) {
					expect(testLT_LT()).toBeTrue();
				});

				it(title="LTE", body = function( data ) {
					expect(testLTE_LT()).toBeTrue();
				});

				it(title="GT", body = function( data ) {
					expect(testGT_LT()).toBeFalse();
				});

				it(title="GTE", body = function( data ) {
					expect(testGTE_LT()).toBeFalse();
				});
			});

			describe( title="left = right", body=function() {
				it(title="EQ", body = function( data ) {
					expect(testEQ_EQ()).toBeTrue();
				});

				it(title="LT", body = function( data ) {
					expect(testLT_EQ()).toBeFalse();
				});

				it(title="LTE", body = function( data ) {
					expect(testLTE_EQ()).toBeTrue();
				});

				it(title="GT", body = function( data ) {
					expect(testGT_EQ()).toBeFalse();
				});

				it(title="GTE", body = function( data ) {
					expect(testGTE_EQ()).toBeTrue();
				});
			});

			describe( title="left > right", body=function() {
				it(title="EQ", body = function( data ) {
					expect(testEQ_GT()).toBeFalse();
				});

				it(title="LT", body = function( data ) {
					expect(testLT_GT()).toBeFalse();
				});

				it(title="LTE", body = function( data ) {
					expect(testLTE_GT()).toBeFalse();
				});

				it(title="GT", body = function( data ) {
					expect(testGT_GT()).toBeTrue();
				});

				it(title="GTE", body = function( data ) {
					expect(testGTE_GT()).toBeTrue();
				});
			});
		});
	};

	// Test functions for left < right scenario
	private function testEQ_LT (){
		var	left = '996012564777658757';
		var right = '996012564777658758';
		return (left eq right);
	}

	private function testLT_LT (){
		var	left = '996012564777658757';
		var right = '996012564777658758';
		return (left lt right);
	}

	private function testLTE_LT (){
		var	left = '996012564777658757';
		var right = '996012564777658758';
		return (left lte right);
	}

	private function testGT_LT (){
		var	left = '996012564777658757';
		var right = '996012564777658758';
		return (left gt right);
	}

	private function testGTE_LT (){
		var	left = '996012564777658757';
		var right = '996012564777658758';
		return (left gte right);
	}

	// Test functions for left = right scenario
	private function testEQ_EQ (){
		var	left = '996012564777658757';
		var right = '996012564777658757';
		return (left eq right);
	}

	private function testLT_EQ (){
		var	left = '996012564777658757';
		var right = '996012564777658757';
		return (left lt right);
	}

	private function testLTE_EQ (){
		var	left = '996012564777658757';
		var right = '996012564777658757';
		return (left lte right);
	}

	private function testGT_EQ (){
		var	left = '996012564777658757';
		var right = '996012564777658757';
		return (left gt right);
	}

	private function testGTE_EQ (){
		var	left = '996012564777658757';
		var right = '996012564777658757';
		return (left gte right);
	}

	// Test functions for left > right scenario
	private function testEQ_GT (){
		var	left = '996012564777658758';
		var right = '996012564777658757';
		return (left eq right);
	}

	private function testLT_GT (){
		var	left = '996012564777658758';
		var right = '996012564777658757';
		return (left lt right);
	}

	private function testLTE_GT (){
		var	left = '996012564777658758';
		var right = '996012564777658757';
		return (left lte right);
	}

	private function testGT_GT (){
		var	left = '996012564777658758';
		var right = '996012564777658757';
		return (left gt right);
	}

	private function testGTE_GT (){
		var	left = '996012564777658758';
		var right = '996012564777658757';
		return (left gte right);
	}

}
