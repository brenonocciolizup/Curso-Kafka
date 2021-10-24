package br.com.orangeTalents.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.HashMap;
import java.util.Map;

public class FraudDetectorService {

    public static void main(String[] args) {
        var fraudService = new FraudDetectorService();
        try(var service = new KafkaService<>(FraudDetectorService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER", fraudService::parse, Order.class, Map.of())){
            service.run();
        }

    }

    private void parse(ConsumerRecord<String, Order> record) {
        System.out.println("___________________________________________");
        System.out.println("Processando NEW ORDER, checking for fraud");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
        System.out.println("Processado com sucesso");
    }
}



