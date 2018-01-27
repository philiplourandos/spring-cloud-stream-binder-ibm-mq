package org.springframework.cloud.stream.binder.jms.ibmmq;

import javax.jms.ConnectionFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.jms.ibmmq.config.IBMMQConfigurationProperties;

import com.ibm.mq.MQException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * {@link QueueProvisioner} for IBM MQ.
 *
 * @author Donovan Muller
 */
public class IBMMQQueueProvisioner implements ProvisioningProvider {

	private static final Logger logger = LoggerFactory
			.getLogger(IBMMQQueueProvisioner.class);

	public static final String IBM_MQ_DLQ = "DLQ";

	private final IBMMQRequests ibmMQRequests;

	public IBMMQQueueProvisioner(ConnectionFactory connectionFactory,
			IBMMQConfigurationProperties configurationProperties) throws MQException {

		this.ibmMQRequests = new IBMMQRequests(connectionFactory,
				configurationProperties);
	}

	@Override
	public Destinations provisionTopicAndConsumerGroup(String topicName,
			String... consumerGroupName) {
		logger.info("Provisioning an IBM topic '{}' and consumer groups: {}", topicName,
				consumerGroupName);

		Destinations.Factory destinationsFactory = new Destinations.Factory();
		// see
		// http://www.ibm.com/support/knowledgecenter/SSFKSJ_9.0.0/com.ibm.mq.tro.doc/q048270_.htm
		String sanitisedTopicName = topicName.replaceAll("-", ".");
		destinationsFactory.withTopic(ibmMQRequests.createTopic(sanitisedTopicName));

		if (ArrayUtils.isEmpty(consumerGroupName)) {
			return destinationsFactory.build();
		}

		for (String queue : consumerGroupName) {
			String sanitisedQueueName = queue.replaceAll("-", ".");
			destinationsFactory.addGroup(ibmMQRequests.createQueue(sanitisedQueueName));
			ibmMQRequests.subcribeQueueToTopic(sanitisedTopicName, sanitisedQueueName);
		}

		return destinationsFactory.build();
	}

	@Override
	public String provisionDeadLetterQueue() {
		ibmMQRequests.createQueue(IBM_MQ_DLQ);

		return IBM_MQ_DLQ;
	}
}
