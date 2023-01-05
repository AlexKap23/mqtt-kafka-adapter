package mqtt.kafka.adapter.mqttkafkaadapter.model;

public interface Message {

    String getPayload();
    String getTopic();

}
