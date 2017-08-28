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
    ArrayList<Player> players;
    static lobbyGui lg;
    static gameGui gg;




    protected Game() throws RemoteException {
        players = new ArrayList<>();

    }


    public static void main(String[] args) throws RemoteException {
        Game G = new Game();
        myself = new Player("anonymous");
        lg = new lobbyGui(G);
        lg.initializeGUI();
        //gg = new gameGui();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public static void bindServer(){
         try {
             LocateRegistry.createRegistry(1099);
         } catch (RemoteException e) {
             //ge.printStackTrace();
         }
         try {
            Registry reg = LocateRegistry.getRegistry(myself.address);
            IPlayerServer ps =  new Game();
            //System.out.println(myself.address+"/"+myself.name);
            reg.rebind(myself.address+"/"+myself.name, ps);
        } catch (RemoteException  e) {
            System.out.println("cannot bind");
            e.printStackTrace();
        }
    }

     public void getUsers() throws RemoteException {
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
    public void initializeLobby(String lobbyAddr) throws RemoteException, NotBoundException, MalformedURLException {
        Registry reg = LocateRegistry.getRegistry(lobbyAddr);
        System.out.println("Connecting to Lobby");
        lobby =  (ILobby) reg.lookup (lobbyAddr+"/LobbyServer");
    }

    public void initializeTable(){
        System.out.println("initialize table");
        gg = new gameGui(this);
        gg.initializeGUI();

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
        //System.out.println("msg received\n"+name +": " +msg+"size of queue " + myself.msgQueue.size());
        myself.msgQueue.add(name+": "+msg);
    }


    @Override
    public void startGame(ArrayList<Player> p, int i) throws RemoteException {
        //System.out.println("my definitive index is: " + i +". was "+ myself.idx);
        players = p;
        myself.setIdx(i);
        //Game.lobby.unregister(players.get(i));
        initializeTable();
        //gg.initPieces();
        if(i == 0){
            System.out.println("i'm first");
            Game.myself.setToken(true);
            gg.rollButton.setEnabled(true);
        }else{
            System.out.println("not my turn");
            gg.rollButton.setEnabled(false);
        }
        int pred = (i + players.size() - 1)%players.size();
        System.out.println("pred is:" + pred);
        Game.myself.setPredecessor( players.get(pred));
        Game.myself.setSuccessor(players.get((i+1)%players.size()));
        gg.printText("it's " +players.get(0).name +"'s turn.",false,true);
    }

    @Override
    public void makeTurn() throws RemoteException {
        gg.printText("it's " + Game.myself.name+"'s turn.",false,true);
        gg.rollButton.setEnabled(true);
        Game.myself.setToken(true);
    }

    @Override
    public void updatePosition(int i, int r) throws RemoteException{
        if (i != myself.idx){
            System.out.println("player " + i + " rolled a " + r +".\nold position is: " + (players.get(i).getPosition() +1));
            players.get(i).updatePosition(r);
            System.out.println("new position: "+ players.get(i).getPosition());
            gg.move(i,players.get(i).getPosition());
        }
        gg.printText(players.get(i).name + " rolled a " + r,false,false);
    }
}

