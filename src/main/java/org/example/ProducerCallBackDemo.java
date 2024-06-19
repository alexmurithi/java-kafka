package org.example;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerCallBackDemo {

    private static final Logger log = LoggerFactory.getLogger(ProducerCallBackDemo.class.getSimpleName());

    public static void main(String[] args) {
        //Create Producer Properties

        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers","localhost:9094");

        //Set Producer Properties
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer",StringSerializer.class.getName());

        //Create the Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i <10; i++){
            //Create a Producer Record
            ProducerRecord<String, String> producerRecord =  new ProducerRecord<>("demo_java","Kafka world " +i);

            //send data
            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    //executes every time a record is sent successfully or an exception is thrown
                    if (e==null){
                        //the record was successfully sent
                        log.info("Received new Metadata \n" +
                                "Topic: " + metadata.topic() + "\n" +
                                "Partition: " + metadata.partition() + "\n" +
                                "Offset: " + metadata.offset() + "\n" +
                                "TimeStamp: " + metadata.timestamp()
                        );
                    }else {
                        log.error("An Error Occurred while processing",e);
                    }
                }
            });
        }


        //tell the producer to send all data and block until done -- synchronous
        producer.flush();

        //flush and close the Producer
        producer.close();

    }
}