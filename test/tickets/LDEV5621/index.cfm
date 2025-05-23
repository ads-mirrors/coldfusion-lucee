<cfscript>
	tika=new org.apache.tika.Tika();
	echo(serializeJson(mvnInfo(tika)));


	function mvnInfo(obj) {
		var sct = dumpStruct(obj);
		var qry=sct.data;
		qry=querySlice(qry,6);
		qry=qry.data1.data;
		var data=[:];
		loop query=qry {
			var arr=listToArray(qry.data1,":");
			data[trim(arr[1])]=trim(arr[2]);
		}
		return data;
	}
</cfscript>