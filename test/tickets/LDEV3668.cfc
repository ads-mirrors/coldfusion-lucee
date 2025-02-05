component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-3668", function() {
			variables.srcArr = ["direct"];

			it( title="default passby=reference for tag argument", body=function( currentSpec ){
				var arr = duplicate(srcArr);
				expect(serializeJSON(byTagReference(arr))).toBe('["direct","by tag reference"]');
				expect(serializeJSON(arr)).toBe('["direct","by tag reference"]');
			});

			it( title="passby=value for tag argument", body=function( currentSpec ){
				var arr = duplicate(srcArr);
				expect(serializeJSON(byTagValue(arr))).toBe('["direct","by tag value"]');
				expect(serializeJSON(arr)).toBe('["direct"]'); // unchanged
			});

			it( title="default passby for script argument", body=function( currentSpec ){
				var arr = duplicate(srcArr);
				expect(serializeJSON(byRef(arr))).toBe('["direct","by reference"]');
				expect(serializeJSON(arr)).toBe('["direct","by reference"]');
			});

			xit( title="passby=value for script argument", body=function( currentSpec ){
				var arr = duplicate(srcArr);
				expect(serializeJSON(byValue(arr))).toBe('["direct","by value"]');
				expect(serializeJSON(arr)).toBe('["direct"]');  //unchanged
			});

		});
	}

	```
		<cffunction name="byTagValue">
			<cfargument name="arr" passby="value">
			<cfset arguments.arr.append("by tag value")>
			<cfreturn arguments.arr>
		</cffunction>

		<cffunction name="byTagReference">
			<cfargument name="arr"> <!-- passby="reference" is default --->
			<cfset arguments.arr.append("by tag reference")>
			<cfreturn arguments.arr>
		</cffunction>

	```

	function byRef (required array arr ){
		arguments.arr.append("by reference");
		return arguments.arr;
	}

	// argument with passBy=value
	function byValue (required array arr passby="value"){
		arguments.arr.append("by value");
		return arguments.arr;
	}
}