/*   MOOS - Mission Oriented Operating Suite 
 *
 *   A suit of Applications and Libraries for Mobile Robotics Research
 *   Copyright (C) 2001-2005 Massachusetts Institute of Technology and
 *   Oxford University.
 *
 *   The original C++ version of this software was written by Paul Newman
 *   at MIT 2001-2002 and Oxford University 2003-2005.
 *   email: pnewman@robots.ox.ac.uk.
 *
 *   This Java version of MOOSClient is part of the MOOSBeans for Java
 *   package written by Benjamin C. Davis at Oxford University 2010-2011
 *   email: ben@robots.ox.ac.uk
 *
 *   This file is part of the MOOSBeans for Java package.
 *
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License as
 *   published by the Free Software Foundation; either version 2 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 *   02111-1307, USA.
 *
 *                      END_GPL
 */
package MOOS;

import MOOS.comms.MessageType;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MOOSCommClient extends MOOSCommObject implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MOOSCommClient.class);

    public static final int MOOS_SERVER_REQUEST_ID = -2;
    protected SocketChannel socket;
    protected int port;
    protected String hostname;
    protected String name; /// Application Name
    protected Stack<MOOSMsg> outboxList;
    protected ArrayList<MOOSMsg> inboxList;
    protected ArrayList<String> publishingList;
    protected TreeMap<String, Double> subscribingList;
    protected Thread theThread;
    protected boolean enable; // whether this thread is running or not.
    protected int MAX_INBOX_MESSAGES = 1000;
    protected int MAX_OUTBOX_MESSAGES = 500;
    protected double fundamentalFrequency = 5; // 200ms
    protected long keepAliveTime = 100; // 100ms
    protected long lastSentMsgTime = 0; // first msg will fire a null msg straight away if outbox is empty. This is keep it ticking over.
    protected double startTime;
    protected boolean doLocalTimeCorrection = false;
    protected boolean useNameAsSrc = true;
    protected int nextMsgID = 0; // start IDs at zero.
    protected MoosMessageHandler messageHandler = null;

    private static final String PROTOCOL_STRING = "ELKS CAN'T DANCE 2/8/10";
    private final static ByteBuffer HANDSHAKE_BUFFER;
    private final static int HANDSHAKE_BUFFER_SIZE = 32;

    static {
        HANDSHAKE_BUFFER = Utils.allocate(HANDSHAKE_BUFFER_SIZE);
        HANDSHAKE_BUFFER.put((PROTOCOL_STRING).getBytes());
        HANDSHAKE_BUFFER.put(new byte[HANDSHAKE_BUFFER_SIZE - PROTOCOL_STRING.length()]);
        HANDSHAKE_BUFFER.flip();
    }

    /**
     *
     * @param hostname MOOSDB hostname or IP address, i.e. localhost
     * @param port of MOOSBD default 9000
     */
    public MOOSCommClient(String hostname, int port) {
        this("JavaMOOSConnector", hostname, port);
    }

    public MOOSCommClient(String appName, String hostname, int port) {
        this.hostname = hostname;
        this.port = port;

        // Initialise message boxes
        outboxList = new Stack<>();
        inboxList = new ArrayList<>();

        // current not editing the publishingList.
        publishingList = new ArrayList<>();
        subscribingList = new TreeMap<>();

        name = appName;
    }
    
    /**
     * Used to indicate to the run() loop that we have already established the
     * connection so it doesn't need to.
     *
     */
    protected boolean manualConnect = false;
    /**
     * If this is set to true the auto reconnection is automatically attempted
     * continuously after 1st initial successful connection. If using
     * tryToConnect() this will throw an exception if 1st connection attempt
     * fails. if autoReconnect == true then this will still only later try and
     * reconnect if the 1st connection was succesfull and moos has been running
     * for a while.
     */
    protected boolean autoReconnect = true;

    /**
     * Automatically calls setEnable(true) to start the server monitoring
     * process if this succeeds. This will throw an exception if cannot connect.
     *
     * @throws IOException
     */
    public void tryToConnect() throws IOException {
        if (!this.enable) { // don't want to try and call if already running
            if (connectToServer()) {
                if (handshake()) {
                    manualConnect = true; // we managed to connect
                    this.setEnable(true); // starts the server loop thread.
                    return;
                }
                this.disconnectFromServer(); // only if tried part handshake()
                throw (new IOException("Couldn't Handshake with Server, no reason given...!"));
            }
            // if get to here then something failed quietly.
            throw (new IOException("Couldn't connect to Server, no reason given...!"));
        }
    }

    /**
     * The main loop - Create a socket connecting to the server. - handshake.
     * Exchange protocol details and wait for a welcome message. - Run round a
     * loop pausing for the fundamental frequency - call iterate() on every
     * iteration. This sends messages in outbox and receives all new MOOSPackets
     * containing messages, and orders then in the inbox in newest 1st.
     */
    @Override
    public void run() {
        while (enable) {
            try {
                if (manualConnect || connectToServer()) { // should only call connectToServer() if manualConnect is false. (i.e. user didn't call tryToConnect())
                    // this.enable = false;
                    //  return;
                    if (manualConnect || handshake()) {
                        sendRegistrationPackets();
                        while (enable && socket.isConnected()) {
                            try {
                                Thread.sleep((int) Math.floor((1000.0 / fundamentalFrequency)));
                                if (enable && socket.isConnected()) {
                                    iterate();
                                } else {
                                    if (enable) {
                                        LOG.info("MOOSDB disconnected us :-( \n");
                                    }
                                    LOG.info("Requesting Disconnect from the MOOSDB... \n");
                                    break;
                                }

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } catch (Exception e) {
                closeConnection();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            manualConnect = false;
            if (!isAutoReconnect()) {
                enable = false;
                break; // don't want to reconnect on disconnect.
            }
        }
        this.disconnectFromServer();

    }

    public synchronized ArrayList<MOOSMsg> getNewMsgs() {
        ArrayList<MOOSMsg> in = (ArrayList<MOOSMsg>) inboxList.clone();
        inboxList.clear();
        return in;
    }

    public static MOOSMsg findNewestMsg(Iterable<MOOSMsg> msgList, String varName) {
        for (MOOSMsg m : msgList) {
            if (m.getKey().equals(varName)) {
                return m;
            }
        }
        return null;
    }

    public synchronized void readNewMessages() throws IOException {
        MOOSCommPkt pktRx = new MOOSCommPkt();
        if (inboxList.size() > this.MAX_INBOX_MESSAGES) {
            //System.out.println("INBOX OVERFLOWING: CLEARING!");
            inboxList.clear();
        } // always empty the mail box.
        while (readPkt(socket, pktRx)) { // only add messages if managed to read a packet. Also the latest packet will be the newest so that gets added to the top of the list.
            ArrayList<MOOSMsg> tempList = new ArrayList<>();
            pktRx.serialize(tempList, false); // read messages into temp list
            inboxList.addAll(0, tempList); // add temp list to front of message queue as last packet received will contain the newest messages
            pktRx = new MOOSCommPkt();
        }
    }

    /**
     * This will also clear the message list once sent
     *
     * @param messages
     */
    public synchronized void sendMessages(Collection<MOOSMsg> messages) {
        // Send the whole list
        if (!messages.isEmpty()) {
            MOOSCommPkt PktTx = new MOOSCommPkt();
            PktTx.serialize(new ArrayList(messages), true); // convert from Stack to ArrayList.
            sendPkt(socket, PktTx);
        }
    }

    /**
     * Read incoming messages and send outgoing ones. This is called at the rate
     * of the thread loop
     *
     * @throws java.io.IOException
     */
    public void iterate() throws IOException {

        /// @TODO The keep alive needs to factor in the warp time, but we won't have
        /// a warp time right now. 
        long timeNow = System.currentTimeMillis();

        if (outboxList.isEmpty()) { // if empty we just send an NULL message to keep things ticking over. Sending at fundamental frequency seems a bit high
            if (timeNow - this.lastSentMsgTime > this.keepAliveTime) { // only if has been a while since last message
                this.lastSentMsgTime = timeNow;
                MOOSMsg msg = new MOOSMsg();
                //outboxList.add(msg);
                if (!post(msg)) {
                    LOG.error("Failed to send keep alive");
                }
            }
        } else {
            this.lastSentMsgTime = timeNow;
        }

        sendMessages(outboxList); // send the outbox
        outboxList.clear(); // clear the outbox

        //  try {
        readNewMessages();

        if (!inboxList.isEmpty()) {
            if (messageHandler != null) {
                messageHandler.handleMessages(getNewMsgs());
            } else {
                /// @todo Need to tell someone that messages have been received
                getNewMsgs().stream().forEach((msg) -> {
                    LOG.debug("No MessageHandler for: {} = {}", msg.getKey(), msg.isDouble() ? msg.getDoubleData() : msg.getStringData());
                });
            }
        }
    }

    public boolean connectToServer() throws IOException {
        if (socket != null && socket.isConnected()) {
            LOG.info("Client is already connected! ... disconnecting first");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        try {
            LOG.trace("Trying {}:{}", this.hostname, this.port);
            socket = SocketChannel.open();
            socket.socket().setReuseAddress(enable); // allows immediate reuse
            socket.socket().setSendBufferSize(4000000);
            socket.socket().setReceiveBufferSize(4000000);
            socket.socket().setPerformancePreferences(0, 1, 2); // we need low latency and bandwidth
            socket.connect(new InetSocketAddress(hostname, port));

            socket.configureBlocking(false); // We want this to be non-blocking.... so false?
            // socket.socket().setSoTimeout(10);

            if (socket.isConnected() && socket.socket().isConnected() && socket.socket().isBound()) {
                LOG.debug("Connected to Server.");
                return true;

            }

            return false;
        } catch (IOException ex) {
            // ex.printStackTrace();
            LOG.debug("Unable to connect to {}:{}", hostname, port);
            throw (ex);
            //    return false;
        }

    }

    protected boolean closeConnection() {
        this.inboxList.clear();
        this.outboxList.clear();
        if (socket != null && socket.isConnected()) {
            LOG.trace("Socket is still open, closing.....");
            try {
                socket.socket().close();
                socket.close();
                LOG.info("CLOSED!\n");
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
                return true;
            }
        } else {
            LOG.trace("Client is NOT connected! \n");
            return false;
        }
    }

    public synchronized boolean disconnectFromServer() {
        enable = false;
        return closeConnection();
    }

    public boolean handshake() throws IOException {
        try {
            LOG.trace("  Handshaking as {} ", name);
            if (doLocalTimeCorrection) {
                //SetMOOSSkew(0);
            } // put in MOOSMsg Little Endian bytebuffer so that it is correct ordering.

            HANDSHAKE_BUFFER.rewind();
            this.socket.write(HANDSHAKE_BUFFER);
            // Send a blank message
            MOOSMsg msg = new MOOSMsg(MessageType.Data, "", name);
            sendMsg(socket, msg);
            for (int i = 0; i < 50; i++) { // try to read 10 times of 3 seconds. Should be enough time to see if we receive a welcome

                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {

                }

                this.readNewMessages();
                for (MOOSMsg welcomeMsg : inboxList) {
                    if (welcomeMsg != null) {
                        if (welcomeMsg.isType(MessageType.Welcome)) {
                            LOG.trace(" Success! {}", welcomeMsg.getStringData());
                            LOG.info("Welcome Message: {}", welcomeMsg);
                            double skew = welcomeMsg.getDoubleData();
                            
                            double time = Utils.currentTime();
                            
                            if(Math.abs(welcomeMsg.getTime() - time) > 10000) {
                                double warpDetected = Math.round((welcomeMsg.getTime() / time) * 100.0) / 100.0;
                                LOG.info("Wraping detected: {}", warpDetected );
                                
                                Utils.setMoosTimeWarp(warpDetected);
                            }
                            
                            if (this.doLocalTimeCorrection) {
                                // SetMOOSSkew(skew);
                            }
                            inboxList.remove(welcomeMsg);
                            return true;
                        } else if (welcomeMsg.isType(MessageType.Poision)) {
                            // filthy MOOS
                            LOG.warn("MOOSDB Poisoned us, handshake() fail, why oh why?! : {}", welcomeMsg.getStringData());
                            return false;
                        } else {
                            LOG.warn("MOOSDB Handshake - Not welcome message?! : {} ... continuing to wait for welcome...", welcomeMsg.getStringData());
                        }
                    }
                }
            }
            LOG.error("MOOSDB Handshake Failed - no data?! : This probably means you are using an OLD MOOSDB! which doesn't accept the protocol string: {}", PROTOCOL_STRING);
            return false;

        } catch (IOException ex) {
            //  ex.printStackTrace();
            throw (ex);
            //  return false;
        }
    }

    // Functions from C++ port
    /**
     * check to see if we are registered to receive a variable
     *
     * @param variable
     * @return
     */
    public boolean isRegisteredFor(String variable) {
        return this.subscribingList.containsKey(variable);
    }

    /**
     *
     * @param msg The MOOSMsg created by the user.
     * @return whether this post was successful or not.
     */
    public boolean notify(MOOSMsg msg) {
        if (!this.publishingList.contains(msg.getKey())) {
            this.publishingList.add(msg.getKey());
        }
        return post(msg);
    }

    /**
     * notify the MOOS community that something has changed (double)
     *
     * @param var
     * @param dfVal
     * @param dfTime
     * @return
     */
    public boolean notify(String var, double dfVal, double dfTime) {
        if (!this.publishingList.contains(var)) {
            this.publishingList.add(var);
        }
        return post(new MOOSMsg(MessageType.Notify, var, dfVal, dfTime));

    }

    /**
     * notify the MOOS community that something has changed (double)
     *
     * @param var The variable you are sending
     * @param dfVal the value
     * @param srcAux extra source info
     * @param dfTime the timestamp for the data
     * @return
     */
    public boolean notify(String var, double dfVal, String srcAux, double dfTime) {

        MOOSMsg msg = new MOOSMsg(MessageType.Notify, var, dfVal, dfTime);
        msg.setSourceAuxInfo(srcAux);

        if (!this.publishingList.contains(var)) {
            this.publishingList.add(var);
        }

        return post(msg);
    }

    /**
     * notify the MOOS community that something has changed (String)
     *
     * @param var The variable you are sending
     * @param sVal the value
     * @param dfTime the timestamp for the data
     * @return
     */
    public boolean notify(String var, String sVal, double dfTime) {
        if (!this.publishingList.contains(var)) {
            this.publishingList.add(var);
        }
        return post(new MOOSMsg(MessageType.Notify, var, sVal, dfTime));

    }

    /**
     * notify the MOOS community that something has changed (String)
     *
     * @param var The variable you are sending
     * @param sVal the value
     * @param srcAux extra source info
     * @param dfTime the timestamp for the data
     * @return
     */
    public boolean notify(String var, String sVal, String srcAux, double dfTime) {
        MOOSMsg msg = new MOOSMsg(MessageType.Notify, var, sVal, dfTime);
        msg.setSourceAuxInfo(srcAux);

        if (!this.publishingList.contains(var)) {
            this.publishingList.add(var);
        }

        return post(msg);
    }

    /**
     * notify the MOOS community that something has changed (Binary)
     *
     * @param var The variable you are sending
     * @param binary the value
     * @param dfTime the timestamp for the data
     * @return
     */
    public boolean notify(String var, byte[] binary, double dfTime) {
        if (!this.publishingList.contains(var)) {
            this.publishingList.add(var);
        }
        return post(new MOOSMsg(MessageType.Notify, var, binary, dfTime));

    }

    /**
     * notify the MOOS community that something has changed (Binary)
     *
     * @param var The variable you are sending
     * @param binary the value
     * @param srcAux
     * @param dfTime the timestamp for the data
     * @return
     */
    public boolean notify(String var, byte[] binary, String srcAux, double dfTime) {
        if (!this.publishingList.contains(var)) {
            this.publishingList.add(var);
        }
        MOOSMsg msg = new MOOSMsg(MessageType.Notify, var, binary, dfTime);
        msg.setSourceAuxInfo(srcAux);

        return post(msg);
    }

    protected void sendRegistrationPackets() {
        for (String var : this.subscribingList.keySet()) {
            register(var, this.subscribingList.get(var));
        }
    }

    public boolean register(String var, double interval) {
        MOOSMsg MsgR = new MOOSMsg(MessageType.Register, var, interval, 1.0);
        boolean bSuccess = post(MsgR);
        if (bSuccess || (this.socket == null || this.socket.isConnected())) {
            if (!subscribingList.containsKey(var)) {
                subscribingList.put(var, interval);
            }

            // subscribingList.add(var);
        }
        return bSuccess;
    }

    /**
     * Register wildcard
     * @param varPattern
     * @param appPattern
     * @param interval
     * @return 
     */
    public boolean register(String varPattern, String appPattern, double interval) {
        if(varPattern.isEmpty()) {
            LOG.warn("Empty variable pattern in MOOSCommClient::register");
            return false;
        }
        
        if(appPattern.isEmpty()) {
            LOG.warn("Empty application pattern in MOOSCommClient::register");
            return false;
        }
        
        
        String msg = "AppPattern=" + appPattern + ",VarPattern=" + varPattern + ",Interval=" + interval;
        MOOSMsg MsgR = new MOOSMsg(MessageType.WildcardRegister, name, msg);
        LOG.debug("Wildcard: {}", MsgR );
        return post(MsgR);
    }
    
    public boolean unregister(String var) {
        if(!isConnected()) {
            return false;
        }
        if (this.subscribingList.containsKey(var)) {
            MOOSMsg MsgUR = new MOOSMsg(MessageType.Unregister, var, 0.0, 0.0);
            if (post(MsgUR)) {
                subscribingList.remove(var);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    public boolean unregister(String varPattern, String appPattern) {
        if(!isConnected()) {
            return false;
        }
        
        if(this.subscribingList.isEmpty()) {
            return true;
        }
        
        String msg = "AppPattern=" + appPattern + ",VarPattern=" + varPattern + ",Interval=0.0";
        return post(new MOOSMsg(MessageType.WildcardUnregister, name, msg));
    }
    
    
    public synchronized boolean post(MOOSMsg msg) { // we don't need to lock in Java if all the methods which deal with the inbox/outbox are synchronized
        if (socket == null || !this.socket.isConnected()) {
            return false;
        }

        //stuff our name in here  - prevent client from having to worry about
        //it...
        if (useNameAsSrc) {
            msg.setSource(name);
        } else if (!msg.isType(MessageType.Notify)) {
            msg.setSource(name);
        }

        if (msg.isType(MessageType.ServerRequest)) {
            msg.setMsgID(MOOS_SERVER_REQUEST_ID);
        } else {
            //set up Message ID;
            msg.setMsgID(nextMsgID++);
        }

        outboxList.push(msg);

        if (outboxList.size() > MAX_OUTBOX_MESSAGES) {
            outboxList.remove(outboxList.lastElement());
        }

        return true;
    }

    /**
     * This fails silently. Must satisfy: 0 &lt; frequency &le; 100
     *
     * @param frequency
     */
    public void setFundamentalFrequency(double frequency) {
        if (frequency < 0.0) {
            fundamentalFrequency = 1;
        } else if (frequency > 100) {
            fundamentalFrequency = 100;
        } else {
            fundamentalFrequency = frequency;
        }
        // this is how often we call into the MOOSDB to get messages!
        this.keepAliveTime = (long) (1000.0 / frequency);
    }

    /**
     *
     * @return the fundamental frequency
     */
    public double getFundamentalFrequency() {
        return this.fundamentalFrequency;
    }

    public boolean isConnected() {
        return this.enable && socket != null && socket.isConnected();
    }

    /**
     * @return the enable
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * Set to true to start the Thread
     *
     * @param enable the enable to set
     */
    public void setEnable(boolean enable) {
        if (!this.enable && enable) {
            // switch on
            if (theThread != null && theThread.isAlive()) {
                theThread.stop(); // This is the most retarded peace of code ever, should never get called, also its dirty. Sanity check...
            }            // Set up the thread
            theThread = new Thread(this);
            this.enable = enable;
            this.startTime = Utils.currentTime();
            theThread.start();

        } else if (this.enable && !enable) {
            // switch off
            this.enable = enable;
        }
        //all other cases can be ignored - idempotent

    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this MOOSclient for identification purposes on the MOOSDB
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @param messageHandler Current MoosMessageHandler. This may be null.
     */
    public void setMessageHandler(MoosMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * @return Current MoosMessageHandler. This may be null.
     */
    public MoosMessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    /**
     * @return the keepAliveTime
     */
    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    /**
     * @param keepAliveTime the keepAliveTime to set
     */
    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    /**
     * @return the useNameAsSrc
     */
    public boolean isUseNameAsSrc() {
        return useNameAsSrc;
    }

    /**
     * @param useNameAsSrc the useNameAsSrc to set
     */
    public void setUseNameAsSrc(boolean useNameAsSrc) {
        this.useNameAsSrc = useNameAsSrc;
    }

    /**
     * @return the MAX_INBOX_MESSAGES
     */
    public synchronized int getMAX_INBOX_MESSAGES() {
        return MAX_INBOX_MESSAGES;
    }

    /**
     * @param MAX_INBOX_MESSAGES the MAX_INBOX_MESSAGES to set
     */
    public synchronized void setMAX_INBOX_MESSAGES(int MAX_INBOX_MESSAGES) {
        this.MAX_INBOX_MESSAGES = MAX_INBOX_MESSAGES;
    }

    /**
     * @return the MAX_OUTBOX_MESSAGES
     */
    public int getMAX_OUTBOX_MESSAGES() {
        return MAX_OUTBOX_MESSAGES;
    }

    /**
     * @param MAX_OUTBOX_MESSAGES the MAX_OUTBOX_MESSAGES to set
     */
    public synchronized void setMAX_OUTBOX_MESSAGES(int MAX_OUTBOX_MESSAGES) {
        this.MAX_OUTBOX_MESSAGES = MAX_OUTBOX_MESSAGES;
    }

    /**
     * @return the autoReconnect
     */
    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    /**
     * @param autoReconnect the autoReconnect to set
     */
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public boolean outboxIsEmpty() {
        return (this.outboxList.empty());
    }
}
