package edu.andrewtorski.tpo.second.client;

import edu.andrewtorski.tpo.second.client.mvc.ChatController;
import edu.andrewtorski.tpo.second.client.mvc.ChatView;

import java.io.IOException;

public class CommandLineChatView implements ChatView {

    //region Private Fields

    private ChatController chatController;

    //endregion Private Fields

    //region Constructors

    public CommandLineChatView(ChatController chatController) {
        this.chatController = chatController;
    }

    //endregion Constructors

    //region ChatView interface methods

    @Override
    public ChatController getController() {
        return chatController;
    }

    @Override
    public void passMessageToController(String message) throws IOException {
        chatController.receiveMessageFromView(message);
    }

    @Override
    public void receieveMessageFromController(String message) {
        System.out.println(message);
    }

    @Override
    public void logIn(String userName) throws IOException {

    }

    @Override
    public void logOut() throws IOException {

    }

    //region ChatView interface methods
}
