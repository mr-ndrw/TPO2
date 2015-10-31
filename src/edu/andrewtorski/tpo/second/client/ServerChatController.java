package edu.andrewtorski.tpo.second.client;

import edu.andrewtorski.tpo.second.client.mvc.ChatController;
import edu.andrewtorski.tpo.second.client.mvc.ChatModel;
import edu.andrewtorski.tpo.second.client.mvc.ChatView;

import java.io.IOException;

/**
 * Created by andrew on 26.10.2015.
 */
public class ServerChatController implements ChatController {

    //region Private fields

    private ChatView chatView;
    private ChatModel chatModel;

    //endregion Private fields

    //region Constructors

    public ServerChatController(ChatView chatView, ChatModel chatModel) {
        this.chatView = chatView;
        this.chatModel = chatModel;
    }

    public ServerChatController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    //endregion Constructors

    //region Methods

    public void setChatView(ChatView chatView) {
        this.chatView = chatView;
    }

    public void setChatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    //endregion Methods

    //region ChatController interface methods

    @Override
    public ChatModel getModel() {
        return chatModel;
    }

    @Override
    public ChatView getView() {
        return chatView;
    }

    @Override
    public void passMessageToModel(String message) throws IOException {
        this.chatModel.sendMessage(message);
    }

    @Override
    public void receiveMessageFromModel(String message) {
        this.passMessageToView(message);
    }

    @Override
    public void passMessageToView(String message) {
        this.chatView.receieveMessageFromController(message);
    }

    @Override
    public void receiveMessageFromView(String message) throws IOException {
        this.passMessageToModel(message);
    }

    @Override
    public void logIn(String message) throws IOException {
        this.getModel().setUsername(message);
        this.getModel().logIn();
    }

    @Override
    public void logOut() throws IOException {
        this.getModel().logOut();
    }

    //endregion ChatController interface methods
}
