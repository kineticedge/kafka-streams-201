package io.kineticedge.ks101.common.serialization;

import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * TODO thread safety.
 */
public class XmlSerializer implements org.apache.kafka.common.serialization.Serializer<Document>  {

    //TODO -- producer and thread safety.
    private final Transformer transformer;

    public XmlSerializer() {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] serialize(String topic, Document document) {
        try {
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
