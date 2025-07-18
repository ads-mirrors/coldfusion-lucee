<cfscript>
    try {
        throw "a";
    }
    finally {
        try {
            try {
                throw "LDEV-4451-nestedfinally";
            }
            finally {}
        }
        finally {}
    }
</cfscript>