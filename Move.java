public class Move {
    private Disc d;
    private Position p;
    public Move(Disc d, Position p)
    {
        this.d=d;
        this.p=p;
    }
    public Disc disc()
    {
        return d;
    }
    public Position position()
    {
        return p;
    }

}
