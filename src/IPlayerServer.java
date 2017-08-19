import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IPlayerServer extends Remote{
    int ping(String name) throws RemoteException;
    //void updateLobbyList() throws RemoteException;
    void recieveMessage(String name, String msg) throws RemoteException;
    void startGame(ArrayList<Player> players, int i) throws RemoteException;
}