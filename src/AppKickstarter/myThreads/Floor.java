package AppKickstarter.myThreads;

public class Floor {
    private int number;
    public boolean up = false;
    public boolean down = false;

    public Floor(int number)
    {
        this.number = number;
    }

    public int GetNumber()
    {
        return number;
    }

    public Floor(Floor floor)
    {
        this.number = floor.GetNumber();
        this.up = floor.up;
        this.down = floor.down;
    }
}