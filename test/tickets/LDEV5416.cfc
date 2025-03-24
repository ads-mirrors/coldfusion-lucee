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
					
					var result = queryResultScopeTest();
					expect(structKeyExists(local, "cfquery")).toBeTrue("The unnamed query result should exist in the local scope");
					expect(structKeyExists(variables, "cfquery")).toBeFalse("The unnamed query result should NOT exist in the variables scope");
				});
			});
		}
	}