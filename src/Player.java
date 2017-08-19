import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;



public class Player implements Serializable {
    String name;
    //Color color;
    boolean token;
    int idx;
    String address;
    boolean ready;
    Deque<String> msgQueue;
    Player successor, predecessor;
    int position;

    public Player(String name) throws RemoteException {
        super();
        this.name = name;
        this.token = false;
        this.address = "//localhost/"+name;
        this.msgQueue = new ArrayDeque<String>(10);
        this.ready = false;
        this.position = 0;
    }


    public void setIdx(int idx) {
        this.idx = idx;
    }
    public void setUsername(String name){
        this.name = name;
        this.address = "localhost/"+name;
    }
    public void setPosition(int pos){
        this.position += pos;
    }


}