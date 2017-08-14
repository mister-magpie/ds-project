import java.net.MalformedURLException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class LobbyServer extends UnicastRemoteObject implements IHelloWorld {
    private ArrayList<Player> users;

    private LobbyServer() throws RemoteException {
        this.users = new ArrayList<Player>();
    }

    @Override
    public int register(Player player) throws RemoteException {
        users.add(player);
        try {
            System.out.println(users.indexOf(player) + " player " + player.name + " connected!\n Address: " + getClientHost());
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
        try {
            IHelloWorld server = new LobbyServer();
            System.out.println("Lobby Server is ONLINE!");
            Naming.rebind("//localhost/LobbyServer", server);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

