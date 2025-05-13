component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql" {
	function beforeAll(){
		if(isNotSupported()) return;
		var mySQL = getCredentials();
		mySQL.storage = true;
		variables.str = mySQL;
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for cfinsert",skip=isNotSupported(), body=function() {
			it(title = "checking cfinsert tag", body = function( currentSpec ) {
				tableCreation();
				form.id = 1;
				form.personName = "testCase";
				cfinsert (tableName = "cfInsertTBL" formFields="id,personName" datasource=str);
				query datasource=str name="local.testQry"{
					echo("SELECT * FROM `cfInsertTBL`");
				}
				expect(testQry.personName).toBe('testcase');
			});

			it(title = "checking cfinsert tag with source", body = function( currentSpec ) {
				tableCreation();
				var sct = {};
				sct.id = 1;
				sct.personName = "testSource";
				cfinsert (tableName = "cfInsertTBL" columns="id,personName" datasource=str source=#sct#);
				query datasource=str name="local.testQry"{
					echo("SELECT * FROM `cfInsertTBL`");
				}
				expect(testQry.personName).toBe(sct.personName);
			});
		});
	}

	private function tableCreation(){
		query datasource=str{
			echo("DROP TABLE IF EXISTS `cfInsertTBL`");
		}
		query datasource=str{
			echo( "
				create table `cfInsertTBL`(id varchar(10),Personname varchar(10))"
				);
		}
	}

	private boolean function isNotSupported() {
		var cred=getCredentials();
		return isNull(cred) || structCount(cred)==0;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

	Function afterAll(){
		if(isNotSupported()) return;
		query datasource=str{
			echo("DROP TABLE IF EXISTS `cfInsertTBL`");
		}
		structDelete(form, "id");
		structDelete(form, "personName");
	}
}