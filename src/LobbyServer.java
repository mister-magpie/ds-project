import com.sun.jndi.rmi.registry.RegistryContext;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class LobbyServer extends UnicastRemoteObject implements ILobby {
    private HashMap<String,Player> users;
    //static String ADDRESS = "25.72.70.109";
    static String ADDRESS = "192.168.1.7";


    private LobbyServer() throws RemoteException {
        this.users = new HashMap<>();
        pingUsers();
    }

    @Override
    public String register(Player player) throws RemoteException {


        try {
            System.out.println("player " + player.name +" #"+ player.idx + " connected!\nAddress: " + getClientHost());
            if(users.containsKey(player.name)) return null;//if player of the same name is present prompt a change;
            player.idx = users.size();
            player.address = getClientHost();
            users.put(player.name,player);
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        return player.address;
    }

    @Override
    public void unregister(Player player) throws RemoteException{
        System.out.println("player #" + player.idx + " disconnected");
        users.remove(player.name);
    }

    @Override
    public void checkReady(Player player) throws RemoteException {
        boolean startGame =  true;
        //users.get(users.indexOf(player)).ready = true;
        users.get(player.name).ready = true;
        for(Player p : users.values()){
            startGame = startGame && p.ready;
            System.out.println(p.name+": "+ p.ready +" AND "+ startGame + " = " + startGame);
        }

        if (startGame == true && users.size()>=2){
            System.out.println("all ready");
            //mischia i giocatori e manda il segnale di inizio!
            ArrayList<Player> players = new ArrayList<>();
            int c = 0;
            for (Player u : users.values()){
                players.add(c,u);
                System.out.println("player: "+ u.name + " is player #"+c);
                c++;
            }

            for(Player p : players) {
                try {
                    System.out.println("calling startgame on " + p.idx);
                    Registry reg = LocateRegistry.getRegistry(p.address);
                    IPlayerServer ps = (IPlayerServer) reg.lookup(p.address+"/"+p.name);
                    ps.startGame(players, players.indexOf(p));
                } catch (NotBoundException  e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public synchronized ArrayList<Player> getPlayers() {

        for(Player p : users.values()){
                try {
                    Registry reg = LocateRegistry.getRegistry(p.address);
                    IPlayerServer ps = (IPlayerServer) reg.lookup(p.address+"/"+p.name);
                    ps.ping("lobbyserver");
                } catch (RemoteException e) {
                    System.out.println(p.name + " not responding!");
                    users.remove(p.name);
                    //e.printStackTrace();
                }catch (NotBoundException e) {
                    System.out.println("not bound!");
                    //e.printStackTrace();
                }
            //System.out.println(p.name + " " + users.indexOf(p) + " " + readyState.get(users.indexOf(p)) + " ");
        }

        return  new ArrayList(users.values());
    }

    public synchronized void pingUsers() {
        java.util.Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run(){
                for(Player u : users.values()){
                    IPlayerServer ps = null;
                    try {
                        Registry reg = LocateRegistry.getRegistry(u.address);
                        ps = (IPlayerServer) reg.lookup(u.address+"/"+u.name);
                        ps.ping("lobbyserver");
                    } catch (NotBoundException e) {
                        System.out.println("no bound!");
                        users.remove(u.name);
                        //e.printStackTrace();
                    } catch (RemoteException e) {
                        System.out.println(u.name + " not responding!");
                        users.remove(u.name);
                        //e.printStackTrace();
                    }

                }
            }
        },0,1000);
    }


    public static void main(String[] args) {
        System.setProperty("java.rmi.server.hostname",ADDRESS);
        //String ADDRESS = "//localhost";
        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            ILobby server = new LobbyServer();
            System.out.println("Lobby Server is ONLINE on " + ADDRESS);
            reg.rebind(ADDRESS + "/LobbyServer", server);
        } catch (RemoteException  e) {
            e.printStackTrace();
        }
    }
}

