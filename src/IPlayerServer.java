import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPlayerServer extends Remote{
    int ping(String name) throws RemoteException;
    //void updateLobbyList() throws RemoteException;
    void recieveMessage(String name, String msg) throws RemoteException;
    String getName() throws RemoteException;
}
