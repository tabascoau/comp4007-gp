package AppKickstarter;

class ElevatorRequestHandler {
    private String pid;
    private int srcFloor, destFloor;

    public ElevatorRequestHandler(String pid, int srcFloor, int destFloor) {
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
}