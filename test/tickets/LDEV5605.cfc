component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {
		describe( title="Test inline component exception has stacktrace", body=function() {

			it(title="test stacktrace for inline component", body = function( currentSpec ) {
				var z = new component {
					function test(){
						throw "ERROR IN INLINE COMPONENT METHOD"; // stack trace should start here
					}
				}
				function test(){
					dump( z.test() ); // but stack trace starts here
				}
				var v = false;
				try {
					test();
				} catch ( e ){
					v = e;
				}
				expect( v ).toBeStruct();
				expect( v.tagContext[ 1 ].line ).toBe( 9 );
				expect( v.tagContext[ 1 ].template ).toInclude( "LDEV5605.cfc" );

			});
	
		});
	}

}