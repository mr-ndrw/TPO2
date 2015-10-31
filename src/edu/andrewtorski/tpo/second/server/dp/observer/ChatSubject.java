package edu.andrewtorski.tpo.second.server.dp.observer;

import edu.andrewtorski.tpo.second.dp.observer.Observer;
import edu.andrewtorski.tpo.second.dp.observer.Subject;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andrew on 19.10.2015.
 */
public class ChatSubject implements Subject {


    //region Private Fields

    Set<ChatObserver> chatObservers = new HashSet<>();
    HashMap<String, ChatObserver> nameToObserverMap = new HashMap<>();
    HashMap<SocketChannel, ChatObserver> channelToObserverMap = new HashMap<>();
    //endregion Private Fields

    //region Subject Methods

    @Override
    public Observer attach(ChatObserver observer) {
        if (this.chatObservers.contains(observer) || observer == null) {
            return null;
        }
        this.chatObservers.add(observer);
        this.nameToObserverMap.put(observer.getName(), observer);
        return observer;
    }

    @Override
    public Observer detach(ChatObserver observer) {
        if (observer == null) {
            return null;
        }
        this.chatObservers.remove(observer);
        return observer;
    }

    public Observer detach(String nameOfTheObserver) {
        if (nameOfTheObserver == null || !nameToObserverMap.containsKey(nameOfTheObserver)) {
            return null;
        }

        ChatObserver foundObserverOfGivenName = nameToObserverMap.get(nameOfTheObserver);

        return detach(foundObserverOfGivenName);
    }

    @Override
    public void notifyObservers(String message) throws IOException {
        for (ChatObserver observer : chatObservers) {
            observer.update(message);
        }
    }

    //endregion Subject Methods
}
