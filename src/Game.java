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
        gg = new gameGui();
    }



     public static void bindServer(){
        try {
            IPlayerServer ps = new Game();
            Naming.rebind(myself.address, ps);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    static public void getUsers() throws RemoteException {
        //System.out.println("retrieving players list");
        players = lobby.getPlayers();
    }


    static public void register() throws RemoteException {
        int i = lobby.register(myself);
        if(i == -1){
            myself.name = myself.name + String.valueOf(new Random().nextInt(100));
        }
        else {
            myself.setIdx(i);
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
        gameGui gamegui = new gameGui();
        gameView = gamegui.initializeGUI();
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
        myself.msgQueue.add(name+": "+msg);
    }


    @Override
    public void startGame(ArrayList<Player> players, int i) throws RemoteException {
        System.out.println("my definitive index is: " + i +". was "+ myself.idx);
        myself.setIdx(i);
        Game.lobby.unregister(players.get(i));
        initializeTable();
    }
}

