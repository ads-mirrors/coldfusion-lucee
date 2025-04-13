component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		describe("cfexecute", function() {
			
			it(title="cfexecute result variable with exit code",  body=function() {
				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? "/c exit /B 7" : "-c 'exit 7'";

				cfexecute(name=exe, timeout="1", arguments=args , result="local.result");
				expect( result.exitCode ).toBe( 7 );
				expect( result ).toHaveKey( "output" );
				expect( result ).toHaveKey( "error" );
			});

			it(title="cfexecute exitCode variable",  body=function() {
				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? "/c exit /B 7" : "-c 'exit 7'";

				cfexecute(name=exe, timeout="1", arguments=args , variable="local.result", exitCodeVariable="local.exitCode");
				expect( exitCode ).toBe( 7 );

			});

		});
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}