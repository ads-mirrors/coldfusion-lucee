<cfscript>
	data = queryNew("Id, DateJoined", "INTEGER, TIMESTAMP", [
		{ID=1, DateJoined="2017-01-03 10:57:54"},
		{ID=2, DateJoined="2017-01-03 10:57:54"},
		{ID=3, DateJoined="2017-01-03 10:57:54"}
	]);
	echo(serializeJSON( data ));
</cfscript>