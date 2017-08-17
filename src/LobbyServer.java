
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class LobbyServer extends UnicastRemoteObject implements ILobby {
    private ArrayList<String> users;
    private ArrayList<Boolean> readyState;
    static String ADDRESS = "//192.168.1.7";

    private LobbyServer() throws RemoteException {
        this.users = new ArrayList<String>();
        this.readyState = new ArrayList<Boolean>();
    }

    @Override
    public int register(String name, String playerAddress) throws RemoteException {
        users.add(playerAddress);
        try {
            System.out.println("player " + name +" #"+ users.indexOf(playerAddress) + " connected!\nAddress: " + getClientHost());
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return users.indexOf(playerAddress);
    }

    @Override
    public void unregister(String address) throws RemoteException{
        users.remove(address);
        System.out.println("player #" + users.indexOf(address) + " disconnected");
    }

    @Override
    public void setReady(String addr,boolean state) throws RemoteException {
        int i = users.indexOf(addr);
        readyState.add(i,state);
        boolean startGame =  true;
        for(boolean b :readyState){
            startGame = startGame && b;
        }
        if (startGame) System.out.println("all ready");
        //mischia i giocatori e manda il segnale di inizio!
    }

    @Override
    public ArrayList<String> getPlayers() {

        for(String addr : users){
            try {
                IPlayerServer ps = null;
                try {
                    ps = (IPlayerServer) Naming.lookup(addr);
                    ps.ping("lobbyserver");
                } catch (RemoteException e) {
                    users.remove(addr);
                    e.printStackTrace();
                }
            } catch (NotBoundException | MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return users;
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

