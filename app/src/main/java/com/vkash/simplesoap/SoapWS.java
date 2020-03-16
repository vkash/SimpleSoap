package com.vkash.simplesoap;

import android.util.Xml;

import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import static java.net.HttpURLConnection.HTTP_OK;

@SuppressWarnings("unused")
public class SoapWS {

    private final SoapCredentials mCredentials;

    public SoapWS(SoapCredentials credentials) {
        mCredentials = credentials;
    }

    private String getEnvelope(SoapRequest request) {
        return new Envelope(request).toString();
    }

    private URL getUrlWs() throws MalformedURLException {
        return new URL(String.format("http://%1$s/%2$s/ws/%3$s", mCredentials.getServer(), mCredentials.getBase(), mCredentials.getWsdl()));
    }

    private HttpURLConnection getConnection(URL url, SoapRequest request) throws IOException {
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

    public File call(SoapRequest request, boolean inMemory) throws IOException, SAXException {

        HttpURLConnection post = null;
        InputStream is = null;
        File body = getTempFile();

        try {
            post = getConnection(getUrlWs(), request);
            is = _call(post, getEnvelope(request));

            if (inMemory) {
                parse(is, body);
            } else {
                File tmp = getTempFile();
                dump(is, tmp);
                parse(tmp, body);
            }
        } finally {
            close(is);
            if (post != null) {
                post.disconnect();
            }
        }

        return body;
    }

    private File getTempFile() throws IOException {
        return File.createTempFile("soap_" + System.currentTimeMillis(), ".tmp");
    }

    private void request(OutputStream os, String envelope) throws IOException {
        BufferedWriter outStream = null;
        try {
            outStream = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            outStream.write(envelope);
        } finally {
            close(outStream);
        }
    }

    private void dump(InputStream is, File output) throws IOException {
        FileOutputStream fos = new FileOutputStream(output);

        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } finally {
            close(fos);
        }
    }

    private void parse(InputStream is, File output) throws IOException, SAXException {
        Xml.parse(is, Xml.Encoding.UTF_8, new ResponseHandler(output));
    }

    private void parse(File file, File output) throws IOException, SAXException {
        Xml.parse(new FileReader(file), new ResponseHandler(output));
    }

    private InputStream _call(HttpURLConnection conn, String envelope) throws IOException {

        request(conn.getOutputStream(), envelope);

        if (conn.getResponseCode() != HTTP_OK) {
            throw new IOException(conn.getResponseMessage());
        }

        InputStream is = conn.getInputStream();
        String encoding = conn.getContentEncoding();

        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            is = new GZIPInputStream(is);
        }

        return is;
    }

    private void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ignore) {
        }
    }
}
