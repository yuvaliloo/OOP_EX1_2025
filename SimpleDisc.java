public class SimpleDisc implements Disc{
    private Player owner;
    public SimpleDisc(Player p)
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
        return "⬤";
    }
}
