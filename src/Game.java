import javax.print.attribute.standard.MediaSize;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class Game extends UnicastRemoteObject implements IPlayerServer{

    static ILobby lobby;
    static Player myself;
    static ArrayList<Player> players;
    static lobbyGui lg;
    static gameGui gg;
    static JFrame lobbyView;
    static JFrame gameView;



    protected Game() throws RemoteException {
        super();
    }


    public static void main(String[] args) throws RemoteException {
        //if(args.length >= 1) System.setProperty("java.rmi.server.hostname",args[0]);
        //else System.setProperty("java.rmi.server.hostname","192.168.1.7");

        myself = new Player("anonymous");
        lg = new lobbyGui();
        lobbyView = lg.initializeGUI();
        //gg = new gameGui();
    }



     public static void bindServer(){
        try {
            IPlayerServer ps = new Game();
            System.out.println(myself.address+"/"+myself.name);
            Naming.rebind(myself.address+"/"+myself.name, ps);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    static public void getUsers() throws RemoteException {
        //System.out.println("retrieving players list");
        players = lobby.getPlayers();
    }


    static public void register() throws RemoteException {
        String a = lobby.register(myself);
        if(a ==null){
            myself.name = myself.name + String.valueOf(new Random().nextInt(100));
        }
        else {
            myself.address = a;
            System.out.println("you have been added with index: " + String.valueOf(myself.idx));
        }
        System.setProperty("java.rmi.server.hostname",a);
        bindServer();

    }
    static public void initializeLobby() throws RemoteException, NotBoundException, MalformedURLException {
        Registry reg = LocateRegistry.getRegistry("192.168.1.7");
        System.out.println("Connecting to Lobby");
        lobby =  (ILobby) reg.lookup ("192.168.1.7/LobbyServer");
    }

    static public void initializeTable(){
        System.out.println("initialize table");
        gg = new gameGui();
        gameView = gg.initializeGUI();
        lg.disposeGUI();

    }
    @Override
    public int ping(String name) throws RemoteException {
        try {
            String s = "ping from " + name + " " + getClientHost();
            //System.out.println(s);
            return 1;
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    public void recieveMessage(String name, String msg){
        System.out.println("msg received\n"+name +": " +msg+"size of queue " + myself.msgQueue.size());
        myself.msgQueue.add(name+": "+msg);
    }


    @Override
    public void startGame(ArrayList<Player> players, int i) throws RemoteException {
        System.out.println("my definitive index is: " + i +". was "+ myself.idx);
        Game.players = players;
        myself.setIdx(i);
        //Game.lobby.unregister(players.get(i));
        initializeTable();
        //gg.initPieces();
        //if(myself.idx == 0);

    }

    @Override
    public void updatePosition(int i, int r) throws RemoteException{
        if (i != myself.idx){
            System.out.println("player " + i + " rolled a " + r +". old position is: " + (players.get(0).getPosition() +1));
            //players.get(i).setPosition(r);
            gg.move(players.get(i),r);
        }
        gg.printText(players.get(i).name + " rolled a " + r,false,false);
    }
}

