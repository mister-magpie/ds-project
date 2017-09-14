import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class LobbyServer extends UnicastRemoteObject implements ILobby {
    private Map<String, Player> users;
    //static String ADDRESS = "25.72.70.109";
    //static String ADDRESS = "192.168.1.7";
    //static String ADDRESS = "127.0.0.1";
    //static String ADDRESS = "127.0.0.1";


    private LobbyServer() throws RemoteException {
        this.users = Collections.synchronizedMap(new HashMap<String, Player>());
        pingUsers();
    }

    public static void main(String[] args) {
        String ADDRESS = "127.0.0.1";
        if (args.length > 0) ADDRESS = args[0];
        System.setProperty("java.rmi.server.hostname", ADDRESS);
        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            ILobby server = new LobbyServer();
            System.out.println("Lobby Server is ONLINE on " + ADDRESS + "\n");
            reg.rebind(ADDRESS + "/LobbyServer", server);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregister(Player player) throws RemoteException{
        System.out.println("player #" + player.idx + " disconnected");
        users.remove(player.name);
    }

    @Override
    public void checkReady(Player player) throws RemoteException {
        boolean startGame =  true;
        System.out.println(player.name + " ready = " +player.ready);
        users.get(player.name).ready = player.ready;
        for(Player p : users.values()){
            startGame = startGame && p.ready;
            System.out.println(p.name+": "+ p.ready +" AND "+ startGame + " = " + startGame);
        }

        if (startGame == true && users.size() >= 2)
        {
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


            // Notify because timer needs to start from the beginning of the game.
            for (Player pToNotify : players)
            {
                try
                {
                    Registry      reg = LocateRegistry.getRegistry(pToNotify.address);
                    IPlayerServer ps  = (IPlayerServer) reg.lookup(pToNotify.address + "/" + pToNotify.name);
                    ps.notifyTurn(players.get(0));
                }
                catch (RemoteException e)
                {
                    System.out.println("\nHo lanciato " + e.getMessage() + "\n perchè non ho potuto notificare il turno a " + pToNotify.name + "\n");
                }
                catch (NotBoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public synchronized ArrayList<Player> getPlayers() {
        try {
            System.out.println("getPlayers call from " +  getClientHost());
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        checkCrashes();
        return  new ArrayList<Player>(users.values());
    }

    @Override
    public String register(Player player) throws RemoteException {
        try {
            System.out.println("\nplayer " + player.name + " #" + player.idx + " connected!\nAddress: " + getClientHost());
            if (users.containsKey(player.name)) return null;//if player of the same name is present prompt a change;
            player.idx = users.size();
            player.address = getClientHost();
            users.put(player.name, player);
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        return player.address;
    }

    public synchronized void pingUsers() {
        java.util.Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run(){
                if (!users.isEmpty()) {
                    //checkCrashes();
                }
            }
        }, 0, 5000);
    }

    public void checkCrashes() {
        System.out.print("\rchecking if anyone is crashed...");
        synchronized (users) {
            Iterator<Player> iterator = users.values().iterator();
            while (iterator.hasNext()) {
                Player u = iterator.next();
                try {
                    Registry reg = LocateRegistry.getRegistry(u.address);
                    IPlayerServer ps = (IPlayerServer) reg.lookup(u.address + "/" + u.name);
                    ps.ping("lobbyserver");
                } catch (NotBoundException e) {
                    System.out.println("not bound!");
                    users.remove(u.name);
                    //e.printStackTrace();
                } catch (RemoteException e) {
                    System.out.println(u.name + " not responding!");
                    users.remove(u.name);
                    //e.printStackTrace();
                } catch (ServerNotActiveException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
