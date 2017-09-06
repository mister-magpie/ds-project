import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

public class cliClient {

    static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {
        boolean exit = false;
        Player me = null;
        ArrayList<Player> players = null;
        ILobby lobby = null;

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


}
