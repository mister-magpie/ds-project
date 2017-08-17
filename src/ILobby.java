import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ILobby extends Remote
{
    int register(String name, String playerAddress) throws RemoteException;
    void unregister(String playerIndex) throws RemoteException;
    void setReady(String addr,boolean state) throws RemoteException;
    ArrayList<String> getPlayers() throws RemoteException;
}

