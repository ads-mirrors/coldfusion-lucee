component extends="org.lucee.cfml.test.LuceeTestCase" skip=true labels="thread" {

	function run( testResults, testBox ){

		describe( "LDEV-3116 thread scope cannot be modified from outside the owner thread", function(){

			it( title="thread without child thread", body=function(){
				thread name="ldev3116_simple" action="run" {
					thread.testing = 'blah';
				}
				thread action="join" name="ldev3116_simple";
				expect( cfthread.ldev3116_simple ).notToHaveKey( "error", cfthread.ldev3116_simple.error.stacktrace?: '???' );
				expect( cfthread.ldev3116_simple.testing ).toBe( "blah" );
			} );

			it( title="thread with child thread (arrayEach parallel=true)", skip=true, body=function(){
				systemOutput("", true);
				systemOutput("before thread: " & getPageContext().getThread().getName(), true);
				thread name="ldev3116" action="run" {
					systemOutput("before arrayEach: " & getPageContext().getThread().getName(), true);
					[1].each(function(key){
						var test = key;
						systemOutput("arrayEach: " & getPageContext().getThread().getName(), true);
					}, true);
					systemOutput("post arrayEach: " & getPageContext().getThread().getName(), true);
					thread.testing = 'blah'; // this fails because the ct != pc.getThread() after arrayEach
				}
				systemOutput("post thread: " & getPageContext().getThread().getName(), true);
				thread action="join" name="ldev3116";
				systemOutput("post thread join: " & getPageContext().getThread().getName(), true);
				expect( cfthread.ldev3116 ).notToHaveKey( "error", cfthread.ldev3116.error.stacktrace?: '???' );
				expect( cfthread.ldev3116.testing ).toBe( "blah" );
			} );

			it( title="thread without child thread (arrayEach parallel=false)", body= function(){
				thread name="ldev3116_without" action="run" {
					[1].each(function(key){
						var test = key;
					}, false);
					thread.testing = 'blah';
				}
				thread action="join" name="ldev3116_without";
				//systemOutput(cfthread.test_without,1,1);
				expect( cfthread.ldev3116_without ).notToHaveKey( "error",  cfthread.ldev3116_without.error.stacktrace?: '???' );
				expect( cfthread.ldev3116_without.testing ).toBe( "blah" );
			} );

			it( title="no thread - arrayEach parallel=true", body=function(){
				var threadBefore = getPageContext().getThread().getName();
				[1].each(function(key){
					var test = key;
				}, true);
				expect( getPageContext().getThread().getName() ).toBe( threadBefore );
			} );

		} );
	}

}
