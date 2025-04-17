<cfcomponent output="false">
	<cffunction name="test" access="remote" output="false" returntype="query" returnformat="json">
		<cfscript>
			//systemOutput(getApplicationSettings().serialization, true);
			return queryNew(
				"Id,Name",
				"numeric,varchar",
				{
					id: [1,2,3],
					name: ['Neo','Trinity','Morpheus']
				}
				);
		</cfscript>
	</cffunction>
</cfcomponent>
