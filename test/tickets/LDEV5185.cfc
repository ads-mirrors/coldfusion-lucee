component extends="org.lucee.cfml.test.LuceeTestCase" {


    function run( testResults , testBox ) {
        describe( title="Testcase for LDEV-5185", body = function() {

            it(title = "Checking entrySet", body = function( currentSpec ) {
                var map=createObject("java","java.util.HashMap");
                map.put("susi","sorglos");
                var it=map.entrySet().iterator();
                while(it.hasNext()) {
                    var e=it.next();
                    expect(e.getKey()).toBe( "susi" );
                    expect(e.getValue()).toBe( "sorglos" );
                }
            });
            it(title = "Checking keySet", body = function( currentSpec ) {
                var map=createObject("java","java.util.HashMap");
                map.put("susi","sorglos");
                var it=map.keySet().iterator();
                while(it.hasNext()) {
                    expect(it.next()).toBe( "susi" );
                }
            });
            it(title = "Checking values", body = function( currentSpec ) {
                var map=createObject("java","java.util.HashMap");
                map.put("susi","sorglos");
                var it=map.values().iterator();
                while(it.hasNext()) {
                    expect(it.next()).toBe( "sorglos" );
                }
            });
            it(title = "checking overlap issue in cache", body = function( currentSpec ) {
                getPageContext().getConfig().getConfigServerImpl().resetAll();
            });

        });
    }
}
