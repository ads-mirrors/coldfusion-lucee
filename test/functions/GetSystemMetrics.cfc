component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function test(){
		var metrics = GetSystemMetrics();
		var stats = "activeRequests,activeThreads,queueRequests,activeDatasourceConnections,idleDatasourceConnections,"
			& "datasourceConnections,tasksOpen,tasksClosed,sessionCount,clientCount,clientCount,applicationContextCount";
		loop list="#stats#" item="local.key"{
			expect( metrics ).toHaveKey( key );
		}
	}

}