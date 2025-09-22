component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run(testResults, testBox) {
		describe("LDEV-1789: invokeImplicitAccessor / triggerDataMember malfunction", function() {
			// Inline CFC with a property and implicit accessor
			variables.obj = new component {
				property name="foo";
				public string function getFoo() { return variables.foo; }
				public void function setFoo(string val) { variables.foo = val; }
			};

			obj.foo = "bar";


			describe("direct property access works", function() {
				it("local", function() {
					expect(obj.foo).toBe("bar");
				});
				it("internalRequest", function() {
					var result = internalRequest(
						template=createURI("LDEV1789") & "/directProperty.cfm",
						urls={invokeImplicitAccessor: "true", triggerDataMember: "true"}
					);
					var parsed = deserializeJSON(result.fileContent);
					expect(parsed.foo).toBe("bar");
				});
			});

			describe("property access after passing to function works", function() {
				it("local", function() {
					function passThrough(o) { return o; }
					var obj2 = passThrough(obj);
					expect(obj2.foo).toBe("bar");
				});
				it("internalRequest", function() {
					var result = internalRequest(
						template=createURI("LDEV1789") & "/passThrough.cfm",
						urls={invokeImplicitAccessor: "true", triggerDataMember: "true"}
					);
					var parsed = deserializeJSON(result.fileContent);
					expect(parsed.foo).toBe("bar");
				});
			});

			describe("property access after passing through multiple functions works", function() {
				it("local", function() {
					function passThrough(o) { return o; }
					function passTwice(o) { return passThrough(passThrough(o)); }
					var obj3 = passTwice(obj);
					expect(obj3.foo).toBe("bar");
				});
				it("internalRequest", function() {
					var result = internalRequest(
						template=createURI("LDEV1789") & "/passTwice.cfm",
						urls={invokeImplicitAccessor: "true", triggerDataMember: "true"}
					);
					var parsed = deserializeJSON(result.fileContent);
					expect(parsed.foo).toBe("bar");
				});
			});

			describe("property access after placing in array works", function() {
				it("local", function() {
					var arr = [obj];
					expect(arr[1].foo).toBe("bar");
				});
				it("internalRequest", function() {
					var result = internalRequest(
						template=createURI("LDEV1789") & "/array.cfm",
						urls={invokeImplicitAccessor: "true", triggerDataMember: "true"}
					);
					var parsed = deserializeJSON(result.fileContent);
					expect(parsed.foo).toBe("bar");
				});
			});

			describe("property access after placing in struct works", function() {
				it("local", function() {
					var s = { myobj: obj };
					expect(s.myobj.foo).toBe("bar");
				});
				it("internalRequest", function() {
					var result = internalRequest(
						template=createURI("LDEV1789") & "/struct.cfm",
						urls={invokeImplicitAccessor: "true", triggerDataMember: "true"}
					);
					var parsed = deserializeJSON(result.fileContent);
					expect(parsed.foo).toBe("bar");
				});
			});

			describe("JSON serialize/deserialize does not throw", function() {
				it("local", function() {
					var json = serializeJSON(obj);
					var parsed = deserializeJSON(json);
					expect(parsed).notToBeNull();
				});
				it("internalRequest", function() {
					var result = internalRequest(
						template=createURI("LDEV1789") & "/json.cfm",
						urls={invokeImplicitAccessor: "true", triggerDataMember: "true"}
					);
					var parsed = deserializeJSON(result.fileContent);
					expect(parsed).notToBeNull();
				});
			});

			describe("accessing missing property throws error", function() {
				it("local", function() {
					var errorThrown = false;
					try { var x = obj.bar; } catch (any e) { errorThrown = true; }
					expect(errorThrown).toBeTrue();
				});
				xit("internalRequest", function() {
					var errorThrown = false;
					try {
						var result = internalRequest(
							template=createURI("LDEV1789") & "/missingProperty.cfm",
							urls={invokeImplicitAccessor: "true", triggerDataMember: "true"}
						);
						var parsed = deserializeJSON(result.fileContent);
					} catch (any e) {
						errorThrown = true;
					}
					expect(errorThrown).toBeTrue();
				});
			});

			describe("property with accessors=false is not accessible as struct key", function() {
				it("local", function() {
					var obj = new component {
						property name="validator";
						public function getValidator() { return "mocked"; }
					};
					var errorThrown = false;
					try {
						var x = obj.validator; // should fail
					} catch (any e) {
						errorThrown = true;
					}
					expect(errorThrown).toBeTrue();
					expect(obj.getValidator()).toBe("mocked");
				});
				it("internalRequest", function() {
					var errorThrown = false;
					try {
						var result = internalRequest(
							template=createURI("LDEV1789") & "/accessorsFalse.cfm",
							urls={invokeImplicitAccessor: "true", triggerDataMember: "true"}
						);
						var parsed = deserializeJSON(result.fileContent);
					} catch (any e) {
						errorThrown = true;
					}
					expect(errorThrown).toBeTrue();
				});
			});
			
			// Direct property access tests with different settings of invokeImplicitAccessor and triggerDataMember
			it("internalRequest with invokeImplicitAccessor=true, triggerDataMember=true", function() {
				var result = internalRequest(
					template=createURI("LDEV1789") & "/directProperty.cfm",
					urls={invokeImplicitAccessor: "true", triggerDataMember: "true"}
				);
				var parsed = deserializeJSON(result.fileContent);
				expect(parsed.foo).toBe("bar");
			});

			xit("internalRequest with invokeImplicitAccessor=false, triggerDataMember=true", function() {
				var errorThrown = false;
				try {
					var result = internalRequest(
						template=createURI("LDEV1789") & "/directProperty.cfm",
						urls={invokeImplicitAccessor: "false", triggerDataMember: "true"}
					);
					var parsed = deserializeJSON(result.fileContent);
					// Should throw, as property access should fail
				} catch (any e) {
					errorThrown = true;
				}
				expect(errorThrown).toBeTrue();
			});

			it("internalRequest with invokeImplicitAccessor=true, triggerDataMember=false", function() {
				var result = internalRequest(
					template=createURI("LDEV1789") & "/directProperty.cfm",
					urls={invokeImplicitAccessor: "true", triggerDataMember: "false"}
				);
				var parsed = deserializeJSON(result.fileContent);
				expect(parsed.foo).toBe("bar");
			});

			xit("internalRequest with invokeImplicitAccessor=false, triggerDataMember=false", function() {
				var errorThrown = false;
				try {
					var result = internalRequest(
						template=createURI("LDEV1789") & "/directProperty.cfm",
						urls={invokeImplicitAccessor: "false", triggerDataMember: "false"}
					);
					var parsed = deserializeJSON(result.fileContent);
					// Should throw, as property access should fail
				} catch (any e) {
					errorThrown = true;
				}
				expect(errorThrown).toBeTrue();
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
