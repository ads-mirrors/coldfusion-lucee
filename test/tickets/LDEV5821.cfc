component extends="org.lucee.cfml.test.LuceeTestCase" {

function run(testResults, testBox) {
	describe("LDEV-5821 property metadata regressions", function() {
		var cfc = new component output="false" {
			property name="foo" type="string" required="true";
			property name="bar" type="string" required="false";
			property name="baz" type="string";
			property name="test_property";
			property name="related_prop" control="objectpicker" maxLength="35" relatedto="someObject" relationship="many-to-one";
			property name="another_property" label="My property" type="date" dbtype="datetime" control="datepicker" required="true";
			property name="some_numeric_property" label="Numeric prop" type="numeric" dbtype="tinyint" control="spinner" required="false" minValue="1" maxValue="10";
		};
		var meta = getMetaData(cfc);
		var props = meta.properties;
		/*
		debug(props);
		systemOutput("", true);
		systemOutput("Properties metadata:", true);
		systemOutput(serializeJson(props), true);
		systemOutput("", true);
		*/

		it("should output required as 'yes' for foo", function() {
			var fooIdx = props.find(function(p){return p.name=="foo";});
			expect(fooIdx).toBeGT(0, "Property 'foo' not found");
			var foo = props[fooIdx];
			expect(structKeyExists(foo, "required")).toBeTrue();
			expect(foo.required).toBeWithCase("yes");
		});
		it("should output required as 'no' for bar", function() {
			var barIdx = props.find(function(p){return p.name=="bar";});
			expect(barIdx).toBeGT(0, "Property 'bar' not found");
			var bar = props[barIdx];
			expect(structKeyExists(bar, "required")).toBeTrue();
			expect(bar.required).toBeWithCase("no");
		});
		it("should output required as 'no' for baz", function() {
			var bazIdx = props.find(function(p){return p.name=="baz";});
			expect(bazIdx).toBeGT(0, "Property 'baz' not found");
			var baz = props[bazIdx];
			if (structKeyExists(baz, "required")) {
				expect(baz.required).toBeWithCase("no");
			} else {
				expect(structKeyExists(baz, "required")).toBeFalse();
			}
		});
		it("should output required as 'no' for test_property", function() {
			var testPropIdx = props.find(function(p){return p.name=="test_property";});
			expect(testPropIdx).toBeGT(0, "Property 'test_property' not found");
			var testProp = props[testPropIdx];
			if (structKeyExists(testProp, "required")) {
				expect(testProp.required).toBeWithCase("no");
			} else {
				expect(structKeyExists(testProp, "required")).toBeFalse();
			}
		});
		it("should output required as 'yes' for another_property", function() {
			var anotherIdx = props.find(function(p){return p.name=="another_property";});
			expect(anotherIdx).toBeGT(0, "Property 'another_property' not found");
			var another = props[anotherIdx];
			expect(structKeyExists(another, "required")).toBeTrue();
			expect(another.required).toBeWithCase("yes");
		});
		it("should output required as 'no' for some_numeric_property", function() {
			var numericIdx = props.find(function(p){return p.name=="some_numeric_property";});
			expect(numericIdx).toBeGT(0, "Property 'some_numeric_property' not found");
			var numeric = props[numericIdx];
			expect(structKeyExists(numeric, "required")).toBeTrue();
			expect(numeric.required).toBeWithCase("no");
		});

		it("should output all dynamic attributes for related_prop", function() {
			var relatedIdx = props.find(function(p){return p.name=="related_prop";});
			expect(relatedIdx).toBeGT(0, "Property 'related_prop' not found");
			var related = props[relatedIdx];
			expect(structKeyArray(related)).toIncludeWithCase("maxlength");
			expect(related.maxlength).toBeWithCase("35");
			expect(related.control).toBeWithCase("objectpicker");
			expect(related.relatedto).toBeWithCase("someObject");
			expect(related.relationship).toBeWithCase("many-to-one");
		});

		it("should output all dynamic attributes for another_property", function() {
			var anotherIdx = props.find(function(p){return p.name=="another_property";});
			expect(anotherIdx).toBeGT(0, "Property 'another_property' not found");
			var another = props[anotherIdx];
			expect(another.label).toBeWithCase("My property");
			expect(another.type).toBeWithCase("date");
			expect(another.dbtype).toBeWithCase("datetime");
			expect(another.control).toBeWithCase("datepicker");
		});

		it("should output all dynamic attributes for some_numeric_property", function() {
			var numericIdx = props.find(function(p){return p.name=="some_numeric_property";});
			expect(numericIdx).toBeGT(0, "Property 'some_numeric_property' not found");
			var numeric = props[numericIdx];
			expect(structKeyArray(numeric)).toIncludeWithCase("minvalue");
			expect(structKeyArray(numeric)).toIncludeWithCase("maxvalue");
			expect(numeric.label).toBeWithCase("Numeric prop");
			expect(numeric.type).toBeWithCase("numeric");
			expect(numeric.dbtype).toBeWithCase("tinyint");
			expect(numeric.control).toBeWithCase("spinner");
			expect(numeric.minvalue).toBeWithCase("1");
			expect(numeric.maxvalue).toBeWithCase("10");
		});
	});
	describe("LDEV-5821 property metadata with inheritance", function() {
		var cfc = new LDEV5821.LDEV5821Child();
		var meta = getMetaData(cfc);
		var props = meta.properties;
		/*
		debug(props);
		systemOutput("", true);
		systemOutput("Inherited Properties metadata:", true);
		systemOutput(serializeJson(props), true);
		systemOutput("", true);
		*/

		it("should include baseProp from base component (if supported)", function() {
			var idx = props.find(function(p){return p.name=="baseProp";});
			if (idx <= 0) {
				systemOutput("Property 'baseProp' not found in metadata (expected for Lucee 6.2)", true);
				return;
			}
			var prop = props[idx];
			expect(structKeyExists(prop, "required")).toBeTrue();
			expect(prop.required).toBeWithCase("yes");
			expect(prop.type).toBeWithCase("string");
		});
		it("should include childProp from child component", function() {
		var idx = props.find(function(p){return p.name=="childProp";});
		expect(idx).toBeGT(0, "Property 'childProp' not found");
		var prop = props[idx];
			expect(structKeyExists(prop, "required")).toBeTrue();
			expect(prop.required).toBeWithCase("no");
			expect(prop.type).toBeWithCase("numeric");
		});
		it("should override overrideProp in child component", function() {
		var idx = props.find(function(p){return p.name=="overrideProp";});
		expect(idx).toBeGT(0, "Property 'overrideProp' not found");
		var prop = props[idx];
			expect(structKeyExists(prop, "required")).toBeTrue();
			expect(prop.required).toBeWithCase("yes"); // overridden to required="true"
			expect(prop.type).toBeWithCase("string");
		});
	});
	describe("LDEV-5821 singularName accessor generation regression", function() {
		var cfc = new component persistent="true" accessors="true" {
			property name="fruits" singularName="fruit" fieldtype="one-to-many" cfc="FruitEntity" fkcolumn="basketId" cascade="all" lazy="false" fetch="join";
		};

		it("should generate addFruit method from singularName attribute", function() {
			expect(structKeyExists(cfc, "addFruit")).toBeTrue("addFruit method should be generated from singularName='fruit'");
		});

		it("should generate hasFruit method from singularName attribute", function() {
			expect(structKeyExists(cfc, "hasFruit")).toBeTrue("hasFruit method should be generated from singularName='fruit'");
		});

		it("should generate removeFruit method from singularName attribute", function() {
			expect(structKeyExists(cfc, "removeFruit")).toBeTrue("removeFruit method should be generated from singularName='fruit'");
		});

		it("should include singularName in property metadata", function() {
			var meta = getMetaData(cfc);
			var props = meta.properties;
			var fruitsIdx = props.find(function(p){return p.name=="fruits";});
			expect(fruitsIdx).toBeGT(0, "Property 'fruits' not found");
			var fruits = props[fruitsIdx];
			expect(structKeyExists(fruits, "singularname")).toBeTrue("singularName should be in property metadata");
			expect(fruits.singularname).toBeWithCase("fruit");
		});
	});
}

}
