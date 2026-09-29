package com.redsocial.util;

import com.redsocial.exception.InternalServerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import io.github.cdimascio.dotenv.Dotenv;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


public class XMLUtil {


    public static String documentToString(Document document) throws InternalServerException {

        try {
            TransformerFactory factory =
                    TransformerFactory.newInstance();

            Transformer transformer =
                    factory.newTransformer();

            transformer.setOutputProperty(
                    OutputKeys.INDENT,
                    "yes"
            );

            transformer.setOutputProperty(
                    OutputKeys.ENCODING,
                    "UTF-8"
            );

            transformer.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION,
                    "no"
            );

            StringWriter writer =
                    new StringWriter();

            transformer.transform(
                    new DOMSource(document),
                    new StreamResult(writer)
            );

            return writer.toString();
        }
        catch (TransformerException e) {
            throw new InternalServerException("Error converting Document to String: " + e.getMessage());
        }
    }

    public static Document parseDocument(String xml) throws InternalServerException {

        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            return builder.parse(
                    new org.xml.sax.InputSource(
                            new StringReader(xml)
                    )
            );
        }
        catch (ParserConfigurationException | IOException | SAXException e) {
            throw new InternalServerException("Error parsing XML Document: " + e.getMessage());
        }
    }
}
