<!---
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" {
	variables.name='testApplicationListener-'& getTickCount();

	function run( testResults , testBox ) {
		describe( "Test application Listener", function() {
			it( title='test ApplicationListener', body=function( currentSpec ) {
				var uri=createURI("ApplicationListener/index.cfm");
				local.res=_InternalRequest(
					template:uri,
					urls:{name:variables.name, req: "first"}
				);
				// first request everything is triggered
				assertEquals("-onApplicationStart--onSessionStart-index.cfm",res.filecontent.trim());

				local.res=_InternalRequest(
					template:uri,
					urls:{name:variables.name, req: "second"},
					addToken:true // passes the current session
				);
				// because we pass non cfif/cftoken and did not before a new session context is generated
				assertEquals("-onSessionStart-index.cfm",res.filecontent.trim());
				debug(res);
				local.res=_InternalRequest(
					template:uri,
					urls:{name:variables.name, req: "third"},
					addToken:true
				);
				debug(res);
				// again the same cfid
				assertEquals("index.cfm",res.filecontent.trim());
			});
		});

		describe( "Test application Listener on Methods", function() {

			it( title='test onRequest NotExisting', body=function( currentSpec ) {
				var uri=createURI("ApplicationListener2/notExists.cfm");
				local.res=_InternalRequest(
					template:uri
				);
				assertEquals(
					"onRequestStart:notExists.cfm;onRequest:notExists.cfm;",
					res.filecontent.trim());
			});

			it( title='test onRequest Existing', body=function( currentSpec ) {
				var uri=createURI("ApplicationListener2/test.cfm");
				local.res=_InternalRequest(
					template:uri
				);
				assertEquals(
					"onRequestStart:test.cfm;onRequest:test.cfm;",
					res.filecontent.trim());
			});

			it( title='test onCFCRequest NotExistingFile', body=function( currentSpec ) {
				var uri=createURI("ApplicationListener2/NotExisting.cfc");
				local.res=_InternalRequest(
					template:uri
					,urls:{method:'notExisting'}
				);
				assertEquals("onRequestStart:NotExisting.cfc;onCFCRequest:NotExisting,notExisting,{};",res.filecontent.trim());
			});

			it( title='test onCFCRequest NotExistingMethod', body=function( currentSpec ) {
				var uri=createURI("ApplicationListener2/Test.cfc");
				local.res=_InternalRequest(
					template:uri
					,urls:{method:'notExisting'}
				);
				assertEquals("onRequestStart:Test.cfc;onCFCRequest:Test,notExisting,{};",res.filecontent.trim());
			});

			it( title='test ON CFCRequest Existing', body=function( currentSpec ) {
				var uri=createURI("ApplicationListener2/Test.cfc");
				local.res=_InternalRequest(
					template:uri
					,urls:{method:'test'}
				);
				assertEquals("onRequestStart:Test.cfc;onCFCRequest:Test,test,{};",res.filecontent.trim());
			});
		});
	};

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

}
</cfscript>