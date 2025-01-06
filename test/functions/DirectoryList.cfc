<!---
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
 --->
 component extends="org.lucee.cfml.test.LuceeTestCase" {

	public function beforeAll() {
		
		variables.SEP = Server.separator.file;
		variables.path = getTempDirectory() & "directoryList";
		if (directoryExists(path))
			directoryDelete(path,true);
		var path2 = path&"#SEP#a";
		directoryCreate(path2);
		cffile( fixnewline=false, output="aaa", file="#path##SEP#b.txt", addnewline=true, action="write" );
		cffile( fixnewline=false, output="aaa", file="#path2##SEP#c.txt", addnewline=true, action="write" );
	}

	public function afterAll() {
		directoryDelete(path,true);
	}

	function run( testResults , testBox ) {
		describe( "test cases for DirectoryList()", function() {
			it(title = "recursive false", body = function( currentSpec ) {
				var dir = directoryList(path);
				assertEquals(2,arrayLen(dir));
				assertEquals("#path##SEP#a,#path##SEP#b.txt",listSort(arrayToList(dir),'textnocase'));
			});

			it(title = "recursive true", body = function( currentSpec ) {
				var dir = directoryList(path,true);
				assertEquals(3,arrayLen(dir));
				assertEquals("#path##SEP#a,#path##SEP#a#SEP#c.txt,#path##SEP#b.txt",listSort(arrayToList(dir),'textnocase'));
			});

			it(title = "type:directory", body = function( currentSpec ) {
				var dir = directoryList(path:path,type:'directory');
				assertEquals(1,arrayLen(dir));
				assertEquals("#path##SEP#a",arrayToList(dir));
			});

			it(title = "type:file", body = function( currentSpec ) {
				var dir = directoryList(path:path,type:'file');
				assertEquals(1,arrayLen(dir));
				assertEquals("#path##SEP#b.txt",arrayToList(dir));
			});

			it(title = "listinfo - name", body = function( currentSpec ) {
				var dir = directoryList(path,true,"name");
				assertEquals(3,arrayLen(dir));
				assertEquals("a,b.txt,c.txt",listSort(arrayToList(dir),'textnocase'));
			});

			it(title = "listinfo - path", body = function( currentSpec ) {
				var dir = directoryList(path,true,"path");
				assertEquals(3,arrayLen(dir));
				assertEquals("#path##SEP#a,#path##SEP#a#SEP#c.txt,#path##SEP#b.txt",listSort(arrayToList(dir),'textnocase'));
			});

			it(title = "listinfo - query", body = function( currentSpec ) {
				var dir = directoryList(path,true,"query");
				expect(dir.recordcount).toBe(3);
				var files = QueryToStruct(dir,"name");
				expect(files).toHaveKey("a");
				expect(files).toHaveKey("c.txt");
				expect(files).toHaveKey("b.txt");
				expect(files.a.type).toBe("dir");
				expect(files["b.txt"].type).toBe("file");
			});

		});
	}

}