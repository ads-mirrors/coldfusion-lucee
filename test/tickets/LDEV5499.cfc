component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		describe("cfexecute", function() {
			
			it(title="cfexecute environment windows",  body=function() {
				var env = [ "LUCEE=rocks", "LUCEE_LDEV5499_RANDOM=#createGUID()#" ];

				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? "/c set" : "-c 'set'";

				cfexecute(name=exe, timeout="1", arguments=args , environment=env, variable="variables.result");
				expect( find( env[ 1 ], result ) ).toBeGT( 0 ); // don't leak env
				expect( find( env[ 2 ], result ) ).toBeGT( 0 ); // don't leak env

			});

		});
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}