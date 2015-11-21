package edu.andrewtorski.tpo.second.client.mvc;

import java.io.IOException;

/**
 * Created by andrew on 26.10.2015.
 */
public interface ChatView {

    ChatController getController();

    void passMessageToController(String message) throws IOException;

    void receieveMessageFromController(String message);

    void logIn(String userName) throws IOException;

    void logOut() throws IOException;

}
