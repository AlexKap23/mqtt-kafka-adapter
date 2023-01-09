package mqtt.kafka.adapter;


import static mqtt.kafka.adapter.util.Constants.AVAILABLE_TOPICS;
import static mqtt.kafka.adapter.util.Constants.CLIENT_STATION_IDENTIFIER_TOPIC;
import static mqtt.kafka.adapter.util.Constants.COMMA_DELIMITER;
import static mqtt.kafka.adapter.util.Constants.DASH_DELIMITER;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import mqtt.kafka.adapter.model.Message;
import mqtt.kafka.adapter.model.Topic;
import mqtt.kafka.adapter.kafka.producer.KafkaMessageProducer;
import mqtt.kafka.adapter.repository.TopicRepository;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class MqttToKafkaAdapter {

    @Value("${mqtt.broker.uri}")
    private String mqttBrokerUri;

    @Value("${mqtt.user}")
    private String mqttUser;

    @Value("${mqtt.password}")
    private String mqttPassword;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private KafkaMessageProducer messageProducer;


    @PostConstruct
    private void listenForSensorMessages() {
        log.info("Initializing subscriber client...");
        String subscriberId = UUID.randomUUID().toString();
        try (IMqttClient subscriber = new MqttClient(mqttBrokerUri, subscriberId)) {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(mqttUser);
            options.setPassword(mqttPassword.toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            subscriber.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("connectionLost: " + throwable.getMessage());
                }
                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    log.info("Handling new mqtt message. Topic is " + topic);
                    handleTopics(topic,mqttMessage);
                    log.info("topic: " + topic);
                    log.info("message content: " + new String(mqttMessage.getPayload()));
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    System.out.println("deliveryComplete--------->" + iMqttDeliveryToken.isComplete());
                }
            });
            if(!subscriber.isConnected()){
                subscriber.connect(options);
            }
            String[] allAvailable = topicRepository.findAll().stream().map(Topic::getTopicName).toArray(String[]::new);
            subscriber.subscribe(allAvailable);
        } catch (MqttException e) {
            log.error("exception caught when initializing subscriber");
        }
    }

    private void handleTopics(String topic, MqttMessage mqttMessage){
        if(StringUtils.isEmpty(topic) || Objects.isNull(mqttMessage)) return;
        if(AVAILABLE_TOPICS.equalsIgnoreCase(topic)){
            List<String> availableTopics = Arrays.stream(new String(mqttMessage.getPayload()).split(COMMA_DELIMITER)).toList();
            availableTopics.forEach(this::persistTopicsOnMongoAndCreateKafkaTopics);
            //sending available topics on everyone that listens to this topic in case they need to handle them (weather app will do handle them)
            messageProducer.sendMessage(AVAILABLE_TOPICS,new Message(AVAILABLE_TOPICS,new String(mqttMessage.getPayload()),null,null));
        }else if(CLIENT_STATION_IDENTIFIER_TOPIC.equalsIgnoreCase(topic)){
            List<String> clientRef = Arrays.stream(new String(mqttMessage.getPayload()).split(DASH_DELIMITER)).toList();
            //list has on 0 the client id and on 1 the station id
            //need to check if we need this thing
        }else{
            //at this point all topics are created on kafka and should be persisted on mongo. So pushing every message on the respective kafka topic
            String [] mqttMessagePayloadParts = new String (mqttMessage.getPayload()).split(DASH_DELIMITER);
            if(ArrayUtils.isEmpty(mqttMessagePayloadParts) || mqttMessagePayloadParts.length<3){
                return;
            }
            String clientId = mqttMessagePayloadParts[0];
            String stationId = mqttMessagePayloadParts[1];
            String sensorValue = mqttMessagePayloadParts[2];
            Message msg = new Message(topic,sensorValue,clientId,stationId);
            messageProducer.sendMessage(topic,msg);
        }
    }

    @Transactional
    public void persistTopicsOnMongoAndCreateKafkaTopics(String topic){
        if(StringUtils.isEmpty(topic)) return;
        if(Objects.isNull(topicRepository.findByTopicName(topic))){
            topicRepository.save(new Topic(topic));
            //create kafka topic
            messageProducer.createKafkaTopic(topic,1,1); //need to check about partitions and replication factor
        }
    }
}
