/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openconnectors.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.openconnectors.config.Config;
import org.openconnectors.connect.ConnectorContext;
import org.openconnectors.connect.PushSourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.openconnectors.config.ConfigUtils.verifyExists;

/**
 * Simple Kafka Source to emit strng messages from a topic
 */
public class KafkaSource implements PushSourceConnector<String> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaSource.class);

    private Consumer<String, String> consumer;
    private Properties props = new Properties();
    private String topic;
    private Boolean autoCommitEnabled;

    private Object waitObject;
    private java.util.function.Consumer<Collection<String>> consumeFunction;

    @Override
    public void open(Config config) throws Exception {

        verifyExists(config, ConfigKeys.KAFKA_SINK_TOPIC);
        verifyExists(config, ConfigKeys.KAFKA_SINK_BOOTSTRAP_SERVERS);
        verifyExists(config, ConfigKeys.KAFKA_SOURCE_GROUP_ID);
        verifyExists(config, ConfigKeys.KAFKA_SOURCE_FETCH_MIN_BYTES);
        verifyExists(config, ConfigKeys.KAFKA_SOURCE_AUTO_COMMIT_INTERVAL_MS);
        verifyExists(config, ConfigKeys.KAFKA_SOURCE_SESSION_TIMEOUT_MS);

        topic = config.getString("kafka.source.topic");
        autoCommitEnabled = config.getBoolean("kafka.source.auto_commit_enabled");

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString(ConfigKeys.KAFKA_SOURCE_BOOTSTRAP_SERVERS));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getString(ConfigKeys.KAFKA_SOURCE_GROUP_ID));
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, config.getString(ConfigKeys.KAFKA_SOURCE_FETCH_MIN_BYTES));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, config.getString(ConfigKeys.KAFKA_SOURCE_AUTO_COMMIT_INTERVAL_MS));
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, config.getString(ConfigKeys.KAFKA_SOURCE_SESSION_TIMEOUT_MS));

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

    }

    @Override
    public void close() throws IOException {
        LOG.info("Stopping kafka source");
        if(consumer != null) {
            consumer.close();
        }
        LOG.info("Kafka source stopped.");
    }

    public void start() throws Exception {
        Thread runnerThread = new Thread(() -> {
            LOG.info("Starting kafka source");
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(topic));
            LOG.info("Kafka source started.");
            ConsumerRecords<String, String> records;
            while(true){
                records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    LOG.debug("Message received from kafka, key: {}. value: {}", record.key(), record.value());
                    consumeFunction.accept(Collections.singleton(record.value()));
                }
                if (!autoCommitEnabled) {
                    consumer.commitSync();
                }
            }

        });
        runnerThread.setName("Kafka Source Thread");
        runnerThread.start();
    }

    @Override
    public String getVersion() {
        return KafkaConnectorVersion.getVersion();
    }

    @Override
    public void setConsumer(java.util.function.Consumer<Collection<String>> consumeFunction) {
        this.consumeFunction = consumeFunction;
    }

    @Override
    public void initialize(ConnectorContext ctx) {
        // Nothing for now.
    }

}