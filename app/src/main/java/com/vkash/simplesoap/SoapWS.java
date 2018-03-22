package com.vkash.simplesoap;

import android.util.Xml;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static java.net.HttpURLConnection.HTTP_OK;

public class SoapWS {

    private static final String URL_WS = "http://%1$s/%2$s/ws/%3$s";

    private final SoapCredentials mCredentials;

    public SoapWS(SoapCredentials credentials) {
        mCredentials = credentials;
    }

    private String getEnvelope(SoapRequest request) throws IOException {
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
        return writer.toString();
    }

    private URL getUrlWs() throws MalformedURLException {
        return new URL(String.format(URL_WS, mCredentials.getServer(), mCredentials.getBase(), mCredentials.getWsdl()));
    }

    private HttpURLConnection getConnection(SoapRequest request) throws IOException {
        URL url = getUrlWs();
        HttpURLConnection post = (HttpURLConnection) url.openConnection();
        post.setDoInput(true);
        post.setDoOutput(true);
        post.setRequestMethod("POST");
        post.setRequestProperty("SOAPAction", request.getNamespace() + "#" + request.getWSName() + ":" + request.getMethod());
        post.setRequestProperty("Accept-Encoding", "gzip,deflate");
        post.setRequestProperty("Authorization", mCredentials.getAuth());
        post.setRequestProperty("Content-type", "application/soap+xml;charset=utf-8");
        post.setRequestProperty("Connection", "close");
        post.setReadTimeout(mCredentials.getTimeout());
        post.setConnectTimeout(mCredentials.getTimeout());
        return post;
    }

    public File call(SoapRequest request) throws IOException, SAXException {

        String envelope = getEnvelope(request);
        HttpURLConnection post = getConnection(request);

        //request
        BufferedWriter outStream = null;
        try {
            outStream = new BufferedWriter(new OutputStreamWriter(post.getOutputStream(), "UTF-8"));
            outStream.write(envelope);
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }

        if (post.getResponseCode() != HTTP_OK) {
            throw new IOException(post.getResponseMessage());
        }

        //response
        InputStream is = post.getInputStream();
        if (post.getContentEncoding().equalsIgnoreCase("gzip")) {
            is = new GZIPInputStream(post.getInputStream());
        }

        File xml = File.createTempFile("soap_" + System.currentTimeMillis(), ".xml");
        BufferedReader inStream = null;
        try {
            inStream = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            Xml.parse(inStream, new ResponseHandler(xml));
        } finally {
            if (inStream != null) {
                inStream.close();
            }
            post.disconnect();
        }

        return xml;
    }
}
