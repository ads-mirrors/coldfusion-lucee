
<script>
	function scroll(){
		window.scrollTo(0, document.body.scrollHeight);
	}
</script>
<style>
	.error {
		font-weight: bolder;
		color: red;
	}
</style>
<cfscript>
	setting requestTimeout="#60*10#";
	function build(){
		var exe = "cmd";
		var start = getTickCount();
		var args = [ "/c", "ant", "fast" ];
		var dir = expandPath("../../loader/");
		_logger( dir );
		_logger( args.toJson() );
		//var outputLog = [];
		var onProgressListener = function( output ){
			//_logger(arguments, true );
			//arrayAppend(outputLog, output);
			// _logger("COMBINED PROGRESS " & arguments.output, true );
			_logger( numberFormat( (getTickCount() - start ) / 1000 ) & chr(9) & arguments.output );
		};
		var onErrorListener = function( output ){
			//_logger(arguments, true );
			//arrayAppend(outputLog, output);
			// _logger("COMBINED PROGRESS " & arguments.output, true );
			_logger( numberFormat( (getTickCount() - start )/1000 ) & chr(9) & arguments.output, true );
		};
		lock name="build-lucee" type="exclusive" timeout="1" throwOnTimeout="true" {
			cfexecute(name=exe, timeout="7200", arguments=args , directory=dir,
				result="local.result",
				onProgress=onProgressListener,
				onError=onErrorListener
			);
		}
		return result;
	}

	c = 0;

	function _logger(message, error=false){
		if (error) echo("<span class='error'>");
		echo( arguments.message & chr(10 ));
		systemOutput( arguments.message, true, error );
		if (error) echo("</span>")
		echo("<script>scroll();</script>")
		c++;
		//cfflush(); // this stops outputting after a while???
	}

	echo("<pre>");
	result = build();
	echo("</pre>");
	dump( result );
	echo("<script>scroll();</script>")
	cfflush();
</cfscript>