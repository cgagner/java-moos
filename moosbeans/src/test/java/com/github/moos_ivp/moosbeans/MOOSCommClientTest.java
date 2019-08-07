/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.moos_ivp.moosbeans;

import com.github.moos_ivp.moosbeans.comms.MessageType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cgagner
 */
public class MOOSCommClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(MOOSCommClientTest.class);
    public MOOSCommClientTest() {
    }

    @Test
    public void testTryToConnect() throws Exception {
    }

    @Test
    public void testRun() {
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
            //client.setFundamentalFrequency(0.5);
            client.setEnable(true);

            try {
                Thread.sleep(1000);
                

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
            client.register("RETURN", 0);
            //client.register("DEPLOY", 0);
            client.register("NODE_REPORT*", "*", 0);
            client.register("*", "*", 0);
            
            try {
                Thread.sleep(1000);
                

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
            double time = Utils.currentTime();
            client.notify("MOOS_MANUAL_OVERRIDE", "false", time);
            client.notify("DEPLOY", "true", time);
            client.notify("RETURN", "false", time);

            try {
                Thread.sleep(5000);
                

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
            LOG.info("Unregistering for all variables.");
            client.unregister("*", "*");
            
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

    @Test
    public void testGetNewMsgs() {
    }

    @Test
    public void testFindNewestMsg() {
    }

    @Test
    public void testReadNewMessages() throws Exception {
    }

    @Test
    public void testSendMessages() {
    }

    @Test
    public void testIterate() throws Exception {
    }

    @Test
    public void testConnectToServer() throws Exception {
    }

    @Test
    public void testDisconnectFromServer() {
    }

    @Test
    public void testHandshake() throws Exception {
    }

    @Test
    public void testIsRegisteredFor() {
    }

    @Test
    public void testNotify_MOOSMsg() {
    }

    @Test
    public void testNotify_3args_1() {
    }

    @Test
    public void testNotify_4args_1() {
    }

    @Test
    public void testNotify_3args_2() {
    }

    @Test
    public void testNotify_4args_2() {
    }

    @Test
    public void testNotify_3args_3() {
    }

    @Test
    public void testNotify_4args_3() {
    }

    @Test
    public void testRegister() {
    }

    @Test
    public void testPost() {
    }

    @Test
    public void testSetFundamentalFrequency() {
    }

    @Test
    public void testGetFundamentalFrequency() {
    }

    @Test
    public void testIsConnected() {
    }

    @Test
    public void testIsEnable() {
    }

    @Test
    public void testSetEnable() {
    }

    @Test
    public void testGetName() {
    }

    @Test
    public void testSetName() {
    }

    @Test
    public void testGetPort() {
    }

    @Test
    public void testSetPort() {
    }

    @Test
    public void testGetHostname() {
    }

    @Test
    public void testSetHostname() {
    }

    @Test
    public void testSetMessageHandler() {
    }

    @Test
    public void testGetMessageHandler() {
    }

    @Test
    public void testGetKeepAliveTime() {
    }

    @Test
    public void testSetKeepAliveTime() {
    }

    @Test
    public void testIsUseNameAsSrc() {
    }

    @Test
    public void testSetUseNameAsSrc() {
    }

    @Test
    public void testGetMAX_INBOX_MESSAGES() {
    }

    @Test
    public void testSetMAX_INBOX_MESSAGES() {
    }

    @Test
    public void testGetMAX_OUTBOX_MESSAGES() {
    }

    @Test
    public void testSetMAX_OUTBOX_MESSAGES() {
    }

    @Test
    public void testIsAutoReconnect() {
    }

    @Test
    public void testSetAutoReconnect() {
    }

    @Test
    public void testOutboxIsEmpty() {
    }
    
}
