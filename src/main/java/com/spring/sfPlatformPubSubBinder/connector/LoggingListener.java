package com.spring.sfPlatformPubSubBinder.connector;

import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.springframework.stereotype.Component;



@NoArgsConstructor
public class LoggingListener implements ClientSessionChannel.MessageListener {

    @Override
    public void onMessage(ClientSessionChannel clientSessionChannel, Message message) {
        if (message.isSuccessful()) {
            System.out.println(">>>>");
            printPrefix();
            System.out.println("Success:[" + clientSessionChannel.getId() + "]");
            System.out.println(message);
            System.out.println("<<<<");
        }

        if (!message.isSuccessful()) {
            System.out.println(">>>>");
            printPrefix();
            System.out.println("Failure:[" + clientSessionChannel.getId() + "]");
            System.out.println(message);
            System.out.println("<<<<");
        }
    }

    private void printPrefix() {
        System.out.print("[" + timeNow() + "] ");
    }

    private String timeNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        return dateFormat.format(now);
    }
}
