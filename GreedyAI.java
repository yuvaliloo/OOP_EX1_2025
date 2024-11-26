public class GreedyAI extends AIPlayer {
    public GreedyAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(Object gameStatus) {
        GameLogic gl =  ((GameLogic)gameStatus);
        int maxi = 0;
        int max = gl.countFlips(gl.ValidMoves().get(0));
        for (int i = 1; i < gl.ValidMoves().size(); i++) {
            int temp = gl.countFlips(gl.ValidMoves().get(i));
            if (temp >= max){
                max=temp;
                maxi=i;

            }
        }
        Position p = (gl.ValidMoves().get(maxi));
        Disc d = new SimpleDisc(gl.curPlayer);
        return new Move(d,p);
    }

}
