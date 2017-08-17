import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;



public class Player extends UnicastRemoteObject implements IPlayerServer {
    String name;
    //Color color;
    boolean token;
    int idx;
    String address;
    Deque<String> msgQueue;
    Player successor, predecessor;

    public Player(String name) throws RemoteException {
        super();
        this.name = name;
        this.token = false;
        this.address = "//localhost/"+name;
        this.msgQueue = new ArrayDeque<String>(10);
    }


    public void setIdx(int idx) {
        this.idx = idx;
    }

    @Override
    public int ping(String name) throws RemoteException {
        try {
            System.out.println("ping from " + name + " " + getClientHost());
            return 1;
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    public void recieveMessage(String name, String msg){
        //System.out.println("playerserver recieve->"+msg);
        msgQueue.add(name+": "+msg);
    }

    @Override
    public String getName() throws RemoteException {
        return this.name;
    }

}