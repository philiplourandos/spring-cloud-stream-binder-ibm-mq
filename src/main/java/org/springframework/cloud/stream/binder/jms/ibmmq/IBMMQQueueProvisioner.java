package org.springframework.cloud.stream.binder.jms.ibmmq;

import javax.jms.ConnectionFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.jms.ibmmq.config.IBMMQConfigurationProperties;

import com.ibm.mq.MQException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.jms.Queue;
import javax.jms.Topic;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.binder.jms.provisioning.JmsConsumerDestination;
import org.springframework.cloud.stream.binder.jms.provisioning.JmsProducerDestination;
import org.springframework.cloud.stream.binder.jms.utils.DestinationNameResolver;
import org.springframework.cloud.stream.binder.jms.utils.DestinationNames;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
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

	private final DestinationNameResolver destinationNameResolver;
	
	public IBMMQQueueProvisioner(ConnectionFactory connectionFactory,
			IBMMQConfigurationProperties configurationProperties,
			DestinationNameResolver destinationNameResolver) throws MQException {

		this.ibmMQRequests = new IBMMQRequests(connectionFactory,
				configurationProperties);

		this.destinationNameResolver = destinationNameResolver;
	}

	@Override
	public ConsumerDestination provisionConsumerDestination(String name, String group, ConsumerProperties properties) throws ProvisioningException {
		String groupName = this.destinationNameResolver.resolveQueueNameForInputGroup(group, properties);
		String topicName = this.destinationNameResolver.resolveQueueNameForInputGroup(name, properties);

		provisionTopic(topicName);
		final Queue queue = provisionConsumerGroup(topicName, groupName);

		return new JmsConsumerDestination(queue);
		
	}

	@Override
	public ProducerDestination provisionProducerDestination(String name, ProducerProperties properties) throws ProvisioningException {
		Collection<DestinationNames> topicAndQueueNames =
				this.destinationNameResolver.resolveTopicAndQueueNameForRequiredGroups(name, properties);

		final Map<Integer, Topic> partitionTopics = new HashMap<>();

		for (DestinationNames destinationNames : topicAndQueueNames) {
			Topic topic = provisionTopic(destinationNames.getTopicName());
			provisionConsumerGroup(destinationNames.getTopicName(),
					destinationNames.getGroupNames());

			if (destinationNames.getPartitionIndex() != null) {
				partitionTopics.put(destinationNames.getPartitionIndex(), topic);
			}
			else {
				partitionTopics.put(-1, topic);
			}
		}
		return new JmsProducerDestination(partitionTopics);
	}
	
	private Topic provisionTopic(String topicName) {
		return ibmMQRequests.createTopic(topicName);
	}

	private Queue provisionConsumerGroup(String topicName, String... consumerGroupName) {
		Queue[] groups = null;
		if (ArrayUtils.isNotEmpty(consumerGroupName)) {
			groups = new Queue[consumerGroupName.length];
			for (int i = 0; i < consumerGroupName.length; i++) {
				groups[i] = ibmMQRequests.createQueue(consumerGroupName[i].replaceAll("-", "."));
			}
		}

		if (groups != null) {
			return groups[0];
		}
		return null;
	}
}
