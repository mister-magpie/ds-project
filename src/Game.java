
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class Game extends UnicastRemoteObject implements IPlayerServer{

    ILobby lobby;
    Player myself;
    ArrayList<Player> players;
    lobbyGui lg;
    gameGui gg;


    public Game() throws RemoteException {
        super();

        players = new ArrayList<>();
        myself = new Player("anonymous");
        lg = new lobbyGui(this);

        lg.connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String username = lg.usernameField.getText();
                if(username == null) username = "anonymous";
                try {
                    myself = new Player(username);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                //System.out.println(username);
                //G.getMyself().setUsername(username);

                //connectiong phase
                try {
                    lg.printText("Connecting...",false,true);
                    String lobbyAddr = lg.lobbyAddressTextField.getText();
                    initializeLobby(lobbyAddr);
                } catch (RemoteException | NotBoundException | MalformedURLException e) {
                    e.printStackTrace();
                }
                lg.printText("ok",true,true);

                //registering phase
                try {
                    lg.printText("Registering...",false,true);
                    register();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                lg.printText("ok",true,true);
                lg.usernameField.setEditable(false);
                lg.connectButton.setEnabled(false);
                //
                lg.readyCheckBox.setEnabled(true);
                lg.printText("when you are ready to start check the box",false,true);

                //update cycles
                try {
                    players = getUsers();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                lg.updateList();
            }
        });

    }

    public void setMyself(Player p){
        this.myself = p;
    }

    public Player getMyself() {
        return myself;
    }


     public void bindServer(){
        try {
            Registry reg = LocateRegistry.getRegistry(myself.address);
            IPlayerServer ps =  new Game();
            reg.rebind(myself.address+"/"+myself.name, ps);
            System.out.println("server bound: " + myself.address+"/"+myself.name);

        } catch (RemoteException  e) {
            System.out.println("cannot bind");
            e.printStackTrace();
        }
    }

     public ArrayList<Player> getUsers() throws RemoteException {
        System.out.println("retrieving players list, i am # " + myself.idx);
        //players = lobby.getPlayers();
        //setMyself(players.get(myself.idx));

        return players;
     }


     public void register() throws RemoteException {
        Player p = lobby.register(myself);
        if(p ==null){
            myself.name = myself.name + String.valueOf(new Random().nextInt(100));
            register();
        }
        else {
            myself = p;
            System.out.println("you have been added as: " + myself.name + String.valueOf(myself.idx) + myself.address);
        }
        System.out.println("register: " + myself.name);
        System.setProperty("java.rmi.server.hostname",p.address);
        bindServer();

    }
     public void initializeLobby(String lobbyAddr) throws RemoteException, NotBoundException, MalformedURLException {
        Registry reg = LocateRegistry.getRegistry(lobbyAddr);
        System.out.println("Connecting to Lobby");
        lobby =  (ILobby) reg.lookup (lobbyAddr+"/LobbyServer");
    }

     public void initializeTable(){
        System.out.println("initialize table");
        gg = new gameGui(this);
        gg.initializeGUI();
        if (this.lg == null) System.out.println("no gg");
        lg.disposeGUI();

    }
    @Override
    public int ping(String name, int idx) throws RemoteException {
        //test(name);
        String s = "ping from " + name + " to " ;//+ myself.name;
        System.out.println(s);
        return 1;

    }


    @Override
    public void recieveMessage(String name, String msg){

        System.out.println("receivemsgs: " + this.players.size());
        myself.msgQueue.add(name+": "+msg);
        System.out.println("msg received\n"+name +": " + msg +" size of queue " + myself.msgQueue.size());
    }


    @Override
    public void startGame(ArrayList<Player> p, int i) throws RemoteException {
        //System.out.println("my definitive index is: " + i +". was "+ myself.idx);
        players = p;
        myself.setIdx(i);
        //Game.lobby.unregister(players.get(i));
        initializeTable();
        //gg.initPieces();
        if(i == 0){
            System.out.println("i'm first");
            myself.setToken(true);
        }
        int pred = (i + players.size() - 1)%players.size();
        System.out.println("pred is:" + pred);
        myself.setPredecessor( players.get(pred));
        myself.setSuccessor(players.get((i+1)%players.size()));
        gg.printText("it's " + players.get(0).name + "'s turn.",false,true);
    }

    @Override
    public void makeTurn() throws RemoteException {
        gg.printText("it's " + myself.name+"'s turn.",false,true);
        myself.setToken(true);
    }

    @Override
    public void updatePosition(int i, int r) throws RemoteException{
        if (i != myself.idx){
            //System.out.println("player " + i + " rolled a " + r +". old position is: " + (players.get(0).getPosition() +1));
            //players.get(i).setPosition(r);
            gg.move(players.get(i),r);
        }
        gg.printText(players.get(i).name + " rolled a " + r,false,false);
    }

    void test(String name){
        String s = "ping from " + name + " to " + myself.name;
        System.out.println(s);
    }

}

