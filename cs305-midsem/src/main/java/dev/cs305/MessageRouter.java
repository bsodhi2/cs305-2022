package dev.cs305;

import io.javalin.Javalin;
import io.javalin.http.UploadedFile;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * The main log of message routing is implemented in this class.
 */
public class MessageRouter {
    /**
     * This is the HTTP form parameter under which the XML
     * file will be sent.
     */
    public static final String XML_DATA = "xml_data";
    private final DbHelper db;
    public static final MediaType MEDIA_TYPE_XML
            = MediaType.parse("text/xml; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Uses the JDBC URL for the DB. E.g.
     * "jdbc:sqlite:/Users/someuser/IdeaProjects/MyJavalin/mydatabase.db"
     * @param dbUrl
     */
    public MessageRouter(String dbUrl) {
        db = new DbHelper(dbUrl);
    }

    private static void log(String msg) {
        System.out.println(">>>>> "+msg);
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Required two params: 1) port number for this service, 2) DB URL\n");
            return;
        }
        int port = Integer.parseInt(args[0]);
        String url = args[1];
        MessageRouter app = new MessageRouter(url);
        app.startServer(port);
    }

    /**
     * Starts the service at the given port on the localhost.
     * @param port
     */
    private void startServer(int port) {
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.maxRequestSize = 1_000_000L;
        });
        app.post("/input", ctx -> {
            UploadedFile file = ctx.uploadedFile(XML_DATA);
            log("Received file of size: "+ctx.contentLength());
            String xml = extractXmlFromUploadedFile(file);
            log("Request data:\n"+xml);
            String[] ri = getSenderAndMessageType(xml);
            try {
                DbHelper.Route route = db.getDestinationForMessage(ri[0], ri[1]);
                log("Got route for sender='"+ri[0]+"' MsgType='"+ri[1]+"':\n"+route);
                String res = null;
                if (route.id == 0) {
                    res = "!!!! No destination found for message !!!";
                    log(res);
                } else {
                    db.saveEvent(route.id, "RECEIVED");
                    if (postMessage(route.destination, xml, file.getFilename())) {
                        db.saveEvent(route.id, "SENT_TO_DEST");
                        res = "Posted the message.";
                    } else {
                        db.saveEvent(route.id, "FAILED_TO_FWD");
                    }
                }
                ctx.result(res);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        app.start(port);
        System.out.println("Started the server!");
    }

    /**
     * XML file's data is extracted as a string from the uploaded file.
     * @param file
     * @return
     * @throws IOException
     */
    public static String extractXmlFromUploadedFile(final UploadedFile file) throws IOException {
        StringWriter writer = new StringWriter();
        assert file != null;
        IOUtils.copy(file.getContent(), writer, "UTF-8");
        return writer.toString();
    }

    /**
     * Sends an HTTP POST request to the given URL, with the given XML string as
     * a file upload.
     * @param toUrl
     * @param xml
     * @param fileName
     * @return
     */
    public static boolean postMessage(String toUrl, String xml, String fileName) {
        System.out.println("Posting message to: "+toUrl);
        RequestBody xmlPart = RequestBody.create(xml, MEDIA_TYPE_XML);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(XML_DATA, fileName, xmlPart)
                .build();

        Request request = new Request.Builder().url(toUrl)
                .post(requestBody).build();
        boolean status = false;
        try (Response response = client.newCall(request).execute()) {
            status = response.isSuccessful();
            log("Data sent successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * Extracts the sender and message type information from corresponding XML
     * elements from the supplied XML string.
     * @param xml
     * @return A String[] whose first item is the sender and second
     * item is the message type.
     * @throws XPathExpressionException
     */
    public static String[] getSenderAndMessageType(String xml) throws XPathExpressionException {
        XPath xp = XPathFactory.newInstance().newXPath();
        InputSource in = new InputSource(new StringReader(xml));
        Document root = (Document) xp.evaluate("/", in, XPathConstants.NODE);
        String sender = xp.evaluate("//Message/Sender/text()", root);
        String msgType = xp.evaluate("//Message/MessageType/text()", root);
        return new String[]{sender, msgType};
    }

}