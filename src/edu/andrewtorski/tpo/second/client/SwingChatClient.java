package edu.andrewtorski.tpo.second.client;

import edu.andrewtorski.tpo.second.client.mvc.ChatController;
import edu.andrewtorski.tpo.second.client.mvc.ChatView;
import edu.andrewtorski.tpo.second.utils.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class SwingChatClient extends JFrame implements ChatView {

    //region Private Fields

    private static String TAG = "SWING_CHAT_CLIENT";

    private static String TITLE = "Chatterrer";

    private ChatController chatController;

    private JTextArea ta_chat = new JTextArea(20, 64);
    private JTextArea ta_input = new JTextArea(1, 64);
    private JButton b_send = new JButton("Send [Enter]");

    private static int frameCounts = 1;

    //endregion Private Fields

    //region Constructors

    public SwingChatClient(ChatController chatController) {
        this.chatController = chatController;
        PreEntryLogInView.promptForUsername(this);
        initializeUI();
    }

    //endregion Constructors

    //region Private Helpers

    private void centerFrame() {
        Dimension windowSize = getSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();

        int dx = centerPoint.x - windowSize.width / 2;
        int dy = centerPoint.y - windowSize.height / 2;
        setLocation(dx, dy);
    }

    private void positionFrame() {
        Dimension windowSize = getSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();

        int dx, dy;

        if (frameCounts == 1) {
            dx = centerPoint.x / 2 - windowSize.width / 2;
            frameCounts++;
        } else {
            dx = centerPoint.x + centerPoint.x / 2 - windowSize.width / 2;
        }
        dy = centerPoint.y - windowSize.height / 2;
        setLocation(dx, dy);
    }

    private void initializeUI() {
        EnterKeyListener enterKeyListener = new EnterKeyListener();

        this.setTitle(TITLE);
        this.setSize(500, 500);
        //this.centerFrame();
        this.positionFrame();
        this.setResizable(false);
        this.addKeyListener(enterKeyListener);

        Container container = this.getContentPane();
        BoxLayout boxLayout = new BoxLayout(container, BoxLayout.Y_AXIS);
        container.setLayout(boxLayout);

        ta_chat.setLineWrap(true);
        ta_input.setLineWrap(true);
        ta_chat.setEnabled(false);

        ta_chat.setAlignmentX(Component.CENTER_ALIGNMENT);
        ta_input.setAlignmentX(Component.CENTER_ALIGNMENT);
        b_send.setAlignmentX(Component.RIGHT_ALIGNMENT);

        b_send.addActionListener(new SendButtonActionListener());
        ta_input.addKeyListener(enterKeyListener);

        ta_chat.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Chat"));
        ta_input.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Your message"));

        container.add(ta_chat);
        container.add(ta_input);
        container.add(b_send);
    }

    private void sendButtonClicked() {
        //  get the message from input text area
        String message = ta_input.getText();
        Log.d("TAG", "Send button clicked. ");
        if (message == null || message.length() == 0) {
            return;
        }
        Log.d(TAG, String.format("Message is as follows: " + message));
        //  clear it
        ta_input.setText("");
        //  call controller
        try {
            this.getController().receiveMessageFromView(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addMessageToChatTextArea(String message) {
        ta_chat.append(message + "\n");
    }

    //endregion Private Helpers

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
        this.addMessageToChatTextArea(message);
    }

    @Override
    public void logIn(String userName) throws IOException {
        Log.d(TAG, "Logging in and dispatching to controller.");
        this.getController().logIn(userName);
        this.setVisible(true);
        Log.d(TAG, "Dispatched and logged in. Windows is now visible.");
    }

    @Override
    public void logOut() throws IOException {
        Log.d(TAG, "Logging out and dispatching to controller...");
        this.getController().logOut();
        Log.d(TAG, "Logged out.");
    }

    //endregion ChatView interface methods

    //region SendButton action lister

    class SendButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            SwingChatClient.this.sendButtonClicked();
        }
    }

    //endregion SendButton action lister

    //region EnterKeyListener action listener

    private class EnterKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            int typedKey = e.getExtendedKeyCode();

            if (typedKey == KeyEvent.VK_ENTER) {
                SwingChatClient.this.sendButtonClicked();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    //endregion EnterKeyListener action listener
}
