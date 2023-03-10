package mqtt.kafka.adapter.repository;

import mqtt.kafka.adapter.model.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TopicRepository extends MongoRepository<Topic,String> {

    @Query("{topicName: '?0'}")
    Topic findByTopicName(String topicName);

}
