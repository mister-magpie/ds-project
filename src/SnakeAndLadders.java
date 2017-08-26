import java.rmi.RemoteException;

public class SnakeAndLadders {
    public static void main(String[] args) {
        try {
            Game game = new Game();
            //System.out.println(game);
            game.lg.initializeGUI();

        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }
}
