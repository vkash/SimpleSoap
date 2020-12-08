package com.vkash.simplesoap;

import android.support.annotation.NonNull;
import android.util.Xml;
import org.xmlpull.v1.XmlSerializer;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

class Envelope {

    private final StringWriter writer = new StringWriter();

    Envelope(SoapRequest request) {

        String soapNS = "http://www.w3.org/2003/05/soap-envelope";
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

            for (Map.Entry<String, String> entry : request.getParams().entrySet()) {
                serializer.startTag(optNS, entry.getKey());
                serializer.text(entry.getValue());
                serializer.endTag(optNS, entry.getKey());
            }

            serializer.endTag(optNS, request.getMethod());
            serializer.endTag(soapNS, "Body");
            serializer.endTag(soapNS, "Envelope");
            serializer.endDocument();
        } catch (Exception ignore) {
        }
    }

    @NonNull
    @Override
    public String toString() {
        return writer.toString();
    }
}
