<cfcomponent javasettings='{maven:["software.amazon.awssdk:auth:2.31.9", "software.amazon.awssdk:identity-spi:2.31.9"]}'>
    <cfscript>
        function test(){
            try {
                awsBasicCredentials = createObject("java", "software.amazon.awssdk.auth.credentials.AwsBasicCredentials");
                return  awsBasicCredentials.create("test", "test").getClass().getName();
            } catch (e) {
                return e.message;
            }
        }
    </cfscript>
</cfcomponent>
    
    
    