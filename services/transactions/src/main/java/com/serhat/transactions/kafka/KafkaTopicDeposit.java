package com.serhat.transactions.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicDeposit {

    @Bean
    public NewTopic newTopic(){
        return TopicBuilder.name("Deposit-transaction")
                .build();
    }
    @Bean
    public NewTopic topic(){
        return TopicBuilder
                .name("Withdrawal-topic")
                .build();
    }


}
