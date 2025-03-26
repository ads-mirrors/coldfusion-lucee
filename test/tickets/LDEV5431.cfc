component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run() {
		describe(title="LDEV-5431 cfcookie can parse expires timestamp", body=function() {
			it("should successfully parse the date", function() {
				var myCookie = {
					"name" : "foo",
					"preservecase" : true,
					"domain" : CGI.HTTP_HOST,
					"path" : "/",
					"expires" : "Mon, 31 Dec 2028 23:59:59 GMT",
					"value" : "qux"
				};
				cfcookie( attributeCollection=myCookie );
			});

		});
	}

}
