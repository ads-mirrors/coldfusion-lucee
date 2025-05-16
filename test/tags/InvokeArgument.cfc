component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults , testBox ) {
		describe( title="Test suite for cfinvokeargument with types", body=function() {

			it("should not cast numbers to strings", function() {
				var num =  42;
				var str = 42;
				invoke component="invoke.InvokeArgumentTest" method="testArgCasting" returnVariable="local.result" {
					invokeargument name="str" value=str;
					invokeargument name="num" value=num;
				}
				expect( result.str.getClass().getName() ).toBe ( str.getClass().getName() );
				expect( result.num.getClass().getName() ).toBe ( num.getClass().getName() );
			});

			it("should not cast numbers to strings", function() {
				var num =  "42";
				var str = "42";
				invoke component="invoke.InvokeArgumentTest" method="testArgCasting" returnVariable="local.result" {
					invokeargument name="str" value=str;
					invokeargument name="num" value=num;
				}
				expect( result.str.getClass().getName() ).toBe ( str.getClass().getName() );
				expect( result.num.getClass().getName() ).toBe ( num.getClass().getName() );
			});

			it("should not cast numbers to strings", function() {
				var args = {
					num:  42,
					str: 42
				};
				invoke component="invoke.InvokeArgumentTest" method="testArgCasting" returnVariable="local.result" {
					invokeargument name="str" value=args.str;
					invokeargument name="num" value=args.num;
				}
				expect( result.str.getClass().getName() ).toBe ( args.str.getClass().getName() );
				expect( result.num.getClass().getName() ).toBe ( args.num.getClass().getName() );
			});


		});

		describe( title="Test suite for cfinvokeargument without types", body=function() {

			it("should not cast numbers to strings", function() {
				var num =  42;
				var str = 42;
				invoke component="invoke.InvokeArgumentTest" method="testArgCastingNoTypes" returnVariable="local.result" {
					invokeargument name="str" value=str;
					invokeargument name="num" value=num;
				}
				expect( result.str.getClass().getName() ).toBe ( str.getClass().getName() );
				expect( result.num.getClass().getName() ).toBe ( num.getClass().getName() );
			});

			it("should not cast strings to numbers", function() {
				var num =  "42";
				var str = "42";
				invoke component="invoke.InvokeArgumentTest" method="testArgCastingNoTypes" returnVariable="local.result" {
					invokeargument name="str" value=str;
					invokeargument name="num" value=num;
				}
				expect( result.str.getClass().getName() ).toBe ( str.getClass().getName() );
				expect( result.num.getClass().getName() ).toBe ( num.getClass().getName() );
			});

			it("should not cast numbers to strings", function() {
				var args = {
					num:  42,
					str: 42
				};
				invoke component="invoke.InvokeArgumentTest" method="testArgCastingNoTypes" returnVariable="local.result" {
					invokeargument name="str" value=args.str;
					invokeargument name="num" value=args.num;
				}
				expect( result.str.getClass().getName() ).toBe ( args.str.getClass().getName() );
				expect( result.num.getClass().getName() ).toBe ( args.num.getClass().getName() );
			});


		});
	}

}