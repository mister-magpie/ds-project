import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

public class lobbyGui {
    public JFrame frame;
    public JCheckBox readyCheckBox;
    public JTextField lobbyAddressTextField;
    public JButton sendButton;
    public JButton connectButton;
    public JTextField chatText;
    public JPanel panelMain;
    public JList list1;
    public JTextPane textPane1;
    public JTextField usernameField;
    public JTextArea textArea1;

    public Game G;

    public lobbyGui(Game game) {

        G = game;

        frame = new JFrame("Lobby Server");
        readyCheckBox.setEnabled(false);

        printText(
                "Welcome! This is the lobby client of " +
                "\n" +
                "Insert your username and the IP of the lobby server and then click Connect",true,true);


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage();
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

    public void sendMessage(){
        String msg = chatText.getText();
        chatText.setText("");
        for(Player p : G.players){
            if (p.name.equals(G.getMyself().name)){
                System.out.println(p.name + " diocano " + G.getMyself().name);
            }
            try {
                Registry reg = LocateRegistry.getRegistry(p.address);
                IPlayerServer ps = (IPlayerServer) reg.lookup(p.address+"/"+p.name);
                System.out.println("sendMessage: " + G.myself.name);
                ps.recieveMessage(G.getMyself().name, msg);
            } catch (NotBoundException | RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void getUserList(){
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
    public void getChatMessages(){
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


        frame.setContentPane(this.panelMain);
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
        if (frame == null) System.out.println(" no frame");
        frame.setVisible(false);

    }
}
