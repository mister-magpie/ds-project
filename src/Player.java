import java.io.Serializable;
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
    boolean crash;

    int[] snakeHeads  = {17, 52, 57, 62, 88, 95, 97};
    int[] snakeTails  = {13, 29, 40, 22, 18, 51, 79};
    int[] ladderStart = {3, 8, 28, 58, 75, 80, 90};
    int[] ladderEnd   = {21, 30, 84, 77, 86, 100, 91};


    public Player(String name) {
        super();
        this.name = name;
        this.token = false;
        this.address = "localhost";
        this.msgQueue = new ArrayDeque<String>(10);
        this.ready = false;
        this.position = 0;
        this.crash = false;
    }


    public void setIdx(int idx) {
        this.idx = idx;
    }
    public void setUsername(String name){
        this.name = name;
        this.address = "localhost/"+name;
    }
    public int updatePosition(int roll){
        //System.out.println(position);

        int newPosition = this.position + roll;

        System.out.println("Position + roll = " + newPosition);

        // Snake found?

        int itemIndex;

        itemIndex = onSnake(newPosition);

        if (itemIndex != -1)
        {
            int tailPosition = snakeTails[itemIndex];

            setPosition(tailPosition - 1);
        }
        else
        {
            // Or Ladder?

            itemIndex = onLadder(newPosition);

            if (itemIndex != -1)
            {
                int ladderPosition = ladderEnd[itemIndex];

                setPosition(ladderPosition - 1);
            }
            else
            {
                //players.get(i).updatePosition(r);
                this.position += roll;


                if (this.position >= 100)
                {
                    this.position = 99 - (this.position % 99);

                    itemIndex = onSnake(this.position);

                    if (itemIndex != -1)
                    {
                        int tailPosition = snakeTails[itemIndex];
                        setPosition(tailPosition - 1);
                    }
                    else
                    {
                        itemIndex = onLadder(this.position);

                        if (itemIndex != -1)
                        {
                            int ladderPosition = ladderEnd[itemIndex];
                            setPosition(ladderPosition - 1);
                        }
                    }

                    System.out.println("Ho settato = " + this.position);
                }
            }
        }

        //
        System.out.println(this.name +": update position " + position);
        return position;
    }

    private int onSnake(int position)
    {

        // Check for a snake

        int itemIndex = -1;

        for (int k = 0; k < snakeHeads.length; k++)
        {
            if (snakeHeads[k] == position + 1)
            {
                itemIndex = k;
                break;
            }
        }

        return itemIndex;
    }

    private int onLadder(int position)
    {

        // Check for a ladder

        int itemIndex = -1;

        for (int k = 0; k < ladderStart.length; k++)
        {
            if (ladderStart[k] == position + 1)
            {
                itemIndex = k;
                break;
            }
        }

        return itemIndex;
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

    public boolean isCrashed()
    {
        return crash;
    }

    public void setCrash(boolean crash)
    {
        this.crash = crash;
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

        return name != null ? name.equals(player.name) : player.name == null;
    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }
}