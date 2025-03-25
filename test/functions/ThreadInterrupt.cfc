component extends="org.lucee.cfml.test.LuceeTestCase" {
    
    function beforeAll() {
        variables.threadPrefix = "testThread_" & createUUID();
    }
    
    function run( testResults , testBox ) {
        describe("ThreadInterrupt Function", function() {
            
            it("interrupts a sleeping thread and verifies interruption", function() {
                // Create a thread that sleeps
                var threadName = variables.threadPrefix & "_sleeping";
                var threadInterrupted = false;
                
                thread name=threadName action="run" {
                    try {
                        sleep(1000); // Sleep for 2 seconds
                        thread.result = "Sleep completed without interruption";
                    } 
                    catch (any e) {
                        if (e.type == "java.lang.InterruptedException") {
                            thread.result = "Thread was interrupted during sleep";
                            thread.threadInterrupted = true;
                        } else {
                            thread.result = "Unexpected exception(#e.type?:""#): " & e.message;
                            thread.threadInterrupted = false;
                        }
                    }
                }
                
                // Give thread time to start sleeping
                sleep(100);
                
                // Interrupt the thread
                threadInterrupt(threadName);
                
                // Join and verify interruption
                threadJoin(threadName);
                
                var result = cfthread[threadName];
                expect(result).toBeStruct();
                expect(result.STATUS).toBe("COMPLETED");
                expect(result.result).toBe("Thread was interrupted during sleep");
                expect(result.threadInterrupted).toBeTrue();
            });

            it("interrupts a sleeping thread and verifies interruption using the alias function interruptThread()", function() {
                // Create a thread that sleeps
                var threadName = variables.threadPrefix & "_sleeping";
                var threadInterrupted = false;
                
                thread name=threadName action="run" {
                    try {
                        sleep(1000); // Sleep for 2 seconds
                        thread.result = "Sleep completed without interruption";
                    } 
                    catch (any e) {
                        if (e.type == "java.lang.InterruptedException") {
                            thread.result = "Thread was interrupted during sleep";
                            thread.threadInterrupted = true;
                        } else {
                            thread.result = "Unexpected exception(#e.type?:""#): " & e.message;
                            thread.threadInterrupted = false;
                        }
                    }
                }
                
                // Give thread time to start sleeping
                sleep(100);
                
                // Interrupt the thread
                interruptThread(threadName);
                
                // Join and verify interruption
                threadJoin(threadName);
                
                var result = cfthread[threadName];
                expect(result).toBeStruct();
                expect(result.STATUS).toBe("COMPLETED");
                expect(result.result).toBe("Thread was interrupted during sleep");
                expect(result.threadInterrupted).toBeTrue();
            });
            
            it("interrupts a thread doing interrupt-aware operations", function() {
                // Create a thread that checks for interruption
                var threadName = variables.threadPrefix & "_interrupt_aware";
                
                thread name=threadName action="run" {
                    thread.failDetected = false;
                    try {
                        sleep(1000);
                    }
                    // java.lang.InterruptedException (i do not get that specific, this may chnage in future versions of java)
                    catch(e) {
                        thread.failDetected = true;
                    }
                }
                
                // Give thread time to start
                sleep(100);
                
                // Interrupt the thread
                threadInterrupt(threadName);
                
                // Join and verify interruption was detected
                threadJoin(threadName);
                var result=cfthread[threadName];
                
                expect(result.STATUS).toBe("COMPLETED");
                expect(result.failDetected).toBeTrue();
            });
            
            it("interrupting non-existent thread cause errors", function() {
                var nonExistentThreadName = "nonExistentThread_" & createUUID();
                
                // Attempt to interrupt non-existent thread
                var exceptionOccurred = false;
                try {
                    threadInterrupt(nonExistentThreadName);
                } catch (any e) {
                    exceptionOccurred = true;
                }
                
                // Assert no exception was thrown
                expect(exceptionOccurred).toBeTrue();
            });
            
            it("interrupts the current thread when name is not specified", function() {
                var threadName = variables.threadPrefix & "_self_interrupt";
                var interrupted = false;
                
                thread name=threadName action="run" {
                    try {
                        // Interrupt itself
                        threadInterrupt();
                        thread.selfInterrupted = true;
                    }
                    catch (any e) {
                        thread.result = "Exception during self-interruption: " & e.message;
                        thread.selfInterrupted = false;
                    }
                }
                
                // Join and verify self-interruption
                threadJoin(threadName);
                var result=cfthread[threadName];
                expect(result.STATUS).toBe("COMPLETED");
                expect(result.SELFINTERRUPTED).toBeTrue();
            });
            
            it("interrupts a thread already in terminated state", function() {
                // Create and complete a thread
                var threadName = variables.threadPrefix & "_already_terminated";
                
                thread name=threadName action="run" {
                    thread.result = "Thread ran to completion";
                }
                
                // Wait for thread to complete
                threadJoin(threadName);
                
                // Try to interrupt the completed thread
                var exceptionOccurred = false;
                try {
                    threadInterrupt(threadName);
                } catch (any e) {
                    exceptionOccurred = true;
                }
                
                // Assert no exception was thrown
                expect(exceptionOccurred).toBeFalse();
            });
        });
    }
}