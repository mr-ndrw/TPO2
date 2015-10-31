package edu.andrewtorski.tpo.second.run;

import edu.andrewtorski.tpo.second.server.ChatServer;

import java.io.IOException;

/**
 * @author Torski Andrzej S10415
 */


public class Server implements Runnable {

    public static void main(String[] args) {
        System.out.println("Server hi!");
        new Thread(new Server()).start();
    }

    @Override
    public void run() {
        try {
            ChatServer server = new ChatServer("localhost", 1337);
            Thread thread = new Thread(server);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
