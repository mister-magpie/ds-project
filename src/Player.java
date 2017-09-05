import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Player implements Serializable {
    String name;
    //Color color;
    private boolean token;
    int idx;
    String address;
    boolean ready;
    Deque<String> msgQueue;
    private Player successor, predecessor;
    private int position;

    private int[] snakeHeads = {17, 52, 57, 62, 88, 95, 97};
    private int[] snakeTails = {13, 29, 40, 22, 18, 51, 79};
    private int[] ladderStart = {3, 8, 28, 58, 75, 80, 90};
    private int[] ladderEnd = {21, 30, 84, 77, 86, 100, 91};


    Player(String name) throws RemoteException {
        super();
        this.name = name;
        this.token = false;
        this.address = "localhost";
        this.msgQueue = new ArrayDeque<>(10);
        this.ready = false;
        this.position = 0;
    }


    void setIdx(int idx) {
        this.idx = idx;
    }

    void setUsername(String name) {
        this.name = name;
        this.address = "localhost/"+name;
    }

    int updatePosition(int roll) {
        //System.out.println(position);

        int newPosition = this.position + roll;

        // Check for a snake

        int itemIndex = -1;

        for (int k = 0; k < snakeHeads.length; k++)
        {
            if (snakeHeads[k] == newPosition + 1)
            {
                itemIndex = k;
                break;
            }
        }

        // Snake found?

        if (itemIndex != -1)
        {
            int tailPosition = snakeTails[itemIndex];

            setPosition(tailPosition - 1);
        }
        else
        {
            // Check for a ladder

            itemIndex = -1;

            for (int k = 0; k < ladderStart.length; k++)
            {
                if (ladderStart[k] == newPosition + 1)
                {
                    itemIndex = k;
                    break;
                }
            }

            if (itemIndex != -1)
            {
                int ladderPosition = ladderEnd[itemIndex];

                setPosition(ladderPosition - 1);
            }
            else
            {
                //players.get(i).updatePosition(r);
                this.position += roll;
            }
        }

        //
        System.out.println(this.name +": update position " + position);
        return position;
    }

    int getPosition() {
        return position;
    }

    void setPosition(int position) {
        this.position = position;
    }

    public boolean isToken() {
        return token;
    }

    Player getSuccessor() {
        return successor;
    }

    void setSuccessor(Player successor) {
        this.successor = successor;
    }

    Player getPredecessor() {
        return predecessor;
    }

    void setPredecessor(Player predecessor) {
        this.predecessor = predecessor;
    }

    void setToken(boolean token) {
        this.token = token;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Player player = (Player) o;

        if (idx != player.idx)
        {
            return false;
        }
        return name.equals(player.name);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + idx;
        return result;
    }
}