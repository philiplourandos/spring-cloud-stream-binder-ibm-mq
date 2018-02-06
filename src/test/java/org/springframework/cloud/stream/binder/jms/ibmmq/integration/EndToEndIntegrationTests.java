package org.springframework.cloud.stream.binder.jms.ibmmq.integration;

import org.springframework.cloud.stream.binder.jms.ibmmq.IBMMQQueueProvisioner;
import org.springframework.cloud.stream.binder.jms.ibmmq.IBMMQTestUtils;
import org.springframework.cloud.stream.binder.jms.utils.Base64UrlNamingStrategy;
import org.springframework.cloud.stream.binder.jms.utils.DestinationNameResolver;

/**
 * @author Donovan Muller
 */
public class EndToEndIntegrationTests extends
		org.springframework.cloud.stream.binder.test.integration.EndToEndIntegrationTests {

	public EndToEndIntegrationTests() throws Exception {
		super(new IBMMQQueueProvisioner(IBMMQTestUtils.createConnectionFactory(),
				IBMMQTestUtils.getIBMMQProperties(), new DestinationNameResolver(new Base64UrlNamingStrategy())),
				IBMMQTestUtils.createConnectionFactory());
	}

	@Override
	protected void deprovisionDLQ() throws Exception {
		IBMMQTestUtils.deprovisionDLQ();
	}

}
