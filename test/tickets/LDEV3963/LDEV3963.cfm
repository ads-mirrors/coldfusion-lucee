<cfscript>
	try {
		function foo() secured:api {}; // secured:api works in tag syntax
		writeOutput(structKeyExists(getMetadata( foo ),"secured:api"))
	}
	catch(any e) {
		writeOutput(e.stracktrace) 
	}
</cfscript>