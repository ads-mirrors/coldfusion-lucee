component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArrayFindNocase()", body=function() {
			it(title="checking ArrayFindNocase() function", body = function( currentSpec ) {
				assertEquals(2, ArrayFindNoCase(listToArray('abba,bb'),'bb'));
				assertEquals(2, ArrayFindNoCase(listToArray('abba,bb,AABBCC,BB'),'BB'));
				assertEquals(0, ArrayFindNoCase(listToArray('abba,bb,AABBCC'),'ZZ'));
				local.tony.this="that";
				tony.foo="bar";
				local.bill.test="testing";
				bill.key="value ";

				local.bill2.test="testing";
				bill2.key="value ";

				local.bill3=createObject('java','java.util.HashMap');
				bill3.put('test',"testing");
				bill3.put('key',"value ");

				local.bill4.test="Testing";
				bill4.key="Value ";

				local.testarray = [ tony,bill ];
				assertEquals(2, arrayFindNoCase( testArray,bill ));
				assertEquals(2, arrayFindNoCase( testArray,bill2 ));
				assertEquals(0, arrayFindNoCase( testArray,bill3 ));
				assertEquals(2, arrayFindNoCase( testArray,bill4 ));

				local.arr1=["abba1","hanna1"];
				local.arr2=["abba2","hanna2"];
				local.arr3=["abba2","hanna2"];

				local.testarray = [ arr1,arr2 ];

				local.arr4=createObject('java','java.util.ArrayList');
				arr4.add('abba2');
				arr4.add('hanna2');

				assertEquals(2, arrayFindNoCase( testArray,arr2 ));
				assertEquals(2, arrayFindNoCase( testArray,arr3 ));
				assertEquals(0, arrayFindNoCase( testArray,arr4 ));

				local.qry1=queryNew('a,b,c');
				QueryAddRow(qry1);
				QuerySetCell(qry1,'a','a1');
				QuerySetCell(qry1,'b','b1');
				QuerySetCell(qry1,'c','c1');
				local.qry2=queryNew('a,b,c');
				QueryAddRow(qry2);
				QuerySetCell(qry2,'a','a1');
				QuerySetCell(qry2,'b','b1');
				QuerySetCell(qry2,'c','c1');
				local.qry3=queryNew('a,b,c');
				QueryAddRow(qry3);
				QuerySetCell(qry3,'a','a1');
				QuerySetCell(qry3,'b','b1');
				QuerySetCell(qry3,'c','c2');

				local.testarray = [ qry1 ];

				assertEquals(1, arrayFindNoCase( testArray,qry1 ));
				assertEquals(1, arrayFindNoCase( testArray,qry2 ));
				assertEquals(0, arrayFindNoCase( testArray,qry3 ));
			});
		});
	}
}