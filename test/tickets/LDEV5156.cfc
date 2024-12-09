component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {

    /* 
    run with

    -DtestFilter="5156" -DUseEpsilonGC="true"

    so it runs alone and without any GC for better memory info
    */

    function testStructSize() localmode=true {
        rounds = 25000;
        TYPE_LINKED_NOT_SYNC = 100; //args
        TYPE_REGULAR = 3; // local
        // systemOutput(" ", true);
        _testStructSize(fill=0, rounds=rounds);
       //  systemOutput(" ---warm up complete--- ", true);
        // systemOutput(" --- test TYPE_REGULAR / local scope --- ", true);
        _testStructSize(fill=0, rounds=rounds, structType=TYPE_REGULAR);
        _testStructSize(fill=2, rounds=rounds, structType=TYPE_REGULAR);
        _testStructSize(fill=4, rounds=rounds, structType=TYPE_REGULAR);
        _testStructSize(fill=8, rounds=rounds, structType=TYPE_REGULAR);

        // systemOutput(" --- test TYPE_LINKED_NOT_SYNC / arguments scope --- ", true);
        _testStructSize(fill=0, rounds=rounds, structType=TYPE_LINKED_NOT_SYNC);
        _testStructSize(fill=2, rounds=rounds, structType=TYPE_LINKED_NOT_SYNC);
        _testStructSize(fill=4, rounds=rounds, structType=TYPE_LINKED_NOT_SYNC);
        _testStructSize(fill=8, rounds=rounds, structType=TYPE_LINKED_NOT_SYNC);
    }

	private function _testStructSize(fill, rounds, structType) localmode=true {
        loop list="4,8,16,32" item="initialCapacity"{
            //createObject( "java", "java.lang.System" ).gc();
            //systemOutput(" ", true);
            //systemOutput("testing initialCapacity: #initialCapacity#, fill: #arguments.fill# ", true);
            s = getTickCount();
            start = reportMem( "", {}, "start" );
            a=[];
            while(true){
                st = createObject("java","lucee.runtime.type.StructImpl").init(int(arguments.structType), initialCapacity);
                for ( i=1; i < arguments.fill ; i++ )
                    st["fill#i#"]=1; // populating the struct, potentially beyond fill factor, triggering a resize
                for ( i=1; i < arguments.fill ; i++ )
                    st["fill#i#"]=2; // also test accessing the struct
                arrayAppend(a, st);
                /*
                if (arrayLen(a) mod 5000 eq 0){
                    // systemOutput(a.len() & " ");
                    // echo(a.len() & ", ");
                    flush;
                }
                */
                if (len(a) == rounds) break;
            }
            //systemOutput(" ", true);
            if (initialCapacity < 10) initialCapacity = " #initialCapacity#"; //
            // systemOutput("initialCapacity: #initialCapacity#, fill: #arguments.fill#, rounds: #numberformat(rounds)#, took #numberformat(getTickCount()-s)# ms, ");
            r = reportMem( "", start.usage, "st" );
            for (rr in r.report){
                // if (rr contains "G1" || rr contains "Epsilon")
                    // systemOutput(rr, true);
            }
            //systemOutput(r.report, true);
            
            //dump(getTickCount()-s & "ms");
            //dump(url.size);
            //dump(r.report);
            //systemOutput(" ", true);
        }
        
        // systemOutput("----", true);
        
    }

    private function reportMem ( string type, struct prev={}, string name="" ) {
        var qry = getMemoryUsage( type );
        var report = [];
        var used = { name: arguments.name };
        querySort(qry,"type,name");
        loop query=qry {
            if (qry.max == -1)
                var perc = 0;
            else 
                var perc = int( ( qry.used / qry.max ) * 100 );
            //if(qry.max<0 || qry.used<0 || perc<90) 	continue;
            //if(qry.max<0 || qry.used<0 || perc<90) 	continue;
            var rpt = replace(ucFirst(qry.type), '_', ' ')
                & " " & qry.name & ": " & numberFormat(perc) & "%, " & numberFormat( qry.used / 1024 / 1024 ) & " Mb";
            if ( structKeyExists( arguments.prev, qry.name ) ) {
                var change = numberFormat( (qry.used - arguments.prev[ qry.name ] ) / 1024 / 1024 );
                if ( change gt 0 ) {
                    rpt &= ", (+ " & change & "Mb )";
                } else if ( change lt 0 ) {
                    rpt &= ", ( " & change & "Mb )";
                }
            }
            arrayAppend( report, rpt );
            used[ qry.name ] = qry.used;
        }
        return {
            report: report,
            usage: used
        };
    }
}
