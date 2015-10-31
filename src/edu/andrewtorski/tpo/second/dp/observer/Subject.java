package edu.andrewtorski.tpo.second.dp.observer;

import edu.andrewtorski.tpo.second.server.dp.observer.ChatObserver;

import java.io.IOException;

/**
 * Created by andrew on 19.10.2015.
 */
public interface Subject {

    Observer attach(ChatObserver observer);

    Observer detach(ChatObserver observer);

    void notifyObservers(String message) throws IOException;
}
