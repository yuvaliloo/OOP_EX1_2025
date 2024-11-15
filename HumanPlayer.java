public class HumanPlayer extends Player {
    private boolean isHuman;

    public HumanPlayer(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    boolean isHuman() {
        return true;
    }
}
