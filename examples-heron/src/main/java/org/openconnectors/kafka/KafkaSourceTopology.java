package org.openconnectors.kafka;

import com.twitter.heron.streamlet.Builder;
import com.twitter.heron.streamlet.Config;
import com.twitter.heron.streamlet.Runner;

public class KafkaSourceTopology {

    private KafkaSourceTopology() { }

    public static void main(String[] args) throws Exception {
        Builder processingGraphBuilder = Builder.createBuilder();
        processingGraphBuilder.newSource(new HeronKafkaSource<>()).log();
        Config config = new Config();
        config.setNumContainers(1);
        new Runner().run("KafkaSourceTopology", config, processingGraphBuilder);
    }
}
