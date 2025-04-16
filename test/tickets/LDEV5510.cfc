component extends="org.lucee.cfml.test.LuceeTestCase" labels="execute" skip=true {

	function beforeAll(){
		variables.exe = isWindows() ? "cmd" : "bash";
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath());
	}

	function run() {
		// when there is a only an onProgress listener, the error stream is combined
		describe("cfexecute onProgress interactive", function() {

			beforeEach(function(currentSpec){
				_logger("", true);
				_logger(currentSpec, true);
			});
	
			it(title="cfexecute react to prompt",  body=function() {

				var args = isWindows() ? "/c echo plzRespond | set /p var="
					: "-c 'echo ""plzRespond"" | while read line; do echo $line; done'";
				var outputLog = [];
				var onCombinedProgressListener = function( output, process ){
					_logger(arguments, true);
					arrayAppend( outputLog, output );
					_logger("onProgress " & arguments.output, true );
					var os = process.getOutputStream();
					os.write( "\n".getBytes() ); // press a key
					_logger("written to outputStream", true );
					os.flush();
					os.close();
				};

				cfexecute(name=exe, timeout="2", arguments=args , directory=dir,
					result="local.result",
					onProgress=onCombinedProgressListener
				);
				expect( result.exitCode ).toBe( 0 );
				expect( outputLog ).notToBeEmpty();
			});

			it(title="cfexecute timeout due to not reacting to prompt",  body=function() {

				// prompt needs to read from stdin
				var args = isWindows() ? "/c echo plzDontRespond | set /p var="
					: "-c 'echo ""plzDontRespond"" | while read line; do echo $line; done'";
				var outputLog = [];
				var onCombinedProgressListener = function( output, process ){
					_logger(arguments, true);
					arrayAppend( outputLog, output );
					_logger("onProgress " & arguments.output, true );
				};

				expect(function(){
					cfexecute(name=exe, timeout="2", arguments=args , directory=dir,
						result="local.result",
						onProgress=onCombinedProgressListener
					);
					_logger( result, true );
				}).toThrow();
				
			});


		});

	}

	private function _logger( mess, nl, err=false ){
		// uncomment for debugging
		systemOutput( mess, nl, err );
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}