public class BombDisc implements Disc{
    private Player owner;
    public BombDisc(Player p)
    {
        owner=p;
    }
    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player player) {
        owner=player;
    }

    @Override
    public String getType() {
        return "💣";
    }
}
