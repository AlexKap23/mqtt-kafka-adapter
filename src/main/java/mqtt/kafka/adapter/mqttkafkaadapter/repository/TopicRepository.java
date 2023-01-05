package mqtt.kafka.adapter.mqttkafkaadapter.repository;

import mqtt.kafka.adapter.mqttkafkaadapter.model.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TopicRepository extends MongoRepository<Topic,String> {

    @Query("{topicName: '?0'}")
    List<Topic> findByTopicName(String topicName);

}
