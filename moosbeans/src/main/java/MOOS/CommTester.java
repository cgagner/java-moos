/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MOOS;

import MOOS.comms.MessageType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cgagner
 */
public class CommTester {

    private static final Logger LOG = LoggerFactory.getLogger(CommTester.class);

    public static void main(String[] args) {
        try {

            // test code for packets and msgs
            LOG.info("Executing a few tests...");
            //String moosHost = properties.getProperty("moos_host");
            //int moosPort = new Integer(properties.getProperty("moos_port"));

            String moosHost = "localhost";
            int moosPort = 9000;

            MOOSMsg smsg = new MOOSMsg(MessageType.Data, "TestVariable", "test");
            MOOSMsg smsg1 = new MOOSMsg(MessageType.Data, "TestVariable", "test", 12345);
            MOOSMsg smsg2 = new MOOSMsg(MessageType.Data, "TestVariable", 3.145d, 54321);
            smsg.setSource("MeJavaMan");
            MOOSCommPkt pkt = new MOOSCommPkt();
            ArrayList<MOOSMsg> l = new ArrayList<>();
            l.add(smsg);
            l.add(smsg1);
            l.add(smsg2);
            ByteBuffer b;
            b = Utils.allocate(smsg.getSizeInBytesWhenSerialised());

            pkt.serialize(l, true);
            b = pkt.getBytes();
            ArrayList<MOOSMsg> l2 = new ArrayList<MOOSMsg>();
            MOOSCommPkt pkt2 = new MOOSCommPkt();
            pkt2.packetData = b; // need setter method
            pkt2.fill();
            pkt2.serialize(l2, false);

            LOG.info("Tests Finished");

            // end of test code

            /* test stopping and starting the client */
            MOOSCommClient client = new MOOSCommClient(moosHost, moosPort);
            client.setEnable(true);
            client.register("RETURN", 0);
            client.register("DEPLOY", 0);
            client.register("NODE_REPORT", 0);
            client.register("NODE_REPORT_LOCAL", 0);
            try {
                Thread.sleep(30000);
                client.setEnable(false);

            } catch (InterruptedException ex) {
                ex.printStackTrace();
                //     Logger.getLogger(MOOSCommClient.class.getName())..log(Level.SEVERE, null, ex);

            }

        } catch (NoJavaZipCompressionSupportYetException ex) {
            ex.printStackTrace();
        }
    }
}
