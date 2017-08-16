

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

public class Game {

    static ILobby lobby;
    static Player myself;


    public static void main(String[] args) {
        Boolean exit = false;
        Scanner scanner = new Scanner(System.in);
        if(args.length > 0){
            myself =  new Player(args[0]);
        }else{
            System.out.println(">>>What's your name?");
            myself = new Player(scanner.nextLine());
        }
        lobby = initializeLobby();
        ArrayList<Player> players;

        System.out.println(">>> Welcome, " + myself.name + "!");

        while(!exit){
            System.out.println(">>> What do you want to do?");
            String action = scanner.nextLine();

            switch (action){
                //send you information to the lobby
                case "register":
                    //myself.startServer(myself.name);
                    register();
                    break;

                //get all the users currently registered
                case "getusers":
                    players = getUsers();
                    for(Player p : players){
                        System.out.println(players.indexOf(p) +" - " + p.name);
                    }
                    break;

                case "ping":
                    players = getUsers();
                    System.out.println(">>> who do you want to ping?");
                    Player p = players.get(scanner.nextInt());

                    try {
                         IPlayerServer ps = (IPlayerServer) Naming.lookup("rmi:"+p.address+p.name);
                         if(ps.ping(myself.name)==1){
                             System.out.println("---ping succesful---");
                         }
                    } catch (NotBoundException | RemoteException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                    break;

                //close the client
                case "exit":
                    exit = true;
                    break;
                default:
                    System.out.println("retry");
            }
        }
    }
    private static ArrayList<Player> getUsers(){
        System.out.println("retrieving players list");
        try {
            return lobby.getPlayers();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void register(){
        try {
            myself.setIdx(lobby.register(myself));
            System.out.println("you have been added with index: " + String.valueOf(myself.idx));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private static ILobby initializeLobby(){
        System.out.println("Connecting to Lobby");
        try
        {
            return (ILobby) Naming.lookup ("rmi://localhost/LobbyServer");
        }
        catch(NotBoundException | MalformedURLException | RemoteException e)
        {
            e.printStackTrace( );
        }
        return null;
    }
}

