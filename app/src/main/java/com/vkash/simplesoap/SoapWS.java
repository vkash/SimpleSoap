package com.vkash.simplesoap;

import android.util.Xml;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

public class SoapWS {

    private static final String URL_WS = "http://%1$s/%2$s/ws/%3$s";

    private final SoapCredentials mCredentials;

    public SoapWS(SoapCredentials credentials) {
        mCredentials = credentials;
    }

    public static String decode(InputStream stream) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(stream, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.CDSECT) {
                    return parser.getText();
                }

                eventType = parser.nextToken();
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getEnvelope(SoapRequest request) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        String soapNS = "http://www.w3.org/2003/05/soap-envelope";
        String optNS = request.getNamespace();

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
        return writer.toString().getBytes();
    }

    private URL getUrlWs() throws MalformedURLException {
        return new URL(String.format(URL_WS, mCredentials.getServer(), mCredentials.getBase(), mCredentials.getWsdl()));
    }

    private HttpURLConnection getConnection(SoapRequest request, int length) throws IOException {
        URL url = getUrlWs();
        HttpURLConnection post = (HttpURLConnection) url.openConnection();
        post.setDoInput(true);
        post.setDoOutput(true);
        post.setRequestMethod("POST");
        post.setRequestProperty("SOAPAction", request.getNamespace() + "#" + request.getWSName() + ":" + request.getMethod());
        post.setRequestProperty("Accept-Encoding", "gzip,deflate");
        post.setRequestProperty("Authorization", mCredentials.getAuth());
        post.setRequestProperty("Content-type", "application/soap+xml;charset=UTF-8");
        post.setRequestProperty("Content-Length", String.valueOf(length));
        post.setReadTimeout(mCredentials.getTimeout());
        return post;
    }

    public File call(SoapRequest request) throws IOException, SAXException {

        byte[] envelope = getEnvelope(request);
        HttpURLConnection post = getConnection(request, envelope.length);

        try {
            //request
            OutputStream outStream = null;
            try {
                outStream = new BufferedOutputStream(post.getOutputStream());
                outStream.write(envelope, 0, envelope.length);
                outStream.flush();
            } finally {
                if (outStream != null) {
                    outStream.close();
                }
            }

            if (post.getResponseCode() != HTTP_OK) {
                throw new IOException(post.getResponseMessage());
            }

            //response
            InputStream inStream = null;
            File file = null;
            try {
                inStream = new BufferedInputStream(post.getInputStream());
                file = parse(inStream);
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
            }

            return file;

        } finally {
            post.disconnect();
        }
    }

    private File parse(InputStream stream) throws IOException, SAXException {
        File xml = File.createTempFile("soap_" + System.currentTimeMillis(), ".xml");
        Xml.parse(stream, Xml.Encoding.UTF_8, new SoapHandler(xml));
        return xml;
    }
}
