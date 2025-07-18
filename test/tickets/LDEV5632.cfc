component extends="org.lucee.cfml.test.LuceeTestCase" labels="javasettings"  {

	function beforeAll() {
		variables.loadPaths=getTempDirectory("LDEV5632");
		// create lib dir
		if (!directoryExists(variables.loadPaths)) {
			directoryCreate(variables.loadPaths);
		}
		
		var trg=variables.loadPaths&"dd-java-agent.jar";
		if(!fileExists(trg))
			fileCopy("https://dtdg.co/latest-java-tracer", trg);

		var trg=variables.loadPaths&"opentelemetry-api.jar";
		if(!fileExists(trg))
			fileCopy("https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-api/1.50.0/opentelemetry-api-1.50.0.jar", trg);

		var trg=variables.loadPaths&"opentelemetry-context.jar";
		if(!fileExists(trg))
			fileCopy("https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-context/1.50.0/opentelemetry-context-1.50.0.jar", trg);
	}

	function afterAll() {
		// remove lib dir
		if (isWindows()) return; // jars are locked on windows
		if (directoryExists(variables.loadPaths)) {
			directoryDelete(variables.loadPaths,true);
		}
	}


	function run( testResults , testBox ) {
		describe( title="test java settings set directly with createObject", body=function() {

			it(title="validate enviroment", body = function( currentSpec ) {
				expect( directoryExists(variables.loadPaths) ).toBeTrue();
				expect( directoryExists(contractPath(variables.loadPaths)) ).toBeTrue();
			});


			it(title="load object from [loadPaths]", body = function( currentSpec ) {
				var obj=createObject(
					type:"java", 
					class:"io.opentelemetry.api.trace.Span",
					context : { loadPaths = [contractPath(variables.loadPaths)] }
				).current();
				expect( getMetadata(obj).getName() ).toBe( "io.opentelemetry.api.trace.PropagatedSpan" );
			});
	
		});
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}

	
}