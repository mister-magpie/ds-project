import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.Timer;

public class gameGui {
    JPanel panelMain;
    JTextField messageField;
    JButton sendButton;
    JButton rollButton;
    JLabel diceLabel;
    JPanel gamePanel;
    JTextPane listArea;
    JTextPane chatArea;
    JLayeredPane gameMap;
    static HashMap<String,JLabel> pieces;
    Game G;

    public gameGui(Game game){
        this.G = game;
        gamePanel.setLayout(null);
        gamePanel.setMinimumSize(new Dimension(800,600));
        gamePanel.setBounds(0,0,800,600);

        gameMap = new JLayeredPane();
        gameMap.setLayout(null);
        gameMap.setBounds(0,0,800,600);
        gameMap.setMinimumSize(new Dimension(800,600));

        gamePanel.add(gameMap);

        ImageIcon bg = new ImageIcon(gameMap.getClass().getResource("/snakesandladders.png"));
        JLabel bgLabel = new JLabel(bg);
        bgLabel.setBounds(0,0,800,600);
        gameMap.add(bgLabel,new Integer(0));

        chatArea.setMargin(new Insets(0,10,10,10));
        listArea.setMargin(new Insets(0,10,10,10));
        printText("Welcome to the game",true,true);
        pieces = initPieces();
        //updateList();

        //tira dado
        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int dice = new Random().nextInt(6) + 1;
                //chatArea.append("\n" + G.myself.name + " rolled a " + String.valueOf(dice));
                move(G.myself.idx, G.myself.updatePosition(dice));
                for (Player p : G.getPlayers()){
                    try {
                        Registry reg = LocateRegistry.getRegistry(p.address);
                        IPlayerServer ps = (IPlayerServer) reg.lookup(p.address+"/"+p.name);
                        ps.updatePosition(G.myself.idx,dice);
                    } catch (NotBoundException | RemoteException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    G.myself.setToken(false);
                    Player suc = G.myself.getSuccessor();
                    if (suc == null) System.out.println("nosuc");
                    Registry reg = LocateRegistry.getRegistry(suc.address);
                    IPlayerServer ps = (IPlayerServer) reg.lookup(suc.address+"/"+suc.name);
                    ps.makeTurn();

                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }

            }
        });
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String msg = messageField.getText();
                System.out.println("chattxt.msg->"+msg);
                messageField.setText("");
                for(Player p : G.players){
                    try {
                        Registry reg = LocateRegistry.getRegistry(p.address);
                        IPlayerServer ps = (IPlayerServer) reg.lookup(p.address+"/"+p.name);
                        ps.recieveMessage(G.myself.name, msg);
                    } catch (NotBoundException | RemoteException e) {
                        System.out.println(p.name + " not responding");
                        //e.printStackTrace();
                    }
                }
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String msg = messageField.getText();
                System.out.println("chattxt.msg->"+msg);
                messageField.setText("");
                for(Player p : G.players){
                    try {
                        Registry reg = LocateRegistry.getRegistry(p.address);
                        IPlayerServer ps = (IPlayerServer) reg.lookup(p.address+"/"+p.name);
                        ps.recieveMessage(G.myself.name, msg);
                    } catch (NotBoundException | RemoteException e) {
                        System.out.println(p.name + " not responding");
                        //e.printStackTrace();
                    }
                }
            }
        });
    }

    public HashMap<String, JLabel> initPieces(){
        System.out.println("init pieces");
        HashMap<String, JLabel> pcs = new HashMap<String, JLabel>();
        for(Player p : G.players){

            //System.out.println(p.name);

            ImageIcon icon = new ImageIcon(gameMap.getClass().getResource("/pawn"+p.idx+".png"));
            JLabel pwn = new JLabel(icon);
            pwn.setBounds(30,525,80,60);
            pwn.setText(p.name);

            gameMap.add(pwn,new Integer(1+p.idx));
            pcs.put(p.name,pwn);
        }
        //System.out.println(pcs.size());
        return pcs;
    }

    public void move(int i, int position){

        Point p = pieces.get(G.players.get(i).name).getLocation();
        System.out.println("new position is " + (position +1));

        int x = (position)%10;
        int y = (position)/10;

        p.y = 525 - 60*y;
        if(y%2 == 0) p.x = 20 + 80*x;
        else p.x = 740 - 80*x;

        if (position >= 100){
            G.players.get(i).setPosition(0);
            p.x = 30;
            p.y = 525;
        }
        //System.out.println(p.x + " " +p.y);
        pieces.get(G.players.get(i).name).setLocation(p);

    }

    private void updateUserList() {
        StyledDocument d = listArea.getStyledDocument();
        SimpleAttributeSet as = new SimpleAttributeSet();
        StyleConstants.setBold(as,true);
        listArea.setText("");
        try {
            d.insertString(0,"number of user: "+ G.players.size()+"\n---\n",as);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        for(Player p : G.players){
            try {
                Registry reg = LocateRegistry.getRegistry(p.address);
                IPlayerServer ps = (IPlayerServer) reg.lookup(p.address+"/"+p.name);
                ps.ping(G.myself.name);

                d.insertString(d.getLength(),p.name + ": ",as);
                d.insertString(d.getLength(),p.address + "\n",null);

                //t = t.concat("\n" +p.name + " - " + p.address);
            } catch (NotBoundException  e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                System.out.println(p.name + " offline");
                G.players.remove(p);
                //e.printStackTrace();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

    }

    private void getChatMessages(){
        String msg = G.myself.msgQueue.poll();
        if(msg != null){
            System.out.println("gettin' msg -> " + msg);
            printText(msg,false,false);
        }
    }

    public void updateGui() {
        java.util.Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run(){
                //getUserList(); //not needed as the list can be update when someone becomes unreachable. no new player will connect
                //update chat
                getChatMessages();
            }
        },0,300);
    }


    public void printText(String text,boolean append,boolean bold) {
        StyledDocument d = chatArea.getStyledDocument();
        //System.out.println("printtextcall");
        if(!append) text = "\n" + text;

        SimpleAttributeSet as = new SimpleAttributeSet();
        StyleConstants.setBold(as,true);

        try {
            if(!bold) d.insertString(d.getLength(),text,null);
            if(bold) d.insertString(d.getLength(),text,as);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        //chatArea.append(text);
    }

    public JFrame initializeGUI() {

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

        JFrame frame = new JFrame("Snake and Ladders");
        frame.setContentPane(this.panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        chatArea.setEditable(false);
        listArea.setEditable(false);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.out.println("chiudo tutto");


                super.windowClosing(windowEvent);
            }
        });
        updateUserList();
        updateGui();
        return frame;
    }

}

