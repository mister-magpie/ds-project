import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IPlayerServer extends Remote{
    int ping(String name) throws RemoteException;
    void recieveMessage(String name, String msg) throws RemoteException;
    void startGame(ArrayList<Player> players, int i) throws RemoteException;
    void updatePosition(int playerIndex, int roll) throws RemoteException;
    void makeTurn() throws RemoteException;
    void notifyTurn(String name) throws RemoteException;
    void notifyWin(int playerIndex) throws RemoteException;
}
