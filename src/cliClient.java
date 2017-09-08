import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

public class cliClient extends UnicastRemoteObject implements IPlayerServer {

    static Player me = null;
    static ArrayList<Player> players = null;
    static ILobby lobby = null;
    static Scanner scanner = new Scanner(System.in);

    protected cliClient() throws RemoteException {

    }


    public static void main(String[] args) {

        boolean exit = false;
        while(!exit){
            System.out.println("\ninsert command");
            String cmd = scanner.next();
            //System.out.println(cmd);
            switch (cmd) {
                case "create":
                    me = createPlayer();
                    me.address = args[0];
                    bindServer(me);
                    break;

                case "status":
                    System.out.println("NAME\tADDRES\n" + me.name + "\t" + me.address);
                    break;

                case "connect":
                    System.out.println("lobby address? ");
                    String lobbyAddr = scanner.next();
                    try {
                        Registry reg = LocateRegistry.getRegistry(lobbyAddr);
                        lobby = (ILobby) reg.lookup(lobbyAddr + "/LobbyServer");
                        System.out.println("registering");
                        me.address = lobby.register(me);
                        System.out.print("...my address is " + me.address);

                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case "get_players":
                    try {
                        players = lobby.getPlayers();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    System.out.println("NAME\tADDRESS");
                    for (Player p : players) {
                        System.out.println(p.name + "\t" + p.address);
                    }
                    break;
                case "exit":
                    try {
                        lobby.unregister(me);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                    break;
                case "send":
                    System.out.println("write your message");
                    String msg = scanner.nextLine();
                    sendMsg(msg, players);
                    break;
            }
        }

    }

    private static void sendMsg(String msg, ArrayList<Player> players) {
        for (Player p : players) {
            try {
                Registry reg = LocateRegistry.getRegistry(p.address);
                IPlayerServer ps = (IPlayerServer) reg.lookup(p.address + "/" + p.name);
                ps.recieveMessage(Game.myself.name, msg);
            } catch (NotBoundException | RemoteException e) {
                System.out.println(p.name + " not responding");
                //e.printStackTrace();
            }
        }
    }

    private static Player createPlayer() {
        System.out.println("name of player?");
        String name = scanner.next();
        return new Player(name);
    }

    private static void bindServer(Player myself) {
        System.out.println("binding player server");
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            //ge.printStackTrace();
            System.out.println("rmi registry already created");
        }
        try {
            Registry reg = LocateRegistry.getRegistry(myself.address);
            IPlayerServer ps = new Game();
            //System.out.println(myself.address+"/"+myself.name);
            reg.rebind(myself.address + "/" + myself.name, ps);
        } catch (RemoteException e) {
            System.out.println("cannot bind");
            e.printStackTrace();
        }
        System.out.println("bound");
    }


    @Override
    public int ping(String name) throws RemoteException, ServerNotActiveException {
        System.out.println("received ping from " + getClientHost());
        return 0;
    }

    @Override
    public void recieveMessage(String name, String msg) throws RemoteException {
        System.out.println(name + ": " + msg);
    }

    @Override
    public void startGame(ArrayList<Player> plrs, int i) throws RemoteException {
        players = plrs;
        System.out.println("Starting the game ");
    }

    @Override
    public void updatePosition(int playerIndex, int roll) throws RemoteException {
        players.get(playerIndex).updatePosition(roll);
    }

    @Override
    public void makeTurn() throws RemoteException {
        me.token = true;
    }

    @Override
    public void notifyTurn(Player player) throws RemoteException {

    }

    @Override
    public void notifyWin(int playerIndex) throws RemoteException {

    }
}

