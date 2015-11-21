package edu.andrewtorski.tpo.second.client.mvc;

import java.io.IOException;

/**
 * Operation contract for classes which will act as Controllers. Provides method stubs for passing data to ChatModel and
 * ChatView as well as receiving messages from them.
 */
public interface ChatController {

    //region Getters

    ChatModel getModel();

    ChatView getView();

    //endregion Getters

    //region Model methods

    /**
     * Passes the message from this ChatController to the bound ChatModel.
     */
    void passMessageToModel(String message) throws IOException;

    /**
     * Passes the data to THIS ChatController.
     * This method should be called by the bound ChatModel instance.
     */
    void receiveMessageFromModel(String message);

    //endregion Model methods

    //region View methods

    /**
     * Passes the message from this ChatController to the bound ChatView.
     */
    void passMessageToView(String message);

    /**
     * Passes the data to THIS ChatController.
     * This method should be called by the bound ChatView instance.
     */
    void receiveMessageFromView(String message) throws IOException;

    void logIn(String message) throws IOException;

    void logOut() throws IOException;

    //endregion View methods
}
