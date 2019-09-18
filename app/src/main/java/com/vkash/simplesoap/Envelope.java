package com.vkash.simplesoap;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.util.Map;

class Envelope {

    private static final String soapNS = "http://www.w3.org/2003/05/soap-envelope";
    private final StringWriter writer = new StringWriter();

    Envelope(SoapRequest request) {

        String optNS = request.getNamespace();
        XmlSerializer serializer = Xml.newSerializer();

        try {
            serializer.setOutput(writer);
            serializer.setPrefix("soap", soapNS);
            serializer.setPrefix("opt", optNS);
            serializer.startTag(soapNS, "Envelope");
            serializer.startTag(soapNS, "Header");
            serializer.endTag(soapNS, "Header");
            serializer.startTag(soapNS, "Body");
            serializer.startTag(optNS, request.getMethod());

            for (Object o : request.getParams().entrySet()) {
                Map.Entry pair = (Map.Entry) o;

                serializer.startTag(optNS, (String) pair.getKey());
                serializer.text((String) pair.getValue());
                serializer.endTag(optNS, (String) pair.getKey());
            }

            serializer.endTag(optNS, request.getMethod());
            serializer.endTag(soapNS, "Body");
            serializer.endTag(soapNS, "Envelope");
            serializer.endDocument();
        } catch (Exception ignore) {
        }
    }

    @Override
    public String toString() {
        return writer.toString();
    }
}
