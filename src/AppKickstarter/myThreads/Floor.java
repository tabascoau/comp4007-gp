package AppKickstarter.myThreads;

import java.util.ArrayList;

public class Floor {
    private int number;

    // up/down to this floor
    public boolean up = false;
    public boolean down = false;

    // Src going direction
    public Elevator.Direction upUserDirection = null;
    public Elevator.Direction downUserDirection = null;

    // Dest number
    public int dest;

    // Src arrived for dest arrival
    public boolean srcArrived;

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

        this.upUserDirection = null;
        this.downUserDirection = null;
    }

    public void InvertDirection()
    {
        up = !up;
        down = !down;
    }

    public void SrcArrival(Elevator.Direction direction)
    {
        switch (direction)
        {
            case Up:
                up = false;
                upUserDirection = null;
                break;
            case Down:
                down = false;
                downUserDirection = null;
                break;
        }
    }

}