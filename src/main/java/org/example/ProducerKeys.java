package org.example;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerKeys {

    private static final Logger log = LoggerFactory.getLogger(ProducerKeys.class.getSimpleName());

    public static void main(String[] args) {
        //Create Producer Properties

        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers","localhost:9094");

        //Set Producer Properties
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer",StringSerializer.class.getName());

        //Create the Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int j=0; j<3; j++) {

            for (int i = 0; i < 10; i++) {

                String topic = "demo_java";
                String key = "id_" + i;
                String value = "Kafka Producer message " + i;

                //Create a Producer Record
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);

                //send data
                producer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        //executes every time a record is sent successfully or an exception is thrown
                        if (e == null) {
                            //the record was successfully sent
                            log.info("Key: " + key + "| Partition: " + metadata.partition());
                        } else {
                            log.error("An Error Occurred while processing", e);
                        }
                    }
                });

            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        //tell the producer to send all data and block until done -- synchronous
        producer.flush();

        //flush and close the Producer
        producer.close();

    }
}