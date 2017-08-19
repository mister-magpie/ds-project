import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

public class gameGui {
    private JPanel panelMain;
    private JTextField messageField;
    private JButton sendButton;
    private JTextArea textArea1;
    private JList list1;
    private JLabel bgLabel;
    private JButton rollButton;
    private JLabel diceLabel;
    private JPanel gamePanel;
    private JLayeredPane gameMap;

    ArrayList<JLabel> pieces;

    public gameGui(){
        gamePanel.setLayout(null);
        gamePanel.setMinimumSize(new Dimension(800,600));
        gamePanel.setBounds(0,0,800,597);

        gameMap = new JLayeredPane();
        gameMap.setLayout(null);
        gameMap.setBounds(0,0,800,597);
        gameMap.setMinimumSize(new Dimension(800,597));

        gamePanel.add(gameMap);

        ImageIcon bg = new ImageIcon(gameMap.getClass().getResource("/snakesandladders.png"));
        bgLabel = new JLabel(bg);
        bgLabel.setBounds(0,0,800,597);
        gameMap.add(bgLabel,new Integer(0));


        ImageIcon pwn = new ImageIcon(gameMap.getClass().getResource("/pawn.png"));
        JLabel lab = new JLabel(pwn);
        lab.setBounds(30,525,37,50);
        gameMap.add(lab,new Integer(1));

        pieces = new ArrayList<>();
        pieces.add(lab);


        //tira dado
        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int dice = new Random().nextInt(6) + 1;
                diceLabel.setText(String.valueOf(dice));
                move(0,dice);
            }
        });
    }
    public void move(int player,int roll){
        Point p = pieces.get(0).getLocation();
        int position = Game.players.get(0).position;
        System.out.println(roll + " " + (position+roll));
        Game.players.get(0).position += roll;
        System.out.println((int) Math.floor((position+roll)/10));
        p.y = 525 - ((position+roll)/10) * 60;
        if ( (position+roll/10)%2 != 0){
            p.x = 30 + ((position+roll)%10)*80;
        }else {
            p.x = 770 - ((position+roll)%10)*80;
        }
        System.out.println( ((position+roll)%10) +" "+ ((position+roll)/10)%2);

        if (position >= 100){
            position = 0;
            p.x = 30;
            p.y = 525;
        }
        pieces.get(0).setLocation(p);

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

        JFrame frame = new JFrame("Lobby Server");
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
        return frame;
    }

}

