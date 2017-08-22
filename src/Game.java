import javax.swing.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Game extends UnicastRemoteObject implements IPlayerServer {

    ILobby lobby;
    Player myself;
    HashMap<String, Player> players;
    static lobbyGui lg;
    static gameGui gg;


    public Game() throws RemoteException {
        //lobby = connectToLobby();
        myself = new Player("anonymous");

    }


    public static void main(String[] args) throws RemoteException {
        Game game = new Game();
        lg = new lobbyGui(game);
        lg.initializeGUI(lg);
        //gg = new gameGui(game);
        //gg.initializeGUI();
    }


    public void bindServer() {
        try {
            IPlayerServer ps = (IPlayerServer) this;
            Naming.rebind(myself.address, ps);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Player> getPlayers() {
        return players;
    }


    public void getUsers() throws RemoteException {
        //System.out.println("retrieving players list");
        players = lobby.getPlayers();
    }


    public void register() throws RemoteException {
        int i = lobby.register(myself);
        if (i == -1) {
            myself.name = myself.name + String.valueOf(new Random().nextInt(100));
        } else {
            myself.setIdx(i);
            System.out.println("you have been added with index: " + String.valueOf(myself.idx));
        }
        bindServer();

    }

    public void connectToLobby() throws RemoteException {
        try {
            System.out.println("Connecting to Lobby");
            lobby = (ILobby) Naming.lookup("rmi://localhost/LobbyServer");
        } catch (NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public void initializeTable() {
        System.out.println("initialize table");
        //gg = new gameGui();
        //gg.initializeGUI();
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
    public void recieveMessage(String name, String msg) {
        myself.addMessage(name + ": " + msg);
        System.out.println("msg received\n" + name + ": " + msg + "size of queue " + myself.msgQueue.size());
        //gg.printText(name+": ",false,true);
        //gg.printText(msg,true,false);
    }


    @Override
    public void startGame(HashMap<String, Player> p) throws RemoteException {
        System.out.println("my definitive index is: " + p.get(myself.name).idx + ". was " + myself.idx);
        players = p;
        myself = players.get(myself.name);
        if (myself.idx == 0) {
            System.out.println("i'm the first! " + myself.name);
            myself.token = true;
        }
        //Game.lobby.unregister(players.get(i));
        initializeTable();

    }

    @Override
    public void updatePosition(String name, int o, int r) throws RemoteException {
        if (Objects.equals(name, myself.name)) {
            //System.out.println("it's me, i'm not updatin'");
            //System.out.println("rolled a " + r);
            players.get(name).updatePosition(r);
            gg.move(name, r);
        } else {
            System.out.println(myself.name);
            System.out.println("player " + name + " rolled a " + r + ". moves: " + o + "->" + (o + r));
            players.get(name).updatePosition(r);
            gg.chatArea.append("\n" + name + " rolled a " + String.valueOf(r));
            gg.move(name, r);
        }
    }

    public Player getMyself() {
        return myself;
    }
}