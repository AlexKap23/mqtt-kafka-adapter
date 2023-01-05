package mqtt.kafka.adapter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "topics")
@Setter
@Getter
public class Topic {
    @Id
    @JsonProperty("id")
    private String id;

    @Indexed
    @JsonProperty("topicName")
    private String topicName;

    public Topic(String topicName) {
        this.topicName = topicName;
    }
}
