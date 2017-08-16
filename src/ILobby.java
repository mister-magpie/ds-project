import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ILobby extends Remote
{
    int register(Player player) throws RemoteException;
    ArrayList<Player> getPlayers() throws RemoteException;
}

