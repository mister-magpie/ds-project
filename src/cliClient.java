import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class cliClient {

    static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {
        boolean exit = false;
        Player me = null;
        ILobby lobby;

        while(!exit){
            System.out.println("\ninsert command");
            String cmd = scanner.next();
            //System.out.println(cmd);
            if (cmd.endsWith("create")){
                me = createPlayer();
            }
            if (cmd.equals("status")){
                System.out.println("i'm " + me.name);
            }
            if (cmd.equals("connect")){
                System.out.println("lobby address? ");
                String lobbyAddr = scanner.next();
                try {
                    Registry reg = LocateRegistry.getRegistry(lobbyAddr);
                    lobby =  (ILobby) reg.lookup (lobbyAddr+"/LobbyServer");
                    System.out.println("registering");
                    me.address = lobby.register(me);
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private static Player createPlayer() {
        System.out.println("name of player?");
        String name = scanner.next();
        return new Player(name);
    }




}
