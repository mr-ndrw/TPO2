package edu.andrewtorski.tpo.second.run;

/**
 * @author Torski Andrzej S10415
 */


public class Main {

    public static void main(String[] args) {
        System.out.println("Main hi!");
        Thread serverThread = new Thread(new Server()),
                client1Thread = new Thread(new Client()),
                client2Thread = new Thread(new Client());

        serverThread.start();

        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client1Thread.start();
        client2Thread.start();

    }
}
