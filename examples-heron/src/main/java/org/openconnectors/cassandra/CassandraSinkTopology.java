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

package org.openconnectors.cassandra;

import com.twitter.heron.streamlet.Builder;
import com.twitter.heron.streamlet.Config;
import com.twitter.heron.streamlet.KeyValue;
import com.twitter.heron.streamlet.Runner;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Basic topology to copy data fro stdin to std out, useful for experimentation
 */
public final class CassandraSinkTopology {

    private CassandraSinkTopology() {
    }

    public static String randomFromList(List<String> ls) {
        return ls.get(new Random().nextInt(ls.size()));
    }

    public static void main(String[] args) throws Exception {
        List<String> persons = Arrays.asList(
                "Mahatma Gandhi",
                "J F Kennedy",
                "Martin Luther King",
                "Abraham Lincoln"
        );
        List<String> sentences = Arrays.asList(
                "I have nothing to declare but my genius",
                "You can even",
                "Compassion is an action word with no boundaries",
                "To thine own self be true"
        );

        Builder processingGraphBuilder = Builder.createBuilder();
        processingGraphBuilder.newSource(() -> new KeyValue<>(randomFromList(persons), randomFromList(sentences)))
                .toSink(new HeronCassandraSink<>());
        Config config = new Config();
        config.setNumContainers(1);
        new Runner().run("CassandraSinkTopology", config, processingGraphBuilder);
    }
}