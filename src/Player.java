import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;


public class Player implements Serializable {
    String name;
    String color;
    boolean token;
    int idx;
    String address;

    public Player(String name){
        this.name = name;
        this.token = false;
        this.address = "//localhost/";
        startServer();
    }

    public void startServer() {
        IPlayerServer endpoint = null;
        try {
            endpoint = new PlayerServer();
            Naming.rebind( this.address + this.name, endpoint);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }
}