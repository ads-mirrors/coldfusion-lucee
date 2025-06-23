component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		_purge();
	}

	function afterAll() {
		_purge();
	}

	function run( testResults, textbox ) {

		describe(title="LDEV-5650 testcase for historical date conversion", body=function(){
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("Europe/Helsinki");
				_purge();
			});
			afterEach( function(){
				setTimeZone(variables.startingTZ?:"UTC");
				_purge();
			});

			xit(title = "Checking historical date conversion - Europe/Helsinki", body = function ( currentSpec ){
				setTimeZone( "Europe/Helsinki" );
				var ts = createTime( 23, 39, 49 );
				expect( datetimeformat( ts, "iso" ) ).toBe( "1899-12-30T23:39:49+02:00" ); // returns 1899-12-30T23:19:38+01:39
			});

			it(title = "Checking historical date conversion - Europe/Berlin", body = function ( currentSpec ){
				setTimeZone( "Europe/Berlin" );
				var ts = createTime( 23, 39, 49 );
				expect( datetimeformat( ts, "iso" ) ).toBe( "1899-12-30T23:39:49+01:00" );
			});

		});

		describe(title="LDEV-5650 testcase for scheduled tasks", body=function(){
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("Europe/Helsinki");
				_purge();
			});
			afterEach( function(){
				setTimeZone(variables.startingTZ?:"UTC");
				_purge();
			});

			xit(title = "Checking Scheduled task start times are consistent in Europe/Helsinki timezone- configImport", body = function ( currentSpec ){

				var task = {
					"scheduledTasks": [{
						"name": "ldev5650",
						"startDate": "{d '2025-06-18'}",
						"startTime": "{t '00:00:18'}",
						"url": "http://127.0.0.1",
						"port": 8888,
						"interval": "3600"
					}]
				};
				configImport( task, "server", request.serverAdminPassword );

				var q = _checkStartTime( createTime( 0, 0, 18 ) ); // throws [{t '23:40:07'}] but received [{t '00:40:07'}]
			});

			xit(title = "Checking Scheduled task start times are consistent in Europe/Berlin timezone- configImport", body = function ( currentSpec ){

				var task = {
					"scheduledTasks": [{
						"name": "ldev5650",
						"startDate": "{d '2025-06-18'}",
						"startTime": "{t '00:00:18'}",
						"url": "http://127.0.0.1",
						"port": 8888,
						"interval": "3600"
					}]
				};
				setTimeZone("Europe/Berlin");
				configImport( task, "server", request.serverAdminPassword );

				var q = _checkStartTime( createTime( 0, 0, 18 ) );
			});

			xit(title = "Checking Scheduled task start times are consistent in Europe/Helsinki timezone - configImport", body = function ( currentSpec ){

				var task = {
					"scheduledTasks": [{
						"name": "ldev5650",
						"startDate": "{d '2025-06-18'}",
						"startTime": "{t '23:49:59'}",
						"url": "http://127.0.0.1",
						"port": 8888,
						"interval": "3600"
					}]
				};
				configImport( task, "server", request.serverAdminPassword );

				var q = _checkStartTime( createTime( 23, 49, 59 )); // throws [{t '23:29:48'}] but received [{t '00:29:48'}]
			});

			it(title = "Checking Scheduled task start times are consistent in Europe/Helsinki timezone - cfschedule", body = function ( currentSpec ){

				cfschedule(
					action="update",
					url="http://127.0.0.1",
					task="ldev5650",
					interval="3600",
					startdate="2025-06-18",
					starttime="00:00:18"
				);

				q = _checkStartTime( createTime( 0, 0, 18 ) );

				cfschedule(
					action="update",
					url="http://127.0.0.1",
					task="ldev5650",
					interval="3600",
					startdate="2025-06-18",
					starttime="#q.ldev5650.starttime#"
				);

				q = _checkStartTime( createTime( 0, 0, 18 ) );

			});
		});
	};

	private function _purge() {
		try {
			cfschedule( action="delete", task="ldev5650");
		} catch (e){
			if ( e.message does not contain "task doesn't exist" )
				rethrow;
		}
	}

	private function _checkStartTime( startTime ){
		cfschedule(action="list", returnvariable = "local.result");
		var q = queryToStruct(result, "task");
		expect( q.ldev5650.startTime ).toBe( arguments.startTime );
		//systemOutput(result, true);
		return q;
	}

}