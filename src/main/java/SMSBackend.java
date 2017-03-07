import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.instance.Sms;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.setPort;

import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.resource.list.CallList;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class SMSBackend {
    public static void main(String[] args) {

        //Heroku assigns different port each time, hence reading it from process.
        ProcessBuilder process = new ProcessBuilder();
        Integer port;
        if (process.environment().get("PORT") != null) {
            port = Integer.parseInt(process.environment().get("PORT"));
        } else {
            port = 4567;
        }
        Spark.port(port);


        get("/", (req, res) -> "Hello, World");
        
        get("/hello.xml", (req, res) -> {
            res.type("text/xml");
            String text = "<Response><Say voice=\"woman\">Hello World!</Say></Response>";
            return text;
        });

        TwilioRestClient client = new TwilioRestClient(System.getenv("TWILIO_ACCOUNT_SID"), System.getenv("TWILIO_AUTH_TOKEN"));

        post("/sms", (req, res) -> {
            String body = req.queryParams("Body");
            String to = req.queryParams("To");
            String from = System.getenv("TWILIO_NUMBER");

            Map<String, String> callParams = new HashMap<>();
            callParams.put("To", to);
            callParams.put("From", from);
            callParams.put("Body", body);
            Sms message = client.getAccount().getSmsFactory().create(callParams);

            return message.getSid();
        });
        
        post("/call", (req, res) -> {
            String url = req.queryParams("Url");
            String to = req.queryParams("To");
            String from = System.getenv("TWILIO_NUMBER");
            // Build a filter for the CallList
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("Url", url));
            params.add(new BasicNameValuePair("To", to));
            params.add(new BasicNameValuePair("From", from));
    
            CallFactory callFactory = client.getAccount().getCallFactory();
            Call call = callFactory.create(params);
            return call.getSid();
        });
    }
}
