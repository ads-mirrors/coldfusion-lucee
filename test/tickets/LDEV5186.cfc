component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults , testBox ) {
		describe( title = "Testcase for LDEV-5186", body = function() {
			it( title = "check to get some classes and instances", body = function( currentSpec ) {
				var XMLUtil = createObject("java", "lucee.runtime.text.xml.XMLUtil");
				XMLUtil.getDocumentBuilderFactoryName();
				XMLUtil.createXMLReader();
				XMLUtil.getSAXParserFactoryResource();
				XMLUtil.getSAXParserFactoryName();
				XMLUtil.getTransformerFactory();
				XMLUtil.getXMLParserConfigurationName();
				XMLUtil.newDocument();
			});
		});
	}
}
