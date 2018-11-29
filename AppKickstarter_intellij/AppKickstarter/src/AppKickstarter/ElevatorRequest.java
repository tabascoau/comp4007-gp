package AppKickstarter;

class ElevatorRequest {
    private String pid;
    private int srcFloor, destFloor;

    public ElevatorRequest(String pid, int srcFloor, int destFloor) {
        this.pid = pid;
        this.srcFloor = srcFloor;
        this.destFloor = destFloor;
    }

    public int getDestFloor() {
        return destFloor;
    }

    public int getSrcFloor() {
        return srcFloor;
    }

    public String getPid() {
        return pid;
    }

    public char getDirection() {
        if (srcFloor > destFloor) {
            return 'D';
        }
        return 'U';
    }
}