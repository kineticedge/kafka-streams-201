package io.kineticedge.ks201.producer;

import io.kineticedge.ks101.common.util.PropertiesUtil;
import io.kineticedge.ks101.consumer.serde.JsonSerializer;
import io.kineticedge.ks201.domain.BagClaim;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class Producer {

    private static final Random random = new Random();

    private final static List<String> AIRLINES = List.of(
            "DELTA",
            "UNITED",
            "AMERICAN",
            "SPIRIT",
            "FRONTIER",
            "SOUTHWEST",
            "SUNCOUNTRY"
    );

    private final static List<String> AIRPORTS = List.of(
            "ATL",
            "DFW",
            "DEN",
            "ORD",
            "LAX",
            "LAS",
            "PHX",
            "JFK",
            "MSP",
            "BOS"
    );

    final KafkaProducer<String, BagClaim> kafkaProducer;

    private final Options options;

    public Producer(final Options options) {
        this.options = options;
        kafkaProducer = new KafkaProducer<>(properties(options));
    }

    public void close() {
        kafkaProducer.close();
    }

    public Future<RecordMetadata> publish(final String topic, final BagClaim customer) {
        log.info("Sending key={}, value={}", customer.getTrackingNumber(), customer);
        return kafkaProducer.send(new ProducerRecord<>(topic, null, null, customer.getTrackingNumber(), customer), (metadata, exception) -> {
            if (exception != null) {
                log.error("error producing to kafka", exception);
            } else {
                log.debug("topic={}, partition={}, offset={}", metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    public void start() {


        final KafkaProducer<String, BagClaim> kafkaProducer = new KafkaProducer<>(properties(options));

        int count = 0;
        while (true) {

            BagClaim bagClaim = create();

            log.info("Sending key={}, value={}", bagClaim.getTrackingNumber(), bagClaim);
            kafkaProducer.send(new ProducerRecord<>(options.getTopic(), null, null, bagClaim.getTrackingNumber(), bagClaim), (metadata, exception) -> {
                if (exception != null) {
                    log.error("error producing to kafka", exception);
                } else {
                    log.debug("topic={}, partition={}, offset={}", metadata.topic(), metadata.partition(), metadata.offset());
                }
            });

            try {
                long pause = options.getPause();
                log.info("pausing for={}", pause);
                Thread.sleep(pause);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            count++;
        }

        // kafkaProducer.close();

    }
    private Map<String, Object> properties(final Options options) {
        Map<String, Object> defaults = Map.ofEntries(
                Map.entry(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers()),
                //Map.entry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT"),
                Map.entry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()),
                Map.entry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName()),
                //Map.entry(ProducerConfig.LINGER_MS_CONFIG, 1L),
                Map.entry(ProducerConfig.ACKS_CONFIG, "all")
        );

        Map<String, Object> map = new HashMap<>(defaults);

        map.putAll(PropertiesUtil.load("/mnt/secrets/connection.properties"));

        return map;
    }

    private BagClaim create() {

        final BagClaim bagClaim = new BagClaim();

        bagClaim.setTrackingNumber(RandomStringUtils.randomAlphanumeric(10));

        String origin = airport();
        String destination = airport(origin);

        bagClaim.setAirlines(airlines());
        bagClaim.setOrigin(origin);
        bagClaim.setDestination(destination);
        bagClaim.setIssued(Instant.now());

        return bagClaim;
    }

    private static void dumpRecord(final ConsumerRecord<String, String> record) {
        log.info("Record:\n\ttopic     : {}\n\tpartition : {}\n\toffset    : {}\n\tkey       : {}\n\tvalue     : {}", record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }

    public static Properties toProperties(final Map<String, Object> map) {
        final Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    private static String airlines() {
        return AIRLINES.get(random.nextInt(AIRLINES.size()));
    }

    private static String airport() {
        return AIRPORTS.get(random.nextInt(AIRPORTS.size()));
    }

    private static String airport(String exclude) {

        String airport = exclude;

        while (exclude.equals(airport)) {
             airport = airport();
        }

        return airport;
    }

}
