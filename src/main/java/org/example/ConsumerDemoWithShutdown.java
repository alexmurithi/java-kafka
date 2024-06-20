package org.example;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoWithShutdown {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class.getSimpleName());
    private static  final String GROUP_ID_KEY = "group.id";
    private static final String GROUP_ID_VALUE = "my-java-application";
    private static final String AUTO_OFFSET_RESET_KEY = "auto.offset.reset";
    private static final String AUTO_OFFSET_RESET_VALUE = "earliest";
    private static final String KAFKA_TOPIC = "demo_java";

    public static void main(String[] args) {
        //Create Consumer Properties

        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers","localhost:9094");

        //create consumer configs
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer",StringDeserializer.class.getName());
        properties.setProperty(GROUP_ID_KEY,GROUP_ID_VALUE);
        properties.setProperty(AUTO_OFFSET_RESET_KEY,AUTO_OFFSET_RESET_VALUE);//reading from the beginning of our topic

        //CREATE CONSUMER
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        //GET A REFERENCE TO THE MAIN THREAD
        final Thread thread  = Thread.currentThread();

        //adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                log.info("Detected a shutdown, exiting by calling consumer wakeup()...");
                consumer.wakeup();

                //JOIN THE MAIN THREAD TO ALLOW EXECUTION OF THE CODE IN THE MAIN THREAD
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            //SUBSCRIBE TO A TOPIC
            consumer.subscribe(Arrays.asList(KAFKA_TOPIC));

            //POLL FOR DATA
            while (true){
                log.info("polling....");

                ConsumerRecords<String,String> consumerRecords  = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String,String> record:consumerRecords){
                    log.info("Key: {} :  Value: {}",record.key(),record.value());
                    log.info("Partition: {} :  Offset: {}",record.partition(),record.offset());
                }
            }
        }catch (WakeupException e){
            log.info("Consumer is starting to shutdown", e);
        }catch (Exception e){
            log.info("Unexpected exception occurred",e);
        }finally {
            log.info("Gracefully shutting down the consumer...");
            consumer.close();
        }


    }
}