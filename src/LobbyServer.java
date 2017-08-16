
import java.net.MalformedURLException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class LobbyServer extends UnicastRemoteObject implements ILobby {
    private ArrayList<Player> users;
    static String ADDRESS = "//192.168.1.7";

    private LobbyServer() throws RemoteException {
        this.users = new ArrayList<Player>();
    }

    @Override
    public int register(Player player) throws RemoteException {
        users.add(player);
        try {
            System.out.println("player " + player.name +" #"+ users.indexOf(player) + " connected!\nAddress: " + getClientHost());
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return users.indexOf(player);
    }

    @Override
    public ArrayList<Player> getPlayers() throws RemoteException {
        try {
            System.out.println("players list asked by: " + getClientHost());
            for(Player p : users){
                System.out.println(p.name + " " + users.indexOf(p));
            }
            return users;
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {
        //System.setProperty("java.rmi.server.hostname",ADDRESS);
        ADDRESS = "//localhost";
        try {
            ILobby server = new LobbyServer();
            System.out.println("Lobby Server is ONLINE!");
            Naming.rebind(ADDRESS + "/LobbyServer", server);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

