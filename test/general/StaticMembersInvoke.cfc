component extends="org.lucee.cfml.test.LuceeTestCase"{
	import java.util.*;
	import lucee.runtime.type.StructImpl;

	function run( testResults , testBox ) {
		describe( "test invokation of static members", function() {
			it(title="component: full name", body=function() {
				org.lucee.cfml.Query::new(columnNames:["lastname","firstname"]);
			});
			it(title="component: full name with cfml prefix", body=function() {
				cfml:org.lucee.cfml.Query::new(columnNames:["lastname","firstname"]);
			});

			it(title="component: implicit org.lucee.cfml", body=function() {
				Query::new(columnNames:["lastname","firstname"]);
			});

			it(title="component: implicit org.lucee.cfml with cfml prefix", body=function() {
				cfml:Query::new(columnNames:["lastname","firstname"]);
			});
			
			it(title="class: full name", body=function() {
				expect(java.lang.Double::valueOf("123")).toBe(123);
			});

			it(title="class: full name with java prefix", body=function() {
				expect(java:java.lang.Double::valueOf("123")).toBe(123);
			});

			it(title="class: implicit java.lang", body=function() {
				expect(Double::valueOf("123")).toBe(123);
			});

			it(title="class: implicit java.lang with java prefix", body=function() {
				expect(java:Double::valueOf("123")).toBe(123);
			});

			it(title="class: implicit with asterix import", body=function() {
				var list=java.util.Collections::emptyList();
				expect(list.size()).toBe(0);
			});

			it(title="class: implicit with explicit import", body=function() {
				var map=new StructImpl();
				expect(map.size()).toBe(0);
			});

			it(title="class: set type", body=function() {
				expect(isNumeric(StructImpl::DEFAULT_INITIAL_CAPACITY)).toBeTrue();
			});

			it(title="test possible conflict", body=function() {
				expect(testFunc(java:1)).toBe("java");
				expect(testFunc(java : 1)).toBe("java");
				expect(testFunc(class:1)).toBe("class");
				expect(testFunc(cfml:1)).toBe("cfml");
				expect(testFunc(cfc:1)).toBe("cfc");
				expect(testFunc(whatever:1)).toBe("whatever");
			});
			
		});
	}

	private function testFunc() {
        return arguments.keyList();
	}
}