package br.com.orangeTalents.ecommerce;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

//vai ter toda a lógica de construção do nosso producer
//Definimos generics para que cada produtor despache seu tipo espefífico, não mais apenas String
class KafkaDispatcher<T> implements Closeable {
    private final KafkaProducer<String, T> producer;

    public KafkaDispatcher() {
        this.producer = new KafkaProducer<>(properties());
    }

    private static Properties properties(){
        var properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GsonSerializer.class.getName());
        return properties;
    }

    void send(String topico, String key, T value) throws ExecutionException, InterruptedException {
        var record = new ProducerRecord<>(topico, key, value);
        Callback callback = (data, e) -> {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            System.out.println("enviado com sucesso " + data.topic() + " ::: partition: " + data.partition() + " / offset: " + data.offset() + " / timestamp " + data.timestamp());
        };
        producer.send(record, callback).get();
    }

    @Override
    public void close() {
        producer.close();
    }
}
