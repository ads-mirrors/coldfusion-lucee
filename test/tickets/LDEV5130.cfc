component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {
		describe( title='LDEV-5130', body=function(){
			it( title='regular addition', body=function() {
				var x = 1;
				var y = 1;
				expect( compare((x+&y)&"","2") ).toBe(0);
				
			});

			it( title='int addition', body=function() {
				var x = 1;
				var y = 1;
				expect( compare((int(x)+y)&"","2") ).toBe(0);
				expect( compare((x+int(y))&"","2") ).toBe(0);
				expect( compare((int(x)+int(y))&"","2") ).toBe(0);
				
			});

			it( title='regular addition', body=function() {
				var x = 1;
				var y = 1;
				expect( compare(Evaluate('1+1')&"","2") ).toBe(0);
				expect( compare(Evaluate('x+y')&"","2") ).toBe(0);
				
			});
			it( title='int addition', body=function() {
				var x = 1;
				var y = 1;
				expect( compare(Evaluate('int(1)+1')&"","2") ).toBe(0);
				expect( compare(Evaluate('1+int(1)')&"","2") ).toBe(0);
				expect( compare(Evaluate('int(1)+int(1)')&"","2") ).toBe(0);
				expect( compare(Evaluate('int(x)+y')&"","2") ).toBe(0);
				expect( compare(Evaluate('x+int(y)')&"","2") ).toBe(0);
				expect( compare(Evaluate('int(x)+int(y)')&"","2") ).toBe(0);
				
			});
		});
	}
}