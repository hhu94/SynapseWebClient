package org.sagebionetworks.web.unitserver.logging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.logging.s3.LogDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test that logging is setup as expected.  This includes writing a UUID to the log and
 * validating that the UUID can be found in the logs in S3.
 *  
 * @author John
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:portal-application-context.spb.xml" })
public class PortalLoggingIntegrationTest {

	private static Logger log = LogManager.getLogger(PortalLoggingIntegrationTest.class);
	
	/**
	 * This test has a long wait time as log rolling only occurs once a minute.
	 */
	public static final long MAX_WAIT_MS = 1000*60*60;
	
	
	@Autowired
	LogDAO logDAO;
	
	@Before
	public void before() throws InterruptedException{
		// This is a slow test so we do not make developers run it.
		// However, if it fails on hudson, then set this true to run the test
		// in a developer stack.
		Assume.assumeTrue(!StackConfiguration.isDevelopStack());
		// clear all s3 log data before we start		
		logDAO.deleteAllStackInstanceLogs();
		log.info("Sleeping after delete for 30 seconds");
		Thread.sleep(30000);
	}
	
	@Ignore
	@Test
	public void testRoundTrip() throws InterruptedException, IOException{
		// This test writes data to the log and then attempts to find that data S3 after the logs have been swept.
		String uuid = UUID.randomUUID().toString();
		log.trace("This is a trace message");
		log.debug("This is a debug message");
		log.info("This is an info message containing a UUID: "+uuid);
		log.error("An Error!!!", new RuntimeException(new IllegalArgumentException("Bad mojo!")));
		// Wait for the UUID to appear in S3.
		long start = System.currentTimeMillis();
		String key = null;
		do{
			key = logDAO.findLogContainingUUID(uuid);
			if(key == null){
				log.info("Waiting for worker logs to get swept to S3...");
				Thread.sleep(1000);
			}
			long elapse = System.currentTimeMillis()-start;
			assertTrue("Timed out waiting for worker logs to be swept to S3",elapse < MAX_WAIT_MS);
		}while(key == null);
		assertNotNull(key);
		log.info("Found the log data in :"+key);
	}
		
}
