import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

public class lobbyGui {

    private JCheckBox readyCheckBox;
    private JTextField lobbyAddressTextField;
    private JButton sendButton;
    private JButton connectButton;
    private JTextField chatText;
    private JPanel panelMain;
    private JList list1;
    private JTextPane textPane1;
    private JTextField usernameField;

    public lobbyGui() {

        readyCheckBox.setEnabled(false);

        printText(
                "Welcome! This is the lobby client." +
                "\n" +
                "Insert your username and the IP of the lobby server and then click Connect",true);


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String msg = chatText.getText();
                chatText.setText("");
                for(String pa : Game.playersAddress){
                    try {
                        IPlayerServer ps = (IPlayerServer) Naming.lookup(pa);
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
                Game.createPlayer(username);
                //connectiong phase
                try {
                    printText("Connecting...",false);
                    String lobbyAddr = lobbyAddressTextField.getText();
                    Game.initializeLobby();
                } catch (RemoteException | NotBoundException | MalformedURLException e) {
                    e.printStackTrace();
                }
                printText("ok",true);

                //registering phase
                try {
                    printText("Registering...",false);
                    Game.register();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                printText("ok",true);
                usernameField.setEditable(false);
                connectButton.setEnabled(false);
                //
                readyCheckBox.setEnabled(true);
                printText("when you are ready to start check the box",false);

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
                for(String pa : Game.playersAddress){
                    try {
                        IPlayerServer ps = (IPlayerServer) Naming.lookup(pa);
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
                    Game.lobby.setReady(Game.myself.address,state);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });



    }


    private void getUserList(){
        ArrayList<String> users = new ArrayList<>();
        try {
            users = Game.lobby.getPlayers();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        DefaultListModel lm = new DefaultListModel();
        for(String pa : users){
            lm.addElement(pa);
        }
        list1.setModel(lm);

    }
    private void getChatMessages(){
        String msg = Game.myself.msgQueue.poll();
        if(msg != null){
            printText(msg,false);
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


    public void printText(String text,boolean append) {
        StyledDocument d = textPane1.getStyledDocument();
        SimpleAttributeSet as = new SimpleAttributeSet();
        StyleConstants.setBold(as,true);
        try {
            if(!append) d.insertString(d.getLength(),"\n" + text,as);
            if(append) d.insertString(d.getLength(),text,null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void initializeGUI(){

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

        JFrame frame = new JFrame("Lobby Server");
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

                        Game.lobby.unregister(Game.myself.address);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                super.windowClosing(windowEvent);
            }
        });

    }
}
