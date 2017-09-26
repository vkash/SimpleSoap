package com.vkash.simplesoap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SoapHandler extends DefaultHandler {

    private final BufferedWriter fileWriter;
    private boolean fault;

    public SoapHandler(File file) throws IOException {
        super();
        fileWriter = new BufferedWriter(new FileWriter(file));
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (qName.equalsIgnoreCase("soap:Fault")) {
            fault = true;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        try {
            fileWriter.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        try {
            fileWriter.write(ch, start, length);
        } catch (IOException ignored) {
        }
    }
}
