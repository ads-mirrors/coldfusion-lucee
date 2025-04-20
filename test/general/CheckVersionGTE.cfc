component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Test suite for checkVersionGTE", function() {
			it( title='test build lucee version checking service', body=function( currentSpec ) {
				var versions = {
					"6.2.1.25": {
						"5": false,
						"5.4": false,
						"5.4.7": false,
						"5.4.7.3": false,
						"6": true,
						"7": false,
						"6.1": false,
						"6.2.1.24": false,
						"6.2.1.25": true,
						"6.2.1.26": true,
						"6.2.0": false,
						"6.2.1": true,
						"6.2.2": false,
						"6.2": true,
						"6.3": false,
						"7.0": false
					},
					"5.4.7.3": {
						"5": true,
						"5.4": true,
						"5.4.6": false,
						"5.4.7.2": false,
						"5.4.7.3": true,
						"5.4.7.4": true,
						"5.4.7": true,
						"6": false,
						"6.1": false,
						"6.2.1.25": false,
						"6.3": false,
						"7.0": false,
						"7": false
					}
				};
				structEach( versions, function( vk, vv ){
					structEach( vv, function( tk, tv ){
						var args = ListToArray (tk, "." );
						arrayPrepend( args, vk) ;
						var result = server.checkVersionGTE( argumentCollection=args );
						expect( result ).toBe( tv, args.toJson() );
					});
				});

				expect(server.checkVersionGTE( "5.4.7.3", 6, 2, 1, 25 )).toBeFalse();
			});
		});
	}
}