import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Game {

    static ILobby lobby;
    static Player myself;
    static ArrayList<String> playersAddress;
    static lobbyGui gui;


    public static void main(String[] args) {
        gui = new lobbyGui();
        gui.initializeGUI();
    }


    static public void createPlayer(String name){
        try {
            myself = new Player(name);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            Naming.rebind(myself.address, myself);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    static public void getUsers() throws RemoteException {
        //System.out.println("retrieving players list");
        playersAddress = lobby.getPlayers();
    }


    static public void register() throws RemoteException {
        myself.setIdx(lobby.register(myself.name, myself.address));
        System.out.println("you have been added with index: " + String.valueOf(myself.idx));

    }
    static public void initializeLobby() throws RemoteException, NotBoundException, MalformedURLException {
        System.out.println("Connecting to Lobby");
        lobby =  (ILobby) Naming.lookup ("rmi://localhost/LobbyServer");
    }

}

