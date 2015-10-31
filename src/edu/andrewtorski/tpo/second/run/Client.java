package edu.andrewtorski.tpo.second.run;

import edu.andrewtorski.tpo.second.client.ServerChatController;
import edu.andrewtorski.tpo.second.client.ServerChatModel;
import edu.andrewtorski.tpo.second.client.SwingChatClient;

import java.io.IOException;

/**
 * @author Torski Andrzej S10415
 */


public class Client implements Runnable {

    public static void main(String[] args) throws IOException {
        new Thread(new Client()).start();
    }

    @Override
    public void run() {
        System.out.println("Client hi!");
        ServerChatModel chatModel = new ServerChatModel("localhost", 1337);
        ServerChatController chatController = new ServerChatController(chatModel);
        chatModel.setChatController(chatController);
        SwingChatClient chatView = new SwingChatClient(chatController);
        chatController.setChatView(chatView);
        Thread thread = new Thread(chatModel);
        thread.start();
    }
}
