package io.kineticedge.ks201.streams;

import io.kineticedge.ks101.common.streams.KafkaStreamsConfigUtil;
import io.kineticedge.ks101.common.streams.ShutdownHook;
import io.kineticedge.ks201.domain.BagClaim;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.metrics.Sensor;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.processor.internals.InternalProcessorContext;
import org.apache.kafka.streams.processor.internals.metrics.StreamsMetricsImpl;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.kafka.streams.processor.internals.metrics.StreamsMetricsImpl.*;

@Slf4j
public class Streams {


    private Map<String, Object> properties(final Options options) {
        return KafkaStreamsConfigUtil.properties(options.getBootstrapServers(), options.getApplicationId());
    }

    public void start(final Options options) {

        Properties p = toProperties(properties(options));

        log.info("starting streams : " + options.getClientId());

        final Topology topology = streamsBuilder(options).build(p);

        log.info("Topology:\n" + topology.describe());

        final KafkaStreams streams = new KafkaStreams(topology, p);

        streams.setUncaughtExceptionHandler(e -> {
            log.error("unhandled streams exception, shutting down (a warning of 'Detected that shutdown was requested. All clients in this app will now begin to shutdown' will repeat every 100ms for the duration of session timeout).", e);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
        });

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(streams)));

    }

    private StreamsBuilder streamsBuilder(final Options options) {

        final var builder = new StreamsBuilder();

        final var materialized = Materialized.<String, BagClaim, KeyValueStore<Bytes, byte[]>>as("aggregate").withCachingDisabled();

        final KStream<String, BagClaim> name = builder.<String, BagClaim>stream(options.getInputPattern(), Consumed.as("input"));

        name
                //.peek((k, v) -> log.debug("key={}, value={}", k, v), Named.as("peek-in"))

                .processValues(() -> new FixedKeyProcessor<String, BagClaim, BagClaim>() {

                    private final Map<String, Sensor> sensors = new HashMap<>();

                    private FixedKeyProcessorContext<String, BagClaim> context;

                    @Override
                    public void init(final FixedKeyProcessorContext<String, BagClaim> context) {
                        this.context = context;
                    }

                    @Override
                    public void process(final FixedKeyRecord<String, BagClaim> record) {

                        context.recordMetadata().ifPresent((m) -> {

                            log.info("PROCESSING: topic={}, partition={}, offset={}, latency={}", m.topic(), m.partition(), m.offset(), (System.currentTimeMillis() - record.timestamp()));

                            get(m.topic()).record(System.currentTimeMillis() - record.timestamp());
                        });

                        context.forward(record);
                    }

                    public Sensor get(final String topicName) {
                        return sensors.computeIfAbsent(topicName, (t) -> createSensor(
                                        Thread.currentThread().getName(),
                                        context.taskId().toString(),
                                        ((InternalProcessorContext<String, BagClaim>) context).currentNode().name(),
                                        topicName,
                                        (StreamsMetricsImpl) context.metrics()
                                )
                        );
                    }

                    public Sensor createSensor(final String threadId, final String taskId, final String processorNodeId, final String topicName, final StreamsMetricsImpl streamsMetrics) {
                        final Sensor sensor = streamsMetrics.topicLevelSensor(threadId, taskId, processorNodeId, topicName, processorNodeId + "-latency", Sensor.RecordingLevel.INFO);
                        addAvgAndMinAndMaxToSensor(
                                sensor,
                                TOPIC_LEVEL_GROUP,
                                streamsMetrics.topicLevelTagMap(threadId, taskId, processorNodeId, topicName),
                                "latency",
                                "average",
                                "minimum",
                                "maximum"
                        );
                        return sensor;
                    }

                }, Named.as("sensor"))
                .groupByKey(Grouped.as("groupByKey"))
                .aggregate(
                        BagClaim::new,
                        (key, event, aggregate) -> event,
                        Named.as("aggregate"),
                        materialized
                )
                .toStream(Named.as("toStream"))
                //.peek((k, v) -> log.debug("key={}, value={}", k, v), Named.as("peek-out"))
                .to(options.getOutput(), Produced.as("output"));

        return builder;
    }


    private static void dumpRecord(final ConsumerRecord<String, String> record) {
        log.info("Record:\n\ttopic     : {}\n\tpartition : {}\n\toffset    : {}\n\tkey       : {}\n\tvalue     : {}", record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }

    public static Properties toProperties(final Map<String, Object> map) {
        final Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

}
