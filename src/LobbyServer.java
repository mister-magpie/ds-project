import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class LobbyServer extends UnicastRemoteObject implements ILobby {
    private ArrayList<Player> users;
    private ArrayList<Boolean> readyState;
    //static String ADDRESS = "//192.168.1.7";

    private LobbyServer() throws RemoteException {
        this.users = new ArrayList<>();
        this.readyState = new ArrayList<>();
    }

    @Override
    public int register(Player player) throws RemoteException {
        if(users.contains(player)) return -1;//if player of the same name is present prompt a change;
        player.idx = users.size();
        users.add(player);

        //readyState.add(users.indexOf(player),false);
        try {
            System.out.println("player " + player.name +" #"+ users.indexOf(player) + " connected!\nAddress: " + getClientHost());
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return users.indexOf(player);
    }

    @Override
    public void unregister(Player player) throws RemoteException{
        System.out.println("player #" + player.idx + " disconnected");
        users.remove(player.idx);
    }

    @Override
    public void checkReady(Player player) throws RemoteException {
        boolean startGame =  true;
        //users.get(users.indexOf(player)).ready = true;
        users.get(player.idx).ready = true;
        for(Player p : users){
            startGame = startGame && p.ready;
            System.out.println(p.ready +" AND "+ startGame + " = " + startGame);
        }

        if (startGame == true){
            System.out.println("all ready");
            //mischia i giocatori e manda il segnale di inizio!
            for(Player p : users) {
                try {
                    System.out.println("calling startgame on " + users.indexOf(p));
                    IPlayerServer ps = (IPlayerServer) Naming.lookup(p.address);
                    ps.startGame(users, users.indexOf(p));
                } catch (NotBoundException | MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ArrayList<Player> getPlayers() {

        for(Player p : users){
            try {
                IPlayerServer ps;
                try {
                    ps = (IPlayerServer) Naming.lookup(p.address);
                    ps.ping("lobbyserver");
                } catch (RemoteException e) {
                    users.remove(p);
                    e.printStackTrace();
                }
            } catch (NotBoundException | MalformedURLException e) {
                e.printStackTrace();
            }
            //System.out.println(p.name + " " + users.indexOf(p) + " " + readyState.get(users.indexOf(p)) + " ");
        }

        return users;
    }


    public static void main(String[] args) {
        //System.setProperty("java.rmi.server.hostname",ADDRESS);
        String ADDRESS = "//localhost";
        try {
            ILobby server = new LobbyServer();
            System.out.println("Lobby Server is ONLINE!");
            Naming.rebind(ADDRESS + "/LobbyServer", server);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

