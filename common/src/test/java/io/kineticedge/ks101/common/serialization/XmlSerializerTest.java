package io.kineticedge.ks101.common.serialization;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

class XmlSerializerTest {


    @Test
    public void x() throws Exception {

        XmlDeserializer d = new XmlDeserializer();
        XmlSerializer s = new XmlSerializer();

        Document node = d.deserialize("topic", "<a><b x=\"y\">cc</b></a>".getBytes());
        node = d.deserialize("topic", "<a><b x=\"y\">cc</b><b x=\"z\">dd</b></a>".getBytes());


        XPath path = XPathFactory.newInstance().newXPath();

        XPathExpression expression = path.compile("//a/b[@x='z']/text()");

        NodeList nodeList = (NodeList) expression.evaluate(node, XPathConstants.NODESET);

        System.out.println("**");
        System.out.println(nodeList.getLength());
        System.out.println(nodeList.item(0).getTextContent());
        System.out.println("**");

        byte[] back = s.serialize("topic", node);

        System.out.println(new String(back));
    }

}