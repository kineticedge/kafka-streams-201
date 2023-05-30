package io.kineticedge.ks101.common.serialization;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This class uses DocumentBuilder which is not thread-safe. However, consumers poll in a single thread, so
 * this is not an issue.
 */
public class XmlDeserializer implements org.apache.kafka.common.serialization.Deserializer<Document> {

    private final DocumentBuilder documentBuilder;

    public XmlDeserializer() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Document deserialize(String topic, byte[] bytes) {
        try {
            documentBuilder.reset();
            return documentBuilder.parse(new ByteArrayInputStream(bytes));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
