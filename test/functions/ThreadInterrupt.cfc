component extends="org.lucee.cfml.test.LuceeTestCase" {
    
    function beforeAll() {
        variables.threadPrefix = "testThread_" & createUUID();
    }
    
    function run( testResults , testBox ) {
        describe("ThreadInterrupt Function", function() {
            
            itx("interrupts a sleeping thread and verifies interruption", function() {
                // Create a thread that sleeps
                var threadName = variables.threadPrefix & "_sleeping";
                var threadInterrupted = false;
                
                thread name=threadName action="run" {
                    try {
                        sleep(2000); // Sleep for 2 seconds
                        thread.output = "Sleep completed without interruption";
                    } catch (any e) {
                        if (e.type == "java.lang.InterruptedException") {
                            thread.output = "Thread was interrupted during sleep";
                            thread.interrupted = true;
                        } else {
                            thread.output = "Unexpected exception(#e.type?:""#): " & e.message;
                            thread.interrupted = false;
                        }
                    }
                }
                
                // Give thread time to start sleeping
                sleep(100);
                
                // Interrupt the thread
                threadInterrupt(threadName);
                
                // Join and verify interruption
                var result = threadJoin(threadName);
                
                expect(isDefined("result")).toBeTrue();
                expect(result).toBeStruct();
                expect(result).toHaveKey(threadName);
                expect(result[threadName].STATUS).toBe("COMPLETED");
                expect(result[threadName].OUTPUT).toBe("Thread was interrupted during sleep");
                expect(result[threadName].INTERRUPTED).toBeTrue();
            });
            
            itx("interrupts a thread in threadJoin and verifies interruption", function() {
                // Create a long-running thread 
                var longRunningThreadName = variables.threadPrefix & "_long_running";
                thread name=longRunningThreadName action="run" {
                    sleep(3000); // Run for 3 seconds
                    thread.output = "Long-running thread completed";
                }
                
                // Create a thread that joins the long-running thread
                var joiningThreadName = variables.threadPrefix & "_joining";
                thread name=joiningThreadName action="run" {
                    try {
                        var joinResult = threadJoin(longRunningThreadName);
                        thread.output = "Join completed without interruption";
                        thread.interrupted = false;
                    } catch (any e) {
                        if (e.type == "java.lang.InterruptedException") {
                            thread.output = "Thread was interrupted during join";
                            thread.interrupted = true;
                        } else {
                            thread.output = "Unexpected exception: " & e.message;
                            thread.interrupted = false;
                        }
                    }
                }
                
                // Give joining thread time to start joining
                sleep(100);
                
                // Interrupt the joining thread
                threadInterrupt(joiningThreadName);
                
                // Join and verify interruption
                var result = threadJoin(joiningThreadName);
                
                expect(isDefined("result")).toBeTrue();
                expect(result).toBeStruct();
                expect(result).toHaveKey(joiningThreadName);
                expect(result[joiningThreadName].STATUS).toBe("COMPLETED");
                expect(result[joiningThreadName].OUTPUT).toBe("Thread was interrupted during join");
                expect(result[joiningThreadName].INTERRUPTED).toBeTrue();
                
                // Clean up - wait for long-running thread to complete
                threadJoin(longRunningThreadName);
            });
            
            itx("interrupts a thread doing interrupt-aware operations", function() {
                // Create a thread that checks for interruption
                var threadName = variables.threadPrefix & "_interrupt_aware";
                
                thread name=threadName action="run" {
                    thread.interruptDetected = false;
                    
                    for (var i = 1; i <= 20; i++) {
                        // Check if thread has been interrupted
                        if (createObject("java", "java.lang.Thread").currentThread().isInterrupted()) {
                            thread.output = "Interrupt detected at iteration " & i;
                            thread.interruptDetected = true;
                            break;
                        }
                        
                        sleep(50); // Small sleep between checks
                    }
                    
                    if (!thread.interruptDetected) {
                        thread.output = "Loop completed without interruption";
                    }
                }
                
                // Give thread time to start
                sleep(100);
                
                // Interrupt the thread
                threadInterrupt(threadName);
                
                // Join and verify interruption was detected
                var result = threadJoin(threadName);
                
                expect(isDefined("result")).toBeTrue();
                expect(result).toBeStruct();
                expect(result).toHaveKey(threadName);
                expect(result[threadName].STATUS).toBe("COMPLETED");
                expect(result[threadName].INTERRUPTDETECTED).toBeTrue();
            });
            
            itx("interrupting non-existent thread doesn't cause errors", function() {
                var nonExistentThreadName = "nonExistentThread_" & createUUID();
                
                // Attempt to interrupt non-existent thread
                var exceptionOccurred = false;
                try {
                    threadInterrupt(nonExistentThreadName);
                } catch (any e) {
                    exceptionOccurred = true;
                }
                
                // Assert no exception was thrown
                expect(exceptionOccurred).toBeFalse();
            });
            
            itx("interrupts the current thread when name is not specified", function() {
                var threadName = variables.threadPrefix & "_self_interrupt";
                var interrupted = false;
                
                thread name=threadName action="run" {
                    try {
                        // Interrupt itself
                        threadInterrupt();
                        
                        // Verify the thread is interrupted
                        if (createObject("java", "java.lang.Thread").currentThread().isInterrupted()) {
                            thread.output = "Current thread successfully interrupted";
                            thread.selfInterrupted = true;
                        } else {
                            thread.output = "Current thread not interrupted";
                            thread.selfInterrupted = false;
                        }
                    } catch (any e) {
                        thread.output = "Exception during self-interruption: " & e.message;
                        thread.selfInterrupted = false;
                    }
                }
                
                // Join and verify self-interruption
                var result = threadJoin(threadName);
                
                expect(isDefined("result")).toBeTrue();
                expect(result).toBeStruct();
                expect(result).toHaveKey(threadName);
                expect(result[threadName].STATUS).toBe("COMPLETED");
                expect(result[threadName].SELFINTERRUPTED).toBeTrue();
            });
            
            itx("interrupts a thread already in terminated state", function() {
                // Create and complete a thread
                var threadName = variables.threadPrefix & "_already_terminated";
                
                thread name=threadName action="run" {
                    thread.output = "Thread ran to completion";
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
    
    function afterAll() {
        // Clean up any potentially remaining threads
        var info = threadInfo();
        for (var threadName in info) {
            if (threadName.startsWith(variables.threadPrefix) && info[threadName].STATUS == "RUNNING") {
                try {
                    threadTerminate(threadName);
                } catch (any e) {
                    // Ignore errors during cleanup
                }
            }
        }
    }
}