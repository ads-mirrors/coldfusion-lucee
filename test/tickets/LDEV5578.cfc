component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	// test suite includes the axis extension, so temp uninstall
	// this reproduces the problem but nested in the caused by
	// Caused by: java.lang.NoClassDefFoundError: org/lucee/extension/axis/Axis1Caster

	function beforeAll(){
		variables.axis = "DF28D0A4-6748-44B9-A2FDC12E4E2E4D38";
		variables.hadAxis = findExtension( axis );
		if ( variables.hadAxis ){
			admin
				action="removeRHExtension"
				type="server"
				password="#server.SERVERADMINPASSWORD#"
				id="#variables.axis#";
		}
	}

	function afterAll(){
		if ( variables.hadAxis ){
			admin
				action="updateRHExtension"
				type="server"
				password="#server.SERVERADMINPASSWORD#"
				id="#variables.axis#";
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test case LDEV-5578, createDynamicProxy depends on axis", body=function() {

			it(title="checking createDynamicProxy", body = function( currentSpec ) {
				var context = new LDEV5578.DynamicProxyLDEV5578({});
				var proxy = context.asProxy(); // throws an error as this depends on ASMUtilToType(type=, axis=true)
				//  lucee.runtime.net.rpc.RPCException:No Webservice Engine is installed! Check out the Extension Store in the Lucee Administrator for "Webservices".
			});

		});
	}

	private function findExtension( axis ){
		var q_axis = extensionList().filter( function (row ){
			return row.id == axis;
		});
		return ( q_axis.recordcount != 0 );
	}

}
