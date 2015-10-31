package edu.andrewtorski.tpo.second.server.dp.observer;

import edu.andrewtorski.tpo.second.dp.observer.Observer;
import edu.andrewtorski.tpo.second.dp.observer.Subject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by andrew on 19.10.2015.
 */
public class ChatObserver implements Observer {

    //region Private Fields

    private String name;
    private SocketChannel clientSocketChannel;
    private ChatSubject chatSubject;

    //endregion Private Fields

    //region Constructors

    public ChatObserver(String name, SocketChannel clientSocketChannel) {
        this.name = name;
        this.clientSocketChannel = clientSocketChannel;
    }

    //endregion Constructors

    //region Methods

    public void close() throws IOException {
        clientSocketChannel.close();
    }

    public String getName() {
        return name;
    }

    //endregion Methods

    //region Observer Methods

    @Override
    public Subject getSubject() {
        return chatSubject;
    }

    @Override
    public Observer update(String message) throws IOException {
        ByteBuffer messageBuffer = ByteBuffer.wrap(message.getBytes());
        clientSocketChannel.write(messageBuffer);
        return this;
    }

    //endregion Observer Methods

}
