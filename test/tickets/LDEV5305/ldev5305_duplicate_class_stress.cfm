<cfscript>
// LDEV-5305: duplicate class definition stress test
// Try to reproduce the bug by running many includes in parallel threads
// easier to repo standlaone with script-runner

if ( structKeyExists(url, "randomTestFile") ) {
	randomTestFile = url.randomTestFile;
} else {
	randomTestFile = "ldev5305.cfm";
}

threadResults = [];
numThreads = 20;

for (i = 1; i <= numThreads; i++) {
	thread name = "ldev5305_#i#" {
		mappingName = "ldev5305_testmap_" & i;
		mappingPath = getDirectoryFromPath(getCurrentTemplatePath());
		// Lock around mapping update for thread safety
		lock name="ldev5305_mapping_update" type="exclusive" timeout="10" {
			var mappings = GetApplicationSettings().mappings;
			mappings[mappingName] = mappingPath;
			// report the number of mappings before update
			systemOutput("Current mappings count: " & structCount(mappings));
			application action="update" mappings="#mappings#";
		}
		// Lock around reading mappings to ensure consistency
		lock name="ldev5305_mapping_update" type="readonly" timeout="10" {
			var currentMappings = GetApplicationSettings().mappings;
		}
		var mappedPath = "/#mappingName#/ldev5305.cfm";
		if (fileExists(mappedPath)) {
			include mappedPath;
		}
	
	}
}

// Wait for all threads to finish
thread action="join" throwonError="true";
echo("LDEV-5305: passed");
</cfscript>
