component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title = "Testcase for createObject() function", body = function() {
			it( title = "Checking the createObject() function", body = function( currentSpec ) {
				object = createObject('java',"java.lang.StringBuffer")
				expect(isObject(object)).toBeTrue();
				expect(object.length()).toBe(0);
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with jlama", body = function( currentSpec ) {
				
				var PromptContext=createObject("java","com.github.tjake.jlama.safetensors.prompt.PromptContext",{
					"maven":[
						{
							"groupId" : "com.github.tjake",
							"artifactId" : "jlama-core",
							"version" : "0.7.0"
						},
						{
							"groupId" : "com.github.tjake",
							"artifactId" : "jlama-native",
							"classifier" : "windows-x86_64",
							"version" : "0.7.0"
						}
					]
				});
				expect(PromptContext.of("test").hasTools()).toBeFalse();
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with guava", body = function( currentSpec ) {
				var ImmutableList=createObject("java","com.google.common.collect.ImmutableList",{
					"maven":[
						{
							"groupId" : "com.google.guava",
							"artifactId" : "guava",
							"version" : "31.0.1-jre"
						}
					]
				});
				expect(ImmutableList.of("a", "b", "c").size()).toBe(3);
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with jackson-databind", body = function( currentSpec ) {
				var ObjectMapper=createObject("java","com.fasterxml.jackson.databind.ObjectMapper",{
					"maven":[
						{
							"groupId" : "com.fasterxml.jackson.core",
							"artifactId" : "jackson-databind",
							"version" : "2.12.5"
						}
					]
				});
				expect(ObjectMapper.writeValueAsString({"key":"value"})).toBe('{"key":"value"}');
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with slf4j", body = function( currentSpec ) {
				var Logger=createObject("java","org.slf4j.LoggerFactory",{
					"maven":[
						{
							"groupId" : "org.slf4j",
							"artifactId" : "slf4j-api",
							"version" : "1.7.32"
						}
					]
				});
				expect(Logger.getLogger("TestLogger").isDebugEnabled()).toBeFalse();
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with commons-lang3", body = function( currentSpec ) {
				var StringUtils=createObject("java","org.apache.commons.lang3.StringUtils",{
					"maven":[
						{
							"groupId" : "org.apache.commons",
							"artifactId" : "commons-lang3",
							"version" : "3.12.0"
						}
					]
				});
				expect(StringUtils.isEmpty("")).toBeTrue();
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with apache-httpclient", body = function( currentSpec ) {
				var HttpClients=createObject("java","org.apache.http.impl.client.HttpClients",{
					"maven":[
						{
							"groupId" : "org.apache.httpcomponents",
							"artifactId" : "httpclient",
							"version" : "4.5.13"
						}
					]
				});
				expect(HttpClients.createDefault().getClass().getSimpleName()).toEqual("InternalHttpClient");
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with logback-classic", body = function( currentSpec ) {
				var LoggerFactory=createObject("java","ch.qos.logback.classic.Logger",{
					"maven":[
						{
							"groupId" : "ch.qos.logback",
							"artifactId" : "logback-classic",
							"version" : "1.2.11"
						}
					]
				});
				expect(LoggerFactory.getName()).toBeDefined();
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with hamcrest", body = function( currentSpec ) {
				var Matchers=createObject("java","org.hamcrest.Matchers",{
					"maven":[
						{
							"groupId" : "org.hamcrest",
							"artifactId" : "hamcrest",
							"version" : "2.2"
						}
					]
				});
				expect(Matchers.is(1).matches(1)).toBeTrue();
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with rxjava", body = function( currentSpec ) {
				var Observable=createObject("java","io.reactivex.rxjava3.core.Observable",{
					"maven":[
						{
							"groupId" : "io.reactivex.rxjava3",
							"artifactId" : "rxjava",
							"version" : "3.0.13"
						}
					]
				});
				expect(Observable.just("Hello").blockingFirst()).toEqual("Hello");
			});

			it( title = "Checking the createObject(..,javasettings:{maven:...}) with maven-core", body = function( currentSpec ) {
				var MavenCli=createObject("java","org.apache.maven.cli.MavenCli",{
					"maven":[
						{
							"groupId" : "org.apache.maven",
							"artifactId" : "maven-core",
							"version" : "3.8.1"
						}
					]
				});
				expect(MavenCli.getClass().getSimpleName()).toEqual("MavenCli");
			});




		});
	}
}