package mqtt.kafka.adapter.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Message {

   private String topic;
   private String payload;
   private String clientId;
   private String stationId;

    public Message(String topic, String payload, String clientId, String stationId) {
        this.topic = topic;
        this.payload = payload;
        this.clientId = clientId;
        this.stationId = stationId;
    }
}
