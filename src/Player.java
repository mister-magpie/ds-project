import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Player implements Serializable {
    String name;
    String color;
    boolean token;
    int idx;

    public Player(String name){
        this.name = name;
        this.token = false;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }
}