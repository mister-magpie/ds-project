import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;

public class PlayerServer extends UnicastRemoteObject implements IPlayerServer{

    public PlayerServer() throws RemoteException {
    }

    public void hello(){
        System.out.println("helloworld");
    }

    @Override
    public int ping(String name) throws RemoteException {
        try {
            System.out.println("ping from " + name + " " + getClientHost());
            //IPlayerServer ps = (IPlayerServer) Naming.lookup("rmi://"+getClientHost()+"/"+name);
            //ps.ping(name);
            return 1;
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
