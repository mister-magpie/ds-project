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

public class lobbyGui {
    private JFrame frame;
    private JCheckBox readyCheckBox;
    private JTextField lobbyAddressTextField;
    private JButton sendButton;
    private JButton connectButton;
    private JTextField chatText;
    private JPanel panelMain;
    private JList list1;
    private JTextPane textPane1;
    private JTextField usernameField;
    private JTextArea textArea1;
    Game G;

    public lobbyGui(Game game) {
        this.G = game;
        readyCheckBox.setEnabled(false);

        printText(
                "Welcome! This is the lobby client." +
                "\n" +
                "Insert your username and the IP of the lobby server and then click Connect",true,true);


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage();
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String username = usernameField.getText();
                if(username == null) username = "anonymous";
                G.myself.setUsername(username);
                //connectiong phase
                try {
                    printText("Connecting...",false,true);
                    String lobbyAddr = lobbyAddressTextField.getText();
                    //lobbyAddr = "25.72.70.109";
                    G.initializeLobby(lobbyAddr);
                } catch (RemoteException | NotBoundException | MalformedURLException e) {
                    e.printStackTrace();
                }
                printText("ok",true,true);

                //registering phase
                try {
                    printText("Registering...",false,true);
                    G.register();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                printText("ok",true,true);
                usernameField.setEditable(false);
                connectButton.setEnabled(false);
                //
                readyCheckBox.setEnabled(true);
                printText("when you are ready to start check the box",false,true);

                //update cycles
                try {
                    G.getUsers();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                updateList();
            }
        });

        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                super.keyTyped(keyEvent);
                if(usernameField.getText().equals("")) connectButton.setEnabled(false);
                else connectButton.setEnabled(true);

            }
        });

        chatText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
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

        readyCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean state = readyCheckBox.isSelected();
                try {
                    G.lobby.checkReady(G.myself);
                    G.myself.ready = state;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    private void sendMessage(){
        String msg = chatText.getText();
        chatText.setText("");
        for(Player p : G.players){
            try {
                Registry reg = LocateRegistry.getRegistry(p.address);
                IPlayerServer ps = (IPlayerServer) reg.lookup(p.address+"/"+p.name);
                ps.recieveMessage(G.myself.name, msg);
            } catch (NotBoundException | RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void getUserList(){
        ArrayList<Player> users = new ArrayList<>();
        try {
            users = G.lobby.getPlayers();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //update
        G.players = users;
        textArea1.setMargin(new Insets(0,10,5,5));

        textArea1.setText("");
        for(Player p : users){
            textArea1.append("\n"+p.name + " - " + p.address);
        }

    }
    private void getChatMessages(){
        String msg = G.myself.msgQueue.poll();
        if(msg != null){
            printText(msg,false,false);
        }
    }

    public void updateList() {
        Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run(){
                //update userlist
                getUserList();
                //update chat
                getChatMessages();
            }
        },0,500);
    }


    public void printText(String text,boolean append,boolean bold) {
        StyledDocument d = textPane1.getStyledDocument();

        if(!append) text = "\n" + text;

        SimpleAttributeSet as = new SimpleAttributeSet();
        StyleConstants.setBold(as,true);


        try {
            if(!bold) d.insertString(d.getLength(),"\n" + text,null);
            if(bold) d.insertString(d.getLength(),text,as);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public JFrame initializeGUI(){

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
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
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

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.out.println("chiudo tutto");

                if(G.lobby !=null){
                    try {

                        G.lobby.unregister(G.myself);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                super.windowClosing(windowEvent);
            }
        });
        return frame;
    }

    public void disposeGUI(){
        frame.setVisible(false);

    }
}