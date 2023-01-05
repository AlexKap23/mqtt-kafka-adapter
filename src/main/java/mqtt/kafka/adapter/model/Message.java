package mqtt.kafka.adapter.model;

public interface Message {

    String getPayload();
    String getTopic();

}
