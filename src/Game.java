import javax.swing.*;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends UnicastRemoteObject implements IPlayerServer{

    static ILobby lobby;
    static Player myself;
    ArrayList<Player> players;
    static lobbyGui lg;
    static gameGui gg;
    JFrame frame;
    Timer predecessorTimer = null;
    TimerTask predecessorTimerTask = null;

    protected Game() throws RemoteException {
        players = new ArrayList<>();

    }


    public static void main(String[] args) throws RemoteException {
        Game G = new Game();
        myself = new Player("anonymous");
        lg = new lobbyGui(G);
        lg.initializeGUI();
        //gg = new gameGui();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public static void bindServer(){
        System.out.println("binding player server");
         try {
             LocateRegistry.createRegistry(1099);
         } catch (RemoteException e) {
             //ge.printStackTrace();
             System.out.println("rmi registry already created");
         }
         try {
            Registry reg = LocateRegistry.getRegistry(myself.address);
            IPlayerServer ps =  new Game();
            //System.out.println(myself.address+"/"+myself.name);
            reg.rebind(myself.address+"/"+myself.name, ps);
        } catch (RemoteException  e) {
            System.out.println("cannot bind");
            e.printStackTrace();
        }
        System.out.println("bound");
    }

     public void getUsers() throws RemoteException {
        //System.out.println("retrieving players list");
        players = lobby.getPlayers();
    }


    static public void register() throws RemoteException {
        String a = lobby.register(myself);
        if(a ==null){
            myself.name = myself.name + String.valueOf(new Random().nextInt(100));
        }
        else {
            myself.address = a;
            System.out.println("you have been added with index: " + String.valueOf(myself.idx));
        }
        System.setProperty("java.rmi.server.hostname",a);
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
        frame = gg.initializeGUI();

        lg.disposeGUI();

    }
    @Override
    public int ping(String name) throws RemoteException, ServerNotActiveException {
        try {
            String s = "ping from " + name + " " + getClientHost();
            //System.out.println(s);
            return 1;
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
            throw e;
        }
        //return 0;
    }


    @Override
    public void recieveMessage(String name, String msg){
        //System.out.println("msg received\n"+name +": " +msg+"size of queue " + myself.msgQueue.size());
        myself.msgQueue.add(name+": "+msg);
    }


    @Override
    public void startGame(ArrayList<Player> p, int i) throws RemoteException {
        //System.out.println("my definitive index is: " + i +". was "+ myself.idx);

        players = p;
        int playerNum = players.size();

        players.get(0).setPredecessor(players.get(playerNum - 1));
        players.get(0).setSuccessor(players.get(1));

        for (int j = 1; j < playerNum; j++)
        {
            players.get(j).setPredecessor(players.get(j - 1));
            players.get(j).setSuccessor(players.get((j + 1) % playerNum));
        }

        myself.setIdx(i);
        //Game.lobby.unregister(players.get(i));
        initializeTable();
        //gg.initPieces();
        if(i == 0){
            System.out.println("i'm first");
            Game.myself.setToken(true);
            gg.rollButton.setEnabled(true);
        }else{
            System.out.println("not my turn");
            gg.rollButton.setEnabled(false);
        }
        int pred = (i + players.size() - 1)%players.size();
        System.out.println("pred is:" + pred);
        Game.myself.setPredecessor( players.get(pred));
        Game.myself.setSuccessor(players.get((i+1)%players.size()));
        gg.printText("it's " +players.get(0).name +"'s turn.",false,true);
    }

    @Override
    public void makeTurn() throws RemoteException {
        gg.printText("It's " + Game.myself.name+"'s turn.",false,true);

        Game.myself.setToken(true);
        gg.setRollButtonEnabled(true);

        for(Player p : players)
        {
            //if (p.idx != myself.idx)
            if(!myself.equals(p))
            {
                try
                {
                    Registry      reg = LocateRegistry.getRegistry(p.address);
                    IPlayerServer ps  = (IPlayerServer) reg.lookup(p.address + "/" + p.name);
                    ps.notifyTurn(myself);
                }
                catch (RemoteException e)
                {
                    System.out.println("\nHo lanciato " + e.getMessage() + "\n perchè non ho potuto notificare il turno a " + p.name + "\n");
                }
                catch (NotBoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void notifyTurn(Player player) throws RemoteException
    {


        gg.printText("It's " + player.name + "'s turn.",false,true);


        check(player);

    }

    private synchronized void check(final Player player)
    {
        if (predecessorTimer != null)
        {
            predecessorTimerTask.cancel();
            predecessorTimer.cancel();

        }
        predecessorTimer = new Timer();
        predecessorTimerTask = new TimerTask(){
            @Override
            public void run(){

                int playerMovingIndex = players.indexOf(player);
                Player playerMoving = players.get(playerMovingIndex);
                Player myPredecessor = myself.getPredecessor();
                int myPredecessorIndex = players.indexOf(myPredecessor);
                int mySelfIndex = players.indexOf(myself);

                System.out.println("Io sono " + myself.name + " e voglio pingare il mio predecessore " + myPredecessor.name);


                //System.out.println(playerMoving.name + " is moving and it's my predecessor (" + myself.name + ")");

                try
                {
                    Registry reg = LocateRegistry.getRegistry(myPredecessor.address);
                    IPlayerServer ps = (IPlayerServer) reg.lookup(myPredecessor.address+"/"+myPredecessor.name);
                    ps.ping(myPredecessor.name);
                }
                catch (RemoteException e)
                {
                    //e.printStackTrace();
                    // Devo appropriarmi del turno
                    System.out.println("PPPPPPPPPPPPPPP");
                    if (playerMoving.equals(myPredecessor)) // Il mio predecessore è crashato senza cedermi il turno.
                    {
                        System.out.println("[" + myself.name + "]: Player moning = " + playerMoving.name + "myPredecessor = " + myPredecessor.name);

                        try
                        {
                            players.get(playerMovingIndex).setToken(false);

                            Registry reg = LocateRegistry.getRegistry(myself.address);
                            IPlayerServer ps = (IPlayerServer) reg.lookup(myself.address+"/"+myself.name);
                            ps.makeTurn();

                            System.out.println("[" + myself.name + "]: " + "Chiamata la prima make");
                            myself.setPredecessor(myPredecessor.getPredecessor());
                            players.get(players.indexOf(myself)).setPredecessor(myPredecessor.getPredecessor());
                            players.get(myPredecessorIndex).getPredecessor().setSuccessor(players.get(mySelfIndex));

                            predecessorTimerTask.cancel();
                            predecessorTimer.cancel();

                            System.out.println("Sono " + myself.name + " e ho stoppato il timer");
                        }
                        catch (RemoteException e1)
                        {
                            e1.printStackTrace();
                        }
                        catch (NotBoundException e1)
                        {
                            e1.printStackTrace();
                        }

                    }
                    else // Check if the player who has to move is up.
                    {
                        Player predecessor = myPredecessor;

                        // 4

                        do
                        {
                            predecessor = myPredecessor.getPredecessor();

                            System.out.println("[" + myself.name + "]: " + "playerMoving diverso da mio predecessore");
                            System.out.println("[" + myself.name + "]: " + "predecessore " + myself.getPredecessor().name + " crashato");
                            System.out.println("[" + myself.name + "]: " + "voglio pingare " + predecessor.name);

                            try
                            {
                                Registry reg = LocateRegistry.getRegistry(predecessor.address);
                                IPlayerServer ps = (IPlayerServer) reg.lookup(predecessor.address+"/"+predecessor.name);
                                ps.ping(predecessor.name);

                                System.out.println("[" + myself.name + "]: " + "ho pingato " + predecessor.name);

                                // A predecessor not crashed found
                                myself.setPredecessor(predecessor);
                                players.get(players.indexOf(myself)).setPredecessor(predecessor);
                                predecessor.setSuccessor(players.get(players.indexOf(myself)));

                                predecessorTimerTask.cancel();
                                predecessorTimer.cancel();
                                break;
                            }
                            catch (RemoteException e1)
                            {
                                //e1.printStackTrace();


                                if (predecessor.equals(playerMoving))
                                {
                                    System.out.println("[" + myself.name + "]: " + predecessor.name + " doveva muovere");

                                    players.get(playerMovingIndex).setToken(false);

                                    try
                                    {
                                        Registry      reg = LocateRegistry.getRegistry(myself.address);
                                        IPlayerServer ps  = (IPlayerServer) reg.lookup(myself.address + "/" + myself.name);
                                        ps.makeTurn();

                                        System.out.println("[" + myself.name + "]: " + "Chiamata la seconda make");

                                        myself.setPredecessor(predecessor.getPredecessor());
                                        players.get(players.indexOf(myself)).setPredecessor(predecessor.getPredecessor());
                                        players.get(players.indexOf(predecessor.getPredecessor())).setSuccessor(players.get(players.indexOf(myself)));

                                        predecessorTimerTask.cancel();
                                        predecessorTimer.cancel();
                                        break;
                                    }
                                    catch (RemoteException e2)
                                    {
                                        e2.printStackTrace();
                                    }
                                    catch (NotBoundException e2)
                                    {
                                        e2.printStackTrace();
                                    }
                                }
                            }
                            catch (ServerNotActiveException e1)
                            {
                                e1.printStackTrace();
                            }
                            catch (NotBoundException e1)
                            {
                                e1.printStackTrace();
                            }
                        } while (!predecessor.equals(myself));
                    }
                }
                catch (ServerNotActiveException e)
                {
                    e.printStackTrace();
                }
                catch (NotBoundException e)
                {
                    e.printStackTrace();
                }
/*
                int myIndex = players.indexOf(myself);

                do
                {
                    try
                    {
                        String toPing = players.get(myIndex).getPredecessor().name;
                        ping(toPing);
                    }
                    catch (RemoteException e)
                    {
                        e.printStackTrace();
                    }

                } while ()*/
            }
        };

        predecessorTimer.schedule(predecessorTimerTask,0,3000);
    }

    @Override
    public void notifyWin(int playerIndex) throws RemoteException
    {
        String playerName = players.get(playerIndex).name;

        JOptionPane pane = new JOptionPane(playerName  + " wins!\nYour position is " + (myself.getPosition() + 1));
        JDialog dialog = pane.createDialog(frame, "Game Over!");
        dialog.setModal(false);
        dialog.setVisible(true);

        gg.printText("GAME OVER!\n" + playerName + " wins!", false, true);

        gg.setRollButtonEnabled(false);
    }

    @Override
    public void updatePosition(int i, int r) throws RemoteException{
        if (i != myself.idx){
            System.out.println("player " + i + " rolled a " + r +".\nold position is: " + (players.get(i).getPosition() +1));
            players.get(i).updatePosition(r);
            System.out.println("new position: "+ players.get(i).getPosition());
            gg.move(i,players.get(i).getPosition());
        }
        gg.printText("\n" + players.get(i).name + " rolled a " + r + "\n",false,false);
    }
}
