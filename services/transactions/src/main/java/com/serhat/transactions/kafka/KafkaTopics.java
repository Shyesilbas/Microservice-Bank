package com.serhat.transactions.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopics {

    @Bean
    public NewTopic deposit(){
        return TopicBuilder.name("Deposit-transaction")
                .build();
    }
    @Bean
    public NewTopic withdrawal(){
        return TopicBuilder
                .name("Withdrawal-topic")
                .build();
    }
    @Bean
    public NewTopic transfer(){
        return TopicBuilder
                .name("Transfer-transaction")
                .build();
    }


}
