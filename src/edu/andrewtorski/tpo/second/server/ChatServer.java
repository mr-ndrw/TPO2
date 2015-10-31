package edu.andrewtorski.tpo.second.server;

import edu.andrewtorski.tpo.second.server.dp.observer.ChatObserver;
import edu.andrewtorski.tpo.second.server.dp.observer.ChatSubject;
import edu.andrewtorski.tpo.second.utils.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Server which uses New Input Output Java mechanics to implement a Chat Server which adheres to a very simple protocol.
 * <p>
 * This protocol is as follows:
 * <p>
 * HEADER:MESSAGE
 * <p>
 * Mind the colon between HEADER and MESSAGE
 * <p>
 * Accepted headers are:
 * <p>
 * MSG - Message to everyone.
 * LOI - Log in a user
 * LOU - Log out a user
 * <p>
 * For particular headers messages that follows are:
 * <p>
 * For MSG - Simple text message.
 * LOI - Username of the user to be logged in.
 * LOU - Username of the user to log out.
 */
public class ChatServer implements Runnable {
    //region Enums

    private enum Request {
        LogIn("LOI:"),
        LogOut("LOU:"),
        Message("MSG:"),
        Error("ERR:");

        private final String requestHeader;

        Request(String requestHeader) {
            this.requestHeader = requestHeader;
        }
    }

    //endregion Enums

    //region Private Fields

    private static final int BUFFER_SIZE = 1024;
    private static final Charset charset = Charset.forName("UTF-8");
    public static final String TAG = "CHAT_SERVER";


    private String hostname;
    private int port;
    private ServerSocketChannel serverSocketChannel;
    private Selector serverSelector;
    private SelectionKey selectionKey;
    private boolean isRunning;

    private ByteBuffer operatingBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private ChatSubject chatSubject = new ChatSubject();

    //endregion Private Fields

    //region Constructors

    public ChatServer(String hostname, int port) throws IOException {
        this.hostname = hostname;
        this.port = port;

        Log.d(TAG, "Chat Server Socket Channel open.");
        this.configureAndOpen();
    }

    //endregion Constructors

    //region Methods

    /**
     * Core of the whole application which contains a connection loop for accepting, reading and writing messages.
     */
    public void run() {
        Log.d(TAG, "Running...");
        try {
            Log.d(TAG, "Awaiting connections...");
            while (true) {
                serverSelector.select();
                Iterator keysIterator = serverSelector.selectedKeys().iterator();
                while (keysIterator.hasNext()) {
                    SelectionKey key = (SelectionKey) keysIterator.next();
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

    //endregion Methods

    //region Private Methods

    /**
     * Configures and opens necessary channels, selectors and selection keys.
     */
    private void configureAndOpen() throws IOException {
        Log.d(TAG, "Creating socket address at hostname: " + this.hostname + " and port: " + this.port);
        SocketAddress serverSocketAddress = new InetSocketAddress(this.hostname, this.port);
        Log.d(TAG, "Binding server socket to address...");
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(serverSocketAddress);
        Log.d(TAG, "Server Socket bound and opened.");
        serverSocketChannel.configureBlocking(false);
        serverSelector = Selector.open();
        selectionKey = serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
        Log.d(TAG, "Selection key registered.");
    }

    /**
     * Processes the SelectionKey for key whether it's acceptable(and thus registering it's channel) or if it is readable
     * and thus sending provided SelectionKey's channel to processing.
     */
    private void processSelectionKey(SelectionKey selectionKey) throws IOException {
        SocketChannel clientSocketChannel;

        if (selectionKey.isAcceptable()) {
            Log.d(TAG, "Selection key has been accepted.");
            clientSocketChannel = serverSocketChannel.accept();
            clientSocketChannel.configureBlocking(false);
            clientSocketChannel.register(serverSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        }
        if (selectionKey.isReadable()) {
            Log.d(TAG, "Selection key is readable. Reading and processing channel...");
            clientSocketChannel = (SocketChannel) selectionKey.channel();
            this.processRequest(clientSocketChannel);
            Log.d(TAG, "Channel was read and processed.");
        }
    }

    /**
     * Processes the request which comes with the provided SocketChannel.
     */
    private void processRequest(SocketChannel clientSocketChannel) throws IOException {
        if (!clientSocketChannel.isOpen()) { //   if closed just return
            return;
        }
        Log.d(TAG, "Reading from socket channel...");
        String requestString = this.readFromSocketChannel(clientSocketChannel);
        Log.d(TAG, "Read request string + \n\t" + requestString);
        FurtherRequest request = this.processStringWithRequest(requestString);

        switch (request.requestType) {
            case LogIn: {
                Log.d(TAG, "Logging in...");
                this.logInEvent(request, clientSocketChannel);
                Log.d(TAG, "Logged in.");
                break;
            }
            case LogOut: {
                Log.d(TAG, "Logging out...");
                this.logOutEvent(request, clientSocketChannel);
                Log.d(TAG, "Logged out.");
                break;
            }
            case Message: {
                Log.d(TAG, "Sending message...");
                this.messageEvent(request);
                Log.d(TAG, "Message sent.");
                break;
            }
            case Error: {
                System.err.println("ERROR!");
                this.errorEvent();
                break;
            }
        }
    }

    //region Server events

    /**
     * Logs in an observer by associating his name which comes from the FurtherRequest's body and the SocketChannel
     * from which the log in message came. Logging-in operation is done by attaching the newly created observer to
     * the list of subscribers.
     */
    private void logInEvent(FurtherRequest request, SocketChannel newClientsSocketChannel) {
        ChatObserver chatObserver = new ChatObserver(request.getRequestBody(), newClientsSocketChannel);
        chatSubject.attach(chatObserver);
    }

    /**
     * Logs out an observer by detaching him from the list of subscribers.
     */
    private void logOutEvent(FurtherRequest request, SocketChannel clientsSocketChannel) {
        chatSubject.detach(request.getRequestBody());
    }

    /**
     * By utilizing the observer design pattern, the request body from provided FurtherRequest is propagated to subscribed
     * observers using ChatSubject object and thus sending the message.
     */
    private void messageEvent(FurtherRequest request) throws IOException {
        chatSubject.notifyObservers(request.getRequestBody());
        Log.d(TAG, request.getRequestBody());
        Log.d(TAG, String.format("%d", request.getRequestBody().length()));
    }

    private void errorEvent() {
        Log.d(TAG, "Blargh error");
    }

    //endregion Server events

    /**
     * Reads and returns data string from the specified SocketChannel using this' directly allocated buffer for speedy input. Read from buffer
     * is continued until any new-line character is encountered.
     */
    private String readFromSocketChannel(final SocketChannel socketChannel) throws IOException {
        operatingBuffer.clear();
        StringBuilder stringBuilder = new StringBuilder();
        readLoop:
        while (socketChannel.read(operatingBuffer) > 0) {
            operatingBuffer.flip();
            for (int i = operatingBuffer.position(); i < operatingBuffer.limit(); i++) {
                byte currentByte = operatingBuffer.get(i);
                char c = (char) currentByte;
                if (c == '\r' || c == '\n') {
                    break readLoop;
                }
                stringBuilder.append(c);
            }
        }
        //  remove any trailing whitespaces by calling String#trim() on returned String
        return stringBuilder.toString().trim();
    }

    /**
     * Processes the given request String and returns FurtherRequest which specifies what kind of Request it is(see
     * Request enumeration) and the requests body.
     */
    private FurtherRequest processStringWithRequest(String request) {
        String requestHeader = request.substring(0, 3);
        String requestBody = request.substring(4);

        FurtherRequest result;

        Request requestType;

        if ("LOI".equals(requestHeader)) {
            requestType = Request.LogIn;
        } else if ("LOU".equals(requestHeader)) {
            requestType = Request.LogOut;
        } else if ("MSG".equals(requestHeader)) {
            requestType = Request.Message;
        } else {
            requestType = Request.Error;
        }

        result = new FurtherRequest(requestType, requestBody);

        return result;
    }

    //endregion Private Methods

    //region FurtherRequest Class

    /**
     * Contains information about processed request like what kind of request it is and it's body.
     */
    class FurtherRequest {

        Request requestType;
        String requestBody;

        public FurtherRequest(Request requestType, String requestBody) {
            this.requestType = requestType;
            this.requestBody = requestBody;
        }

        public Request getRequestType() {
            return requestType;
        }

        public String getRequestBody() {
            return requestBody;
        }
    }

    //endregion FurtherRequest Class

}
