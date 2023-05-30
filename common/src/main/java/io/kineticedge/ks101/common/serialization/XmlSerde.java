package io.kineticedge.ks101.common.serialization;

import org.apache.kafka.common.serialization.Serdes;
import org.w3c.dom.Document;


/**
 * Each Stream Thread gets its own Serde, so while the Xml DocumentFactory and Transformers, are not thread-safe
 * separate instances of this class, ensures thread safety.
 */
public final class XmlSerde extends Serdes.WrapperSerde<Document> {
    public XmlSerde() {
        super(new XmlSerializer(), new XmlDeserializer());
    }
}

