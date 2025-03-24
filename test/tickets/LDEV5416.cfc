component extends="org.lucee.cfml.test.LuceeTestCase" {

		function run() {
			describe("Lucee 7 Unnamed Query Result Scope Tests", function() {
				
				it("should store unnamed query results in local scope not variables scope within a function", function() {
					
					// Create initial query
					var employeeQuery = queryNew(
						"empID,firstName,lastName,department,salary",
						"integer,varchar,varchar,varchar,decimal",
						[
							{empID: 1, firstName: "John", lastName: "Doe", department: "IT", salary: 75000.00},
							{empID: 2, firstName: "Jane", lastName: "Smith", department: "HR", salary: 65000.00},
							{empID: 3, firstName: "Mike", lastName: "Johnson", department: "IT", salary: 82000.00},
							{empID: 4, firstName: "Sara", lastName: "Williams", department: "Marketing", salary: 68000.00}
						]
					);
					
					// Execute unnamed query
					query dbtype="query" {
						```
						SELECT empID, firstName, lastName, department, salary
						FROM employeeQuery
						WHERE department = 'IT'
						ORDER BY salary DESC
						```
					}
					expect(structKeyExists(local, "cfquery")).toBeTrue("The unnamed query result should exist in the local scope");
					expect(structKeyExists(variables, "cfquery")).toBeFalse("The unnamed query result should NOT exist in the variables scope");
				});


				it("should store unnamed lock results in local scope not variables scope within a function", function() {
				
					// Create a test variable to modify inside the lock
					var testVar = 0;
					
					// Execute unnamed lock
					lock name="testLock" timeout="10" type="exclusive" {
						// Do some operation inside the lock
						testVar = 1;
					}
					
					// Check if the lock result exists in the expected scopes
					expect(structKeyExists(local, "cflock")).toBeTrue("The unnamed lock result should exist in the local scope");
					expect(structKeyExists(variables, "cflock")).toBeFalse("The unnamed lock result should NOT exist in the variables scope");
				});
			});
		}
	}