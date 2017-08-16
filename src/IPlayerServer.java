import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPlayerServer extends Remote{
    int ping(String name) throws RemoteException;
}
