package com.serhat.transactions.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfigs{

    @Bean
    public ProducerFactory<String, DepositEvent> producerFactory(){
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String , DepositEvent> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, WithdrawalEvent> producerFactoryForWithdrawal(){
        Map<String, Object> configPropsForWithdrawal = new HashMap<>();
        configPropsForWithdrawal.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configPropsForWithdrawal.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configPropsForWithdrawal.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configPropsForWithdrawal);
    }

    @Bean
    public KafkaTemplate<String , WithdrawalEvent> kafkaTemplateForWithdrawal(){
        return new KafkaTemplate<>(producerFactoryForWithdrawal());
    }

    @Bean
    public ProducerFactory<String, TransferEvent> producerFactoryForTransfer(){
        Map<String, Object> configPropsForTransfer = new HashMap<>();
        configPropsForTransfer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configPropsForTransfer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configPropsForTransfer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configPropsForTransfer);
    }
    @Bean
    public KafkaTemplate<String , TransferEvent> kafkaTemplateForTransfer(){
        return new KafkaTemplate<>(producerFactoryForTransfer());
    }

}
