package edu.andrewtorski.tpo.second.client.mvc;

import java.io.IOException;

/**
 * Operation contract for classes which will act as Models.
 * Provides method stubs for getting the data from the outside system as well as passing messages to the bound
 * controller.
 * This interface sets a requirement for the implementing class to contain some sort of userName field.
 * This interface also provides methods for logging in and out the user.
 */
public interface ChatModel {

    //region Getters and Setters

    ChatController getController();

    void setUsername(String message);

    String getUserName();

    //endregion Getters and Setters

    //region Methods

    void passMessageToController(String message);

    void sendMessage(String message) throws IOException;

    void logIn() throws IOException;

    void logOut() throws IOException;

    //endregion Methods
}
