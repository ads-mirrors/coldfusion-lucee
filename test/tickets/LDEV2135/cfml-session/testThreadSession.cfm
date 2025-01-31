<cfscript>
	session.ldev3125 = {};
	session.ldev3125.sessionCluster = url.sessionCluster;
	session.ldev3125.before = 'survived';
	session.ldev3125.threads = [];
	session.ldev3125.sessionStorage = url.sessionStorage;

	_threads = [];

	for(i in [ 1, 2, 3, 4, 5 ] ) {
		name = "ldev2135-#i#";
		arrayAppend( _threads, name);
		thread name="#name#" {
			try {
				ArrayAppend( session.ldev3125.threads, thread.name );
				sleep( 10 );
				throw(type="blah", message="boom");
			} catch(any e) {
				writedump(session.ldev3125);
			}
		}
	}

	thread action="join" name="#_threads.toList()#";
	session.ldev3125.after = 'goodbye';
	echo(session.ldev3125.toJson());
</cfscript>