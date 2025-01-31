component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-2135 using memory", function() {
			it( title='thread looses session variables - sessionCluster=false', body=function( currentSpec ) {
				test( {sessionCluster: false, sessionStorage: "memory"} );
			});

			it( title='thread looses session variables - sessionCluster=true', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "memory"} );
			});
		});

		describe( title="Test suite for LDEV-2135 using redis", skip=skipRedis(), body=function() {
			it( title='thread looses session variables - redis- sessionCluster=false', body=function( currentSpec ) {
				test( {sessionCluster: false, sessionStorage: "redis"} );
			});

			it( title='thread looses session variables - redis - sessionCluster=true', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "redis"} );
			});
		});

		describe( title="Test suite for LDEV-2135 using memcached", skip=skipMemcached(), body=function() {
			it( title='thread looses session variables - memcached -sessionCluster=false', body=function( currentSpec ) {
				test( {sessionCluster: false, sessionStorage: "memcached"} );
			});

			it( title='thread looses session variables - memcached -sessionCluster=true', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "memcached"} );
			});
		});
	}

	private function test( args ){
		var uri = createURI( "LDEV2135" );
		local.result = _InternalRequest(
			template : "#uri#/cfml-session/testThreadSession.cfm",
			url: args
		);
		checkSess( result.fileContent );
	}

	private function checkSess( fileContent ){
		expect( fileContent).toBeJson();
		systemOutput( fileContent, true );
		var s = deserializeJSON( fileContent );
		expect( s ).toHaveKey( "before" );
		expect( s ).toHaveKey( "after" );
		expect( s ).toHaveKey( "threads" );
		expect( s.threads ).toHaveLength( 5 );
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function skipRedis(){
		return (structCount(server.getTestService( "redis" )) eq 0);
	}

	private function skipMemcached(){
		return (structCount(server.getTestService( "memcached" )) eq 0);
	}
}