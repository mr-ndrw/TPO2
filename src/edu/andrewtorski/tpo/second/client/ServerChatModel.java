package edu.andrewtorski.tpo.second.client;

import edu.andrewtorski.tpo.second.client.mvc.ChatController;
import edu.andrewtorski.tpo.second.client.mvc.ChatModel;
import edu.andrewtorski.tpo.second.utils.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 */
public class ServerChatModel implements ChatModel, Runnable {

    //region Private Fields

    public static final String TAG = "SERVER_CHAT_MODEL";
    private static final int BUFFER_SIZE = 1024;

    private ChatController chatController;
    private String username = "Unknown";

    /**
     * May be IP address or http string.
     */
    private String hostName;
    private int port;
    private SocketChannel socketChannel;
    private Selector selector;

    /**
     * Contains ByteBuffers that should be written to the Server whenever the opportunity arises.
     */
    private LinkedList<ByteBuffer> pendingDataToBeSent = new LinkedList<>();

    /**
     * Directly allocated buffer for speedy inputs and outputs.
     */
    private ByteBuffer operatingBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    //endregion Private Fields

    //region Constructors

    public ServerChatModel(String ipAddress, int port, ChatController chatController) {
        this(ipAddress, port);
        this.chatController = chatController;
    }

    public ServerChatModel(String ipAddress, int port) {
        this.hostName = ipAddress;
        this.port = port;
        this.configureConnections();
    }

    //endregion Constructors

    //region Methods

    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
    }

    //endregion Methods

    //region Private Helpers

    /**
     * Instantiates and configures Selector and SocketChannel which will be utilized by this server.
     */
    private void configureConnections() {
        Log.d(TAG, "Configuring connections...");
        try {
            selector = SelectorProvider.provider().openSelector();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress(hostName, port));
        } catch (IOException e) {
            throw new RuntimeException("Something awful happened while opening and configuring connections.");
        }
        Log.d(TAG, "Connections configured.");
    }

    /**
     * Prepends the given message with username followed by a colon and space and returns it.
     */
    private String produceMessage(String message) {
        return this.getUserName() + ": " + message;
    }

    /**
     * Checks the provided key for it's type(whether its connectable, readable, writable) and invokes method on selector's
     * channel specific to it's type. If it's writable, call #write(SocketChannel) etc
     */
    private void processSelectionKey(SelectionKey key) throws IOException {
//        Log.d(TAG, "Processing selecection key...");
        SocketChannel cliSocketChannel;

        if (key.isConnectable()) {
            cliSocketChannel = (SocketChannel) key.channel();
            this.connect(cliSocketChannel);
        } else if (key.isReadable()) {
            cliSocketChannel = (SocketChannel) key.channel();
            this.read(cliSocketChannel);
        } else if (key.isWritable()) {
            cliSocketChannel = (SocketChannel) key.channel();
            this.write(cliSocketChannel);
        }
//        Log.d(TAG, "Selection key processed.");
    }

    /**
     * Finishes the connection process on the given SocketChannel by check if there is a connection and pending and finally
     * finishing it. Just for safety, given SocketChannel is also configured as nonblocking and registered with this'
     * Selector as READ and WRITE.
     */
    private void connect(SocketChannel socketChannelToConnect) throws IOException {
        Log.d(TAG, "Finishing connecting...");
        if (socketChannelToConnect.isConnectionPending()) {
            socketChannelToConnect.finishConnect();
        }
        socketChannelToConnect.configureBlocking(false);
        socketChannelToConnect.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        Log.d(TAG, "Connection finished and socket registered.");
    }

    /**
     * Reads the data string from the given channel by using this' directly allocated byte buffer until the end of line.
     * When done it trims the received String for any trailing whitespace and then passes it to the bound controller, by
     * calling #passMessageToController(String)
     */
    private void read(SocketChannel channelToRead) throws IOException {
        Log.d(TAG, "Reading from socket...");

        operatingBuffer.clear();
        StringBuilder stringBuilder = new StringBuilder();
        readLoop:
        while (socketChannel.read(operatingBuffer) > 0) {
            operatingBuffer.flip();
//            Log.d(TAG, "Read: " + bytes.toString());
            for (int i = operatingBuffer.position(); i < operatingBuffer.limit(); i++) {
                byte byteReadFromBuffer = operatingBuffer.get(i);
                char c = (char) byteReadFromBuffer;
//                Log.d(TAG, "Read char: " + c);
                if (c == '\r' || c == '\n')
                    break readLoop;
                stringBuilder.append(c);
            }
        }

        String readMessage = stringBuilder.toString().trim();
        Log.d(TAG, "Message read. Passing to controller...");
        this.passMessageToController(readMessage);
    }

    /**
     * Writes any data left on the queue(by LinkedList#pop()) to the given SocketChannel.
     * This should be synchronized.
     */
    private void write(SocketChannel channelToWrite) throws IOException {
//        Log.d(TAG, "Writing to socket...");
        synchronized (this.pendingDataToBeSent) {
            while (!pendingDataToBeSent.isEmpty()) {
                ByteBuffer buf = pendingDataToBeSent.pop();
                System.out.println();
                channelToWrite.write(buf);
                if (buf.remaining() > 0) {
                    break;
                }
            }
        }
//        Log.d(TAG, "Socket write complete.");
    }

    /**
     * Returns a nonblocking SocketChannel connected to this' specified hostName and port.
     */
    private SocketChannel initiateConnection() throws IOException {
        Log.d(TAG, "Initiating socket connection...");
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(this.hostName, this.port));
        Log.d(TAG, "Socket connection initiated.");
        return socketChannel;
    }

    /**
     * Appends given byte[] to the pendingDataToBeSent linked list and wakes up the selector to perform write operations.
     */
    private void queueDataToSend(byte[] data) throws IOException {
        SocketChannel socket = this.initiateConnection();

        synchronized (this.pendingDataToBeSent) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            pendingDataToBeSent.add(buffer);
        }

        this.selector.wakeup();
    }
    //endregion Private Helpers

    //region ChatModel interface methods

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public ChatController getController() {
        return chatController;
    }

    /**
     * Passes the given message to the bound controller through it's ChatController#receiveMessageFromModel(String)
     * method.
     */
    @Override
    public void passMessageToController(String message) {
        Log.d(TAG, "Passing message to controller...");
        //String prependedMessageWithUsername = this.produceMessage(message);
        chatController.receiveMessageFromModel(message);
        Log.d(TAG, "Message sent to controller...");
    }

    /**
     * Processes the given message by prepending 'MSG:' header and user name to it and queues it to be sent by calling
     * #queueDataToSend(byte[]).
     */
    @Override
    public void sendMessage(String message) throws IOException {
        Log.d(TAG, "Sending message...");
        String prependedMessageWithUsernameAndProtocolHeader = "MSG:" + this.produceMessage(message);
        byte[] dataBytes = prependedMessageWithUsernameAndProtocolHeader.getBytes();
        this.queueDataToSend(dataBytes);
        Log.d(TAG, "Message sent.");
    }

    /**
     * Logs in to the server by prepending the username with 'LOI:' header and then calling #queueDataToSend(byte[]).
     */
    @Override
    public void logIn() throws IOException {
        Log.d(TAG, "Logging in...");
        String logInMessage = "LOI:" + this.getUserName();
        byte[] dataBytes = logInMessage.getBytes();
        this.queueDataToSend(dataBytes);
        Log.d(TAG, "Logged in.");
    }

    /**
     * Logs out from the server by prepending the username with 'LOU:' header and then calling #queueDataToSend(byte[]).
     */
    @Override
    public void logOut() throws IOException {
        Log.d(TAG, "Logging out...");
        String logInMessage = "LOU:" + this.getUserName();
        byte[] dataBytes = logInMessage.getBytes();
        this.queueDataToSend(dataBytes);
        Log.d(TAG, "Logged out.");
    }

    //endregion ChatModel interface methods

    //region Runnable interface methods

    /**
     * Core of the whole model. Kicks off the connection loop.
     * Any configuration of selectors and socket channels is done beforehand in the constructors.
     */
    @Override
    public void run() {
        if (chatController == null) {
            throw new RuntimeException("Chat controller must be set prior to running the model!");
        }
        //  connection loop
        Log.d(TAG, "Running...");
        try {
            while (true) {
                this.selector.select();
                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();

                while (keysIterator.hasNext()) {
                    SelectionKey key = keysIterator.next();
                    keysIterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    this.processSelectionKey(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion Runnable interface methods
}
