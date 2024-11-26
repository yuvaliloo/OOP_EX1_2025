import java.util.Random;

public class RandomAI extends AIPlayer{
    public RandomAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(Object gameStatus) {
        Random rnd=new Random();
        int n= rnd.nextInt(0,((GameLogic)gameStatus).ValidMoves().size());
        Position p = ((GameLogic)gameStatus).ValidMoves().get(n);
        Disc d;
        n= rnd.nextInt(0,3);
        if (n==0) d= new SimpleDisc(((GameLogic)gameStatus).curPlayer);
        else if (n==1)  d = new BombDisc(((GameLogic)gameStatus).curPlayer);
        else d= new UnflippableDisc(((GameLogic)gameStatus).curPlayer);
        return new Move(d,p);

    }

}
