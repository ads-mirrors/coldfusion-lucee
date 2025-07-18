<cfcomponent>

<cfscript>
function onMissingMethod(target,args){
	var ReturnStruct = {arguments=arguments,target=target,args=args};
	return ReturnStruct;
}
</cfscript>

</cfcomponent>
	

