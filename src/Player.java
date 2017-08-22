import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.Deque;



public class Player implements Serializable {
    String name;
    //Color color;
    boolean token;
    int idx;
    String address;
    boolean ready;
    Deque<String> msgQueue;
    private Player successor, predecessor;
    private int position;

    public Player(String name) throws RemoteException {
        super();
        this.name = name;
        this.token = false;
        this.address = "localhost";
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
    public void updatePosition(int roll){
        System.out.println(position);
        this.position += roll;
        System.out.println("update position " + position);

    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public boolean isToken() {
        return token;
    }

    public void setSuccessor(Player successor) {
        this.successor = successor;
    }

    public void setPredecessor(Player predecessor) {
        this.predecessor = predecessor;
    }

    public Player getSuccessor() {
        return successor;
    }

    public Player getPredecessor() {
        return predecessor;
    }

    public void setToken(boolean token) {
        this.token = token;
    }
}