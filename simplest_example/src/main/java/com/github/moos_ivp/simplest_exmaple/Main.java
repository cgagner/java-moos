package com.github.moos_ivp.simplest_exmaple;

import com.github.moos_ivp.moosbeans.MOOSCommClient;
import com.github.moos_ivp.moosbeans.MOOSMsg;
import java.util.ArrayList;

/**
 *
 * @author cgagner
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MOOSCommClient client = new MOOSCommClient("SimplestExample", "localhost", 9000);
        client.setEnable(true);
        client.register("DEPLOY", 0.0);
        client.register("RETURN", 0.0);
        client.setMessageHandler((ArrayList<MOOSMsg> messages) -> {
            return onNewMail(messages);
        });

        // Listen for 30 seconds and quit
        try {
            Thread.sleep(30000);
            client.setEnable(false);

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    static boolean onNewMail(ArrayList<MOOSMsg> messages) {
        messages.stream().forEachOrdered(msg -> {
            System.out.println("Received Message: " + msg.getKey() + "="
                    + (msg.isDouble() ? msg.getDoubleData() : msg.getStringData()));
        });
        return true;
    }
}
