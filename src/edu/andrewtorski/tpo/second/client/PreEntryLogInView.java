package edu.andrewtorski.tpo.second.client;

import edu.andrewtorski.tpo.second.utils.Log;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

/**
 * Simple frame which prompts the user for his username and returns it.
 */
public class PreEntryLogInView extends JFrame {

    //region Private Fields

    private static String TAG = "PRE_ENTRY_LOG_IN_VIEW";

    private JLabel lab_username = new JLabel("What will be your username?");
    private JTextArea ta_username = new JTextArea(1, 20);
    private JButton b_confirm = new JButton("Sounds good! [Enter]");
    private SwingChatClient swingChatClient;

    //endregion Private Fields

    //region Constructors

    private PreEntryLogInView(SwingChatClient swingChatClient) {
        this.swingChatClient = swingChatClient;
        initializeUI();
    }

    //endregion Constructors

    public static void promptForUsername(SwingChatClient chatClient) {
        PreEntryLogInView prompt = new PreEntryLogInView(chatClient);
    }

    //region Private Helpers

    private void centerFrame() {

        Dimension windowSize = getSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();

        int dx = centerPoint.x - windowSize.width / 2;
        int dy = centerPoint.y - windowSize.height / 2;
        setLocation(dx, dy);
    }

    private void initializeUI() {
        this.setSize(250, 100);
        this.centerFrame();
        this.setResizable(false);

        Container container = this.getContentPane();
        BoxLayout boxLayout = new BoxLayout(container, BoxLayout.Y_AXIS);
        container.setLayout(boxLayout);

        lab_username.setAlignmentX(CENTER_ALIGNMENT);
        ta_username.setAlignmentX(CENTER_ALIGNMENT);
        b_confirm.setAlignmentX(CENTER_ALIGNMENT);

        b_confirm.addActionListener(new ConfirmButtonListener());
        b_confirm.setEnabled(false);
        ta_username.getDocument().addDocumentListener(new UserNameTextAreaDocumentListener());

        ta_username.addKeyListener(new EnterKeyListener());

        container.add(lab_username);
        container.add(ta_username);
        container.add(b_confirm);

        this.setVisible(true);
    }

    private void confirmUserName() {
        Log.d(TAG, "Confirm button clicked...");
        String textAreaContent = ta_username.getText();
        Log.d(TAG, String.format("Read text is: " + textAreaContent));
        boolean isTextAreaContentNullOrEmpty = textAreaContent == null || textAreaContent.isEmpty();

        if (!isTextAreaContentNullOrEmpty) {
            try {
                Log.d(TAG, "Dispatching event to SwingChatClient via log in.");
                this.swingChatClient.logIn(textAreaContent.trim());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            this.setVisible(false);
            this.dispose();
        }

    }

    //endregion Private Helpers

    //region ConfirmButtonListener inner class

    private class ConfirmButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            PreEntryLogInView.this.confirmUserName();
        }
    }

    //endregion ConfirmButtonListener inner class

    //region UserNameTextAreaDocumentListener inner class

    private class UserNameTextAreaDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            this.checkIfTextAreaIsEmpty(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            this.checkIfTextAreaIsEmpty(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            this.checkIfTextAreaIsEmpty(e);
        }

        private void checkIfTextAreaIsEmpty(DocumentEvent event) {
            Document document = event.getDocument();
            boolean shouldButtonBeEnabled = false;
            if (document.getLength() > 0) {
                shouldButtonBeEnabled = true;
            }
            PreEntryLogInView.this.b_confirm.setEnabled(shouldButtonBeEnabled);

        }
    }

    //endregion UserNameTextAreaDocumentListener inner class

    //region EnterKeyListener action listener

    private class EnterKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            int typedKey = e.getExtendedKeyCode();
            if (typedKey == KeyEvent.VK_ENTER) {
                PreEntryLogInView.this.confirmUserName();
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
