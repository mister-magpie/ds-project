import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface IPlayerServer extends Remote{
    int ping(String name) throws RemoteException;
    //void updateLobbyList() throws RemoteException;
    void recieveMessage(String name, String msg) throws RemoteException;
    void startGame(HashMap<String,Player> players) throws RemoteException;
    void updatePosition(String name, int oldpos, int roll) throws RemoteException;
}
