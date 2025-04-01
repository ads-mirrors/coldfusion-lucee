<cfscript>
    try {
        awsCredentials = createObject('java','software.amazon.awssdk.auth.credentials.AwsBasicCredentials').create(
            "test",
            "test"
        );
        writeOutput(awsCredentials.getClass().getName());
    } catch (e) {
        writeOutput(e.stacktrace);
    } 
</cfscript>
