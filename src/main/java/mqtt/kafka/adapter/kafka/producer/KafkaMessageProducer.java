package mqtt.kafka.adapter.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import mqtt.kafka.adapter.model.Message;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class KafkaMessageProducer {

    @Autowired
    private KafkaTemplate<String, Message> kafkaTemplate;

    @Autowired
    private Admin kafkaAdmin;

    public void sendMessage(Message message) {
        CompletableFuture<SendResult<String, Message>> future = kafkaTemplate.send(message.getTopic(), message);
        //TODO check if we need to add error handling
    }

    public void createKafkaTopic(String topicName,int numberOfPartition,int replicationFactor){
        NewTopic topic = new NewTopic(topicName, numberOfPartition, (short)replicationFactor);
        kafkaAdmin.createTopics(Collections.singletonList(topic));
    }

}
