package zad1;

import edu.andrewtorski.tpo.second.server.ChatServer;

import java.io.IOException;

/**
 * @author Torski Andrzej S10415
 */


public class Server implements Runnable {

    public static void main(String[] args) {
//        System.out.println("Server hi!");
//        new Thread(new Server()).start();
        new Server().run();
    }


    @Override
    public void run() {
        try {
            ChatServer server = new ChatServer("localhost", 1337);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
