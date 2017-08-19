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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Timer;

public class gameGui {
    JPanel panelMain;
    JTextField messageField;
    JButton sendButton;
    JButton rollButton;
    JLabel diceLabel;
    JPanel gamePanel;
    JTextArea listArea;
    JTextArea chatArea;
    JLayeredPane gameMap;
    static HashMap<String,JLabel> pieces;


    public gameGui(){
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
        updateList();

        //tira dado
        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int dice = new Random().nextInt(6) + 1;
                chatArea.append("\n" + Game.myself.name + " rolled a " + String.valueOf(dice));
                //move(Game.myself.idx,dice);
                for (Player p : Game.players){
                    try {
                        IPlayerServer ps = (IPlayerServer) Naming.lookup(p.address);
                        ps.updatePosition(Game.myself.idx,dice);
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String msg = messageField.getText();
                System.out.println("chattxt.msg->"+msg);
                messageField.setText("");
                for(Player p : Game.players){
                    try {
                        IPlayerServer ps = (IPlayerServer) Naming.lookup(p.address);
                        ps.recieveMessage(Game.myself.name, msg);
                    } catch (NotBoundException | MalformedURLException | RemoteException e) {
                        System.out.println(p.name + " not responding");
                        //e.printStackTrace();
                    }
                }
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

            }
        });
    }

    public HashMap<String, JLabel> initPieces(){
        System.out.println("init pieces");
        HashMap<String, JLabel> pcs = new HashMap<String, JLabel>();
        for(Player p : Game.players){

            System.out.println(p.name);

            ImageIcon icon = new ImageIcon(gameMap.getClass().getResource("/pawn"+p.idx+".png"));
            JLabel pwn = new JLabel(icon);
            pwn.setBounds(30,525,80,60);
            pwn.setText(p.name);

            gameMap.add(pwn,new Integer(1+p.idx));
            pcs.put(p.name,pwn);
        }
        System.out.println(pcs.size());
        return pcs;
    }

    public static void move(Player player,int roll){

        Point p = pieces.get(player.name).getLocation();
        int position = player.position;
        player.setPosition(roll);
        System.out.println("new position is " + (player.position +1));

        int x = (roll+position)%10;
        int y = (position+roll)/10;

        p.y = 525 - 60*y;
        if(y%2 == 0) p.x = 20 + 80*x;
        else p.x = 740 - 80*x;

        if (position + roll >= 100){
            player.position = 0;
            p.x = 30;
            p.y = 525;
        }
        System.out.println(p.x + " " +p.y);
        pieces.get(player.name).setLocation(p);

    }

    private void getUserList() {
        String t = "number of user: "+ Game.players.size()+"\n";
        for(Player p : Game.players){
            try {
                IPlayerServer ps = (IPlayerServer) Naming.lookup(p.address);
                ps.ping(Game.myself.name);
                t = t.concat("\n" +p.name + " - " + p.address);
            } catch (NotBoundException | MalformedURLException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                System.out.println(p.name + " offline");
                Game.players.remove(p);
                //e.printStackTrace();
            }
        }
        listArea.setText(t);
    }

    private void getChatMessages(){
        String msg = Game.myself.msgQueue.poll();
        if(msg != null){
            System.out.println("gettin' msg -> " + msg);
            chatArea.append("\n" + msg);
        }
    }

    public void updateList() {
        java.util.Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run(){
                getUserList(); //not needed as the list can be update when someone becomes unreachable. no new player will connect
                //update chat
                getChatMessages();
            }
        },0,100);
    }


    public void printText(String text,boolean append,boolean bold) {
        System.out.println("printtextcall");
        chatArea.append(text);
        /*StyledDocument d = chatArea.getStyledDocument();

        if(!append) text = "\n" + text;

        SimpleAttributeSet as = new SimpleAttributeSet();
        StyleConstants.setBold(as,true);

        try {
            if(!bold) d.insertString(d.getLength(),text,null);
            if(bold) d.insertString(d.getLength(),text,as);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }*/
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
        frame.setContentPane(new gameGui().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.out.println("chiudo tutto");


                super.windowClosing(windowEvent);
            }
        });

        updateList();
        return frame;
    }

}

