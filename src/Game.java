import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

public class Game {

    static IHelloWorld lobby;
    static Player myself;


    public static void main(String[] args) {
        Boolean exit = false;
        Scanner scanner = new Scanner(System.in);
        if(args[0]!= null){
            myself =  new Player(args[0]);
        }else{
            myself= new Player("anonymous");
        }
        lobby = initializeLobby();
        ArrayList<Player> players;

        System.out.println("Welcome, " + myself.name + "!");

        while(!exit){
            System.out.println("What do you want to do?");
            String action = scanner.nextLine();

            switch (action){
                //send you information to the lobby
                case "register":
                    register();
                    break;
                //get all the users currently registered
                case "getusers":
                    players = getUsers();
                    for(Player p : players){
                        System.out.println(players.indexOf(p) +" - " + p.name);
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
    private static IHelloWorld initializeLobby(){
        System.out.println("Connecting to Lobby");
        try
        {
            return (IHelloWorld) Naming.lookup ("rmi://localhost/LobbyServer");
        }
        catch(NotBoundException | MalformedURLException | RemoteException e)
        {
            e.printStackTrace( );
        }
        return null;
    }
}

