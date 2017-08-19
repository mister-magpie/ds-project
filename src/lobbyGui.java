import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

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

    public lobbyGui() {

        readyCheckBox.setEnabled(false);

        printText(
                "Welcome! This is the lobby client." +
                "\n" +
                "Insert your username and the IP of the lobby server and then click Connect",true,true);


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String msg = chatText.getText();
                chatText.setText("");
                for(Player p : Game.players){
                    try {
                        IPlayerServer ps = (IPlayerServer) Naming.lookup(p.address);
                        ps.recieveMessage(Game.myself.name, msg);
                    } catch (NotBoundException | MalformedURLException | RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String username = usernameField.getText();
                if(username == null) username = "anonymous";
                Game.myself.setUsername(username);
                //connectiong phase
                try {
                    printText("Connecting...",false,true);
                    //String lobbyAddr = lobbyAddressTextField.getText();
                    Game.initializeLobby();
                } catch (RemoteException | NotBoundException | MalformedURLException e) {
                    e.printStackTrace();
                }
                printText("ok",true,true);

                //registering phase
                try {
                    printText("Registering...",false,true);
                    Game.register();
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
                    Game.getUsers();
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
                String msg = chatText.getText();
                //System.out.println("chattxt.msg->"+msg);
                chatText.setText("");
                for(Player p : Game.players){
                    try {
                        IPlayerServer ps = (IPlayerServer) Naming.lookup(p.address);
                        ps.recieveMessage(Game.myself.name, msg);
                    } catch (NotBoundException | MalformedURLException | RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });



        readyCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean state = readyCheckBox.isSelected();
                try {
                    Game.lobby.checkReady(Game.myself);
                    Game.myself.ready = state;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });



    }


    private void getUserList(){
        ArrayList<Player> users = new ArrayList<>();
        try {
            users = Game.lobby.getPlayers();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //update
        Game.players = users;
        textArea1.setMargin(new Insets(0,10,5,5));

        textArea1.setText("");
        for(Player p : users){
            textArea1.append("\n"+p.name + " - " + p.address);
        }

    }
    private void getChatMessages(){
        String msg = Game.myself.msgQueue.poll();
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

        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(info.getClassName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        frame = new JFrame("Lobby Server");
        frame.setContentPane(new lobbyGui().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.out.println("chiudo tutto");

                if(Game.lobby !=null){
                    try {

                        Game.lobby.unregister(Game.myself);
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
