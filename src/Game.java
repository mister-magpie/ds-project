import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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

        myself = new Player("anonymous");
        lg = new lobbyGui();
        lobbyView = lg.initializeGUI();
        //gg = new gameGui();
    }



     public static void bindServer(){
        try {
            IPlayerServer ps = new Game();
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
        bindServer();

    }
    static public void initializeLobby() throws RemoteException, NotBoundException, MalformedURLException {
        System.out.println("Connecting to Lobby");
        lobby =  (ILobby) Naming.lookup ("rmi://localhost/LobbyServer");
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
        System.out.println("player " + i + " rolled a " + r +". old position is: " + (players.get(0).position +1));
        //players.get(i).setPosition(r);
        gg.move(players.get(i),r);
    }
}

