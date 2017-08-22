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
    ArrayDeque<String> msgQueue;
    Player successor, predecessor;
    int position;

    public Player(String name) throws RemoteException {
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
        System.out.println("set username " + name);
        this.name = name;
        this.address = "localhost/"+name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void updatePosition(int roll){
        System.out.println("updateposition: "+ (position+1) +" "+(position+roll+1));
        int p = position+roll;
        position = p;
        System.out.println("new position: " + (position +1));
    }
    public ArrayDeque<String> getMessageQueue(){
        System.out.println("get msg q");
        return msgQueue;
    }
    public String getLastMessage(){
        return msgQueue.poll();
    }
    public void addMessage(String msg){
        msgQueue.add(msg);
    }

}