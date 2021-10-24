package br.com.orangeTalents.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

class KafkaService<T> implements Closeable {
    private final KafkaConsumer<String, T> consumer;
    private final ConsumerFunction parse;
    private final Class<T> clazz;
    private final Map<String, String> properties;

    KafkaService(String groupId, String topico, ConsumerFunction parse, Class<T> clazz, Map<String, String> properties) {
        this.parse = parse;
        this.consumer = new KafkaConsumer<>(getProperties(clazz, groupId, properties));
        this.clazz = clazz;
        this.properties = properties;
        //realiza a inscrição em um tópico
        consumer.subscribe(Collections.singleton(topico));

    }

    KafkaService(String groupId, Pattern topico, ConsumerFunction parse,
                 Class<T> clazz, Map<String, String> properties) {
        this.parse = parse;
        this.consumer = new KafkaConsumer<>(getProperties(clazz, groupId, properties));
        this.clazz = clazz;
        this.properties = properties;
        //realiza a inscrição em um tópico
        consumer.subscribe(topico);
    }

    void run() {
        while (true) {
            var records = consumer.poll(Duration.ofMillis(100));
            if (!records.isEmpty()) {
                System.out.println("Encontrado " + records.count() + " registros");
                for (var record : records) {
                    parse.consume(record);
                }
            }
        }
    }

    private Properties getProperties(Class<T> clazz, String groupId, Map<String, String> overrideProperties) {
        var properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        //deserializa de byte para string
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        properties.setProperty(GsonDeserializer.TYPE_CONFIG, clazz.getName());
        properties.putAll(overrideProperties);
        return properties;
    }

    @Override
    public void close() {
        consumer.close();
    }
}