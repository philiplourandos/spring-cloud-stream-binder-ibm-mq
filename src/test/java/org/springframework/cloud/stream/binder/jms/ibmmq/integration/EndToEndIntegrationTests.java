package org.springframework.cloud.stream.binder.jms.ibmmq.integration;

import org.springframework.cloud.stream.binder.jms.ibmmq.IBMMQQueueProvisioner;
import org.springframework.cloud.stream.binder.jms.ibmmq.IBMMQTestUtils;

/**
 * @author Donovan Muller
 */
public class EndToEndIntegrationTests extends
		org.springframework.cloud.stream.binder.test.integration.EndToEndIntegrationTests {

	public EndToEndIntegrationTests() throws Exception {
		super(new IBMMQQueueProvisioner(IBMMQTestUtils.createConnectionFactory(),
				IBMMQTestUtils.getIBMMQProperties()),
				IBMMQTestUtils.createConnectionFactory());
	}

	@Override
	protected void deprovisionDLQ() throws Exception {
		IBMMQTestUtils.deprovisionDLQ();
	}

}
