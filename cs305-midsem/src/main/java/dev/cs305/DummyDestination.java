package dev.cs305;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.UploadedFile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Creates dummy services listening on given ports and paths
 * on the localhost (i.e., 127.0.0.1) over HTTP.
 */
public class DummyDestination {
    private static Config cfg;
    public static class Config {
        public String routerUrl;
        public List<Map<String, String>> services;
        public List<Map<String, String>> requests;
    }

    /**
     * Prints the message on stdout.
     * @param msg
     */
    private static void log(String msg) {
        System.out.println("\t[Service] "+msg);
    }

    /**
     * Starts the program. Expects one CLI arg which is the path of the
     * config JSON file.
     *
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Starting dummy services.");
        Gson gs = new Gson();
        cfg = gs.fromJson(new FileReader(args[0]), Config.class);
        for (Map<String, String> svc : cfg.services) {
            startService(svc);
        }
        triggerMessageExchange(cfg.routerUrl, cfg.requests);
    }

    /**
     * Creates a dummy XML request message.
     * @param sender
     * @param msgType
     * @param payload
     * @return
     */
    private static String makeXmlMessage(String sender, String msgType, String payload) {
        String xmlTemplate = "<Message>\n" +
                "    <Sender>$SENDER$</Sender>\n" +
                "    <MessageType>$MSG_TYPE$</MessageType>\n" +
                "    <MessageUUID>$UUID$</MessageUUID>\n" +
                "    <Body>\n" +
                "        <![CDATA[\n" +
                "       $PAYLOAD$" +
                "       ]]>\n" +
                "    </Body>\n" +
                "</Message>";
        String xml = xmlTemplate;
        xml = xml.replace("$SENDER$", sender);
        xml = xml.replace("$MSG_TYPE$", msgType);
        xml = xml.replace("$UUID$", UUID.randomUUID().toString());
        return xml.replace("$PAYLOAD$", payload);
    }

    private static void triggerMessageExchange(String routerUrl,
                                               List<Map<String, String>> reqs) {
        for (Map<String, String> item : reqs) {
            String sender = item.get("sender");
            String msgType = item.get("messageType");
            String xml = makeXmlMessage(sender, msgType, "Payload for "+msgType);
            boolean status = MessageRouter.postMessage(routerUrl, xml, "mydata.xml");
            if (status) {
                log("Sent the request message to: "+routerUrl);
            } else {
                log("Failed to send the request message to: "+routerUrl);
            }
        }
    }

    private static void startService(Map<String, String> svcDef) {
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.maxRequestSize = 1_000_000L;
        });
        for (String path : svcDef.get("paths").split(",")) {
            String myUrl = "http://127.0.0.1:"+svcDef.get("port")+"/"+path;
            app.post(path, ctx -> {
                UploadedFile file = ctx.uploadedFile(MessageRouter.XML_DATA);
                assert file != null;
                log(path+":: Received file: "+file.getFilename());
                String xml = MessageRouter.extractXmlFromUploadedFile(file);
                String[] smt = MessageRouter.getSenderAndMessageType(xml);
                if (smt[1].startsWith("REPLY_")) {
                    log("Received the reply from "+smt[0]);
                } else {
                    log("Received a request of type "+smt[1]);
                    String xx = smt[0].substring(smt[0].lastIndexOf('/')+1).toUpperCase();
                    String xmlReply = makeXmlMessage(myUrl, "REPLY_"+xx, "Reply from me!");
                    MessageRouter.postMessage(cfg.routerUrl, xmlReply, "reply.xml");
                }
            });
        }
        app.start(Integer.parseInt(svcDef.get("port")));
        System.out.println("Started service at port "+svcDef.get("port")+
                " for paths "+svcDef.get("paths"));
    }
}
