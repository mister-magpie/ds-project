import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class lobbyGui
{
    private JFrame      frame;
    private JCheckBox   readyCheckBox;
    private JTextField  lobbyAddressTextField;
    private JButton     sendButton;
    private JButton     connectButton;
    private JTextField  chatText;
    private JPanel      panelMain;
    private JTextPane   textPane1;
    private JTextField  usernameField;
    private JTextArea   textArea1;
    private JScrollPane communicationScrollPane;
    private boolean opened;
    Timer t;
    Game G;

    public lobbyGui(Game game)
    {
        opened = true;
        this.G = game;
        readyCheckBox.setEnabled(false);

        printText(
                "Welcome! This is the lobby client." +
                        "\n" +
                        "Insert your username and the IP of the lobby server and then click Connect", true, true);


        sendButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                sendMessage();
            }
        });

        connectButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                String username = usernameField.getText();
                if (username == null)
                {
                    username = "anonymous";
                }
                Game.myself.setUsername(username);
                //connectiong phase
                try
                {
                    System.out.println("lobbygui try connecting");
                    printText("Connecting...", false, true);
                    String lobbyAddr = lobbyAddressTextField.getText();
                    //lobbyAddr = "192.168.1.7";
                    G.initializeLobby(lobbyAddr);
                }
                catch (RemoteException | NotBoundException | MalformedURLException e)
                {
                    e.printStackTrace();
                }
                printText("ok", true, true);

                //registering phase
                try
                {
                    printText("Registering...", false, true);
                    Game.register();
                }
                catch (RemoteException e)
                {
                    e.printStackTrace();
                }
                printText("ok", true, true);
                usernameField.setEditable(false);
                connectButton.setEnabled(false);
                //
                readyCheckBox.setEnabled(true);
                printText("when you are ready to start check the box", false, true);

                //update cycles
                t = new Timer();
                G.getUsers();
                updateList(t);
                //getUserList();
            }
        });

        usernameField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent keyEvent)
            {
                super.keyTyped(keyEvent);
                if (usernameField.getText().equals(""))
                {
                    connectButton.setEnabled(false);
                }
                else
                {
                    connectButton.setEnabled(true);
                }

            }
        });

        chatText.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                sendMessage();
            }
        });

        chatText.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent documentEvent)
            {
                if (!sendButton.isEnabled())
                {
                    sendButton.setEnabled(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent)
            {
                if (chatText.getText().isEmpty())
                {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent)
            {

            }
        });

        readyCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                boolean state = readyCheckBox.isSelected();
                System.out.println("am i ready? " + state);
                try
                {
                    Game.myself.ready = state;
                    Game.lobby.checkReady(Game.myself);
                }
                catch (RemoteException e)
                {
                    e.printStackTrace();
                }
            }
        });


    }

    private void sendMessage()
    {
        String msg = chatText.getText();
        chatText.setText("");
        G.getUsers();
        getUserList();
        for (Player p : G.players)
        {
            try
            {
                Registry      reg = LocateRegistry.getRegistry(p.address);
                IPlayerServer ps  = (IPlayerServer) reg.lookup(p.address + "/" + p.name);
                ps.recieveMessage(Game.myself.name, msg);
            }
            catch (NotBoundException | RemoteException e)
            {
                System.out.println(p.name + " not responding");
                G.players.remove(p.name);
                getUserList();
                //e.printStackTrace();
            }
        }
    }

    private void getUserList()
    {
        ArrayList<Player> users = new ArrayList<>();
        try
        {
            if (opened) users = Game.lobby.getPlayers();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        //update127.0.0.1
        G.players = users;
        textArea1.setMargin(new Insets(0, 10, 5, 5));

        textArea1.setText("");
        for (Player p : users)
        {
            textArea1.append("\n" + p.name + " - " + p.address);
        }

    }

    private void getChatMessages()
    {
        String msg = Game.myself.msgQueue.poll();
        if (msg != null)
        {
            printText(msg, false, false);
        }
    }

    public void updateList(Timer t)
    {
        t.schedule(new TimerTask()
        {
            @Override
            public void run() {
                if (opened) {
                    System.out.println("updateList!");
                    //update userlist
                    getUserList();
                    //update chat
                    getChatMessages();
                }
            }
        }, 500, 1000);
    }

    public void printText(String text, boolean append, boolean bold)
    {
        StyledDocument d = textPane1.getStyledDocument();

        if (!append)
        {
            text = "\n" + text;
        }

        SimpleAttributeSet as = new SimpleAttributeSet();
        StyleConstants.setBold(as, true);


        try
        {
            if (!bold)
            {
                d.insertString(d.getLength(), "\n" + text, null);
            }
            if (bold)
            {
                d.insertString(d.getLength(), text, as);
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }

    public JFrame initializeGUI()
    {

        /*for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(info.getClassName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException e) {
                    e.printStackTrace();
                }
                break;
            }
        }*/
        try
        {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (Exception e)
        {
            // If Nimbus is not available, you can set the GUI to another look and feel.
            // Set cross-platform Java L&F (also called "Metal")
            try
            {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1)
            {
                e1.printStackTrace();
            }
        }
        frame = new JFrame("Lobby Server");
        frame.setContentPane(new lobbyGui(G).panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent windowEvent)
            {
                System.out.println("chiudo tutto");

                if (Game.lobby != null)
                {
                    try
                    {

                        Game.lobby.unregister(Game.myself);
                    }
                    catch (RemoteException e)
                    {
                        e.printStackTrace();
                    }
                }

                super.windowClosing(windowEvent);
            }
        });
        return frame;
    }

    public void disposeGUI()
    {
        frame.setVisible(false);
        opened = false;
        System.out.println("opened is " + opened);
        t.cancel();
        t.purge();

    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        panelMain = new JPanel();
        panelMain.setLayout(new GridBagLayout());
        panelMain.setEnabled(true);
        panelMain.setMinimumSize(new Dimension(344, 400));
        panelMain.setPreferredSize(new Dimension(600, 400));
        lobbyAddressTextField = new JTextField();
        lobbyAddressTextField.setText("127.0.0.1");
        lobbyAddressTextField.setToolTipText("ip of lobby");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(lobbyAddressTextField, gbc);
        connectButton = new JButton();
        connectButton.setEnabled(false);
        connectButton.setText("Connect");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(connectButton, gbc);
        sendButton = new JButton();
        sendButton.setEnabled(false);
        sendButton.setText("Send");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(sendButton, gbc);
        communicationScrollPane = new JScrollPane();
        communicationScrollPane.setAutoscrolls(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(communicationScrollPane, gbc);
        textPane1 = new JTextPane();
        textPane1.setEditable(false);
        textPane1.putClientProperty("JEditorPane.honorDisplayProperties", Boolean.TRUE);
        communicationScrollPane.setViewportView(textPane1);
        usernameField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMain.add(usernameField, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Username");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelMain.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Server IP");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panelMain.add(label2, gbc);
        chatText = new JTextField();
        chatText.setToolTipText("you can send messages");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(chatText, gbc);
        readyCheckBox = new JCheckBox();
        readyCheckBox.setText("Ready?");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panelMain.add(readyCheckBox, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelMain.add(scrollPane1, gbc);
        textArea1 = new JTextArea();
        textArea1.setEditable(false);
        textArea1.setEnabled(true);
        textArea1.setLineWrap(false);
        scrollPane1.setViewportView(textArea1);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return panelMain;
    }
}