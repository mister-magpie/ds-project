import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
                //int dice = 99;
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
                //passa il turno
                try {
                    G.myself.setToken(false);
                    Player suc = G.myself.getSuccessor();
                    if (suc == null) System.out.println("nosuc");
                    Registry reg = LocateRegistry.getRegistry(suc.address);
                    IPlayerServer ps = (IPlayerServer) reg.lookup(suc.address+"/"+suc.name);
                    ps.makeTurn();
                    rollButton.setEnabled(false);
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
                rollButton.setEnabled(false);

                int position = G.myself.getPosition();

                if (position == 99)
                {
                    for(Player p : G.getPlayers())
                    {
                        if (p.idx != G.myself.idx)
                        {
                            try
                            {
                                Registry      reg = LocateRegistry.getRegistry(p.address);
                                IPlayerServer ps  = (IPlayerServer) reg.lookup(p.address + "/" + p.name);
                                ps.notifyWin(G.myself.idx);
                                //System.out.println("Ho terminato di notificare la mia vittoria");
                            }
                            catch (RemoteException | NotBoundException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                    printText("Congratulations " + G.myself.name + ", YOU WIN!", false,true);
                    JOptionPane.showMessageDialog(null, "Game Over!", "YOU WIN!", JOptionPane.NO_OPTION);
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

        messageField.getDocument().addDocumentListener(new DocumentListener()
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
                if (messageField.getText().isEmpty())
                {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent)
            {

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
            } catch (NotBoundException | BadLocationException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                System.out.println(p.name + " offline");
                G.players.remove(p);
                //e.printStackTrace();
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

    public void setRollButtonEnabled(boolean enabled)
    {
        rollButton.setEnabled(enabled);
    }

    public JFrame initializeGUI() {

        /*for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(info.getClassName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
*/        try {
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
        JFrame frame = new JFrame("Snake and Ladders " + "[" + G.myself.name + "]");
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
