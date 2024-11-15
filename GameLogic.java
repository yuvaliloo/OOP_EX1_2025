import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
public class GameLogic  implements PlayableLogic {
    private Disc [][] board=new Disc[8][8];
    private Player player1;
    private Player player2;
    private int turn;
    private Stack<Move> moveHistory;
    private ArrayList<Disc> curFlipped, lastFlipped;
    private Stack<Disc [][]> boardHistory;
    private boolean undid;
    public GameLogic()
    {
        boardHistory=new Stack<>();
        turn=1;
        moveHistory=new Stack<Move>();
        player1=new HumanPlayer(true);
        player2= new HumanPlayer(false);
        curFlipped=new ArrayList<>();
        undid=false;
        for(int i=0;i<8;i++)
        {
            for(int j=0;j<8;j++)
            {
                board[i][j]=null;
            }
        }
        board[3][4]=new SimpleDisc(player2);
        board[4][3]=new SimpleDisc(player2);
        board[3][3]=new SimpleDisc(player1);
        board[4][4]=new SimpleDisc(player1);
    }

    @Override
    public boolean locate_disc(Position a, Disc disc) {
        boolean isValid=false;
        for(int i=0;i<ValidMoves().size();i++)
        {
           if(ValidMoves().get(i).row()==a.row() && ValidMoves().get(i).col()==a.col()) {
               isValid = true;
               i=64;
           }
        }
        if(isValid) {
            board[a.row()][a.col()]=disc;
            saveFlipped(a);
            lastFlipped=curFlipped;
            moveHistory.add(new Move(disc, a));
            for(int i=0;i<curFlipped.size();i++)
            {
                if(isFirstPlayerTurn())
                    curFlipped.get(i).setOwner(player1);
                else
                    curFlipped.get(i).setOwner(player2);
            }
            turn++;
            Disc [][]b=copyBoard(board);
            boardHistory.add(b);
            curFlipped=new ArrayList<>();
            undid=false;
            return true;
        }
        return false;
    }
    public void saveFlipped(Position a)
    {
        int [][] directions={{-1,-1},{-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1}};
        for(int [] direction : directions)
        {
            saveFlippedInDirection(a.row(),a.col(),direction[0],direction[1]);
        }

    }
    public void saveFlippedInDirection(int row, int col, int deltaRow, int deltaCol)
    {
        ArrayList<Disc> temp=new ArrayList<>();
        int curRow=row+deltaRow;
        int curCol=col+deltaCol;
        while((curRow<8 && curRow>=0 && curCol<8 && curCol>=0))
        {
            if(board[curRow][curCol]!=null)
                if((isFirstPlayerTurn())&& !(board[curRow][curCol].getOwner().isPlayerOne)||(!isFirstPlayerTurn())&& (board[curRow][curCol].getOwner().isPlayerOne))
                    temp.add(board[curRow][curCol]);
            curRow+=deltaRow;
            curCol+=deltaCol;
        }
        if((curRow<8 && curRow>=0 && curCol<8 && curCol>=0)&& ((isFirstPlayerTurn())&& (board[curRow][curCol].getOwner().isPlayerOne)||(!isFirstPlayerTurn())&& (!board[curRow][curCol].getOwner().isPlayerOne)))
        {
            curFlipped=temp;
        }
    }
    @Override
    public Disc getDiscAtPosition(Position position) {
        return board[position.row()][position.col()];
    }

    @Override
    public int getBoardSize() {
        return 8;
    }

    @Override
    public List<Position> ValidMoves() {
        ArrayList<Position> valids=new ArrayList<Position>();
        for(int i=0;i<8;i++)
            for(int j=0;j<8;j++)
            {
                if(board[i][j]==null && (countFlips(new Position(i,j))!=0))
                    valids.add(new Position(i,j));
            }
        return valids;
    }

    @Override
    public int countFlips(Position a) {
        int flips=0;
        int [][] directions={{-1,-1},{-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1}};
            for(int [] direction : directions)
            {
                flips+=countFlipsInDirection(a.row(),a.col(),direction[0],direction[1]);
            }
        return flips;
    }
    public int countFlipsInDirection( int row, int col, int deltaRow, int deltaCol)
    {
        boolean isFirstNull=true;
        int flips=0;
        int curRow=row+deltaRow;
        int curCol=col+deltaCol;
        if(curRow<8 && curRow>=0 && curCol<8 && curCol>=0 && board[curRow][curCol]!=null)
            isFirstNull=false;
        if(!isFirstNull)
            while((curRow<8 && curRow>=0 && curCol<8 && curCol>=0))
            {
                if(board[curRow][curCol]!=null) {
                    if (((isFirstPlayerTurn()) && !(board[curRow][curCol].getOwner().isPlayerOne)) || (!isFirstPlayerTurn()) && (board[curRow][curCol].getOwner().isPlayerOne))
                        flips++;
                    if (isFirstPlayerTurn() == board[curRow][curCol].getOwner().isPlayerOne)
                        return flips;
                }
                curRow+=deltaRow;
                curCol+=deltaCol;
            }
        return 0;
    }
    public boolean containsDisc(ArrayList<Disc> lst, Disc d)
    {
        for(int i=0;i<lst.size();i++)
        {
            if(lst.get(i).equals(d))
                return true;
        }
        return false;
    }
    public int setOffBomb(Position p)
    {
        int enemyFlips=0;
        for(int i=p.row()-1;i<p.row()+2;i++)
        {
            for(int j=p.col()-1;j<p.col()+2;j++)
                if((i!=p.row() && j!=p.col()) && !board[i][j].getType().equals("⭕")) {
                    if((isFirstPlayerTurn())&& !(board[i][j].getOwner().isPlayerOne)||(!isFirstPlayerTurn())&& (board[i][j].getOwner().isPlayerOne)) {
                        enemyFlips++;
                        curFlipped.add(board[i][j]);
                    }
                }
        }

        return enemyFlips;
    }
    @Override
    public Player getFirstPlayer() {
        return player1;
    }

    @Override
    public Player getSecondPlayer() {
        return player2;
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        this.player1=player1;
        this.player2=player2;
    }

    @Override
    public boolean isFirstPlayerTurn() {
        return turn%2==1;
    }

    @Override
    public boolean isGameFinished() {
        return ValidMoves().isEmpty();
    }

    @Override
    public void reset() {
        for(int i=0;i<8;i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j]=null;
            }
        }
        board[3][4]=new SimpleDisc(player2);
        board[4][3]=new SimpleDisc(player2);
        board[3][3]=new SimpleDisc(player1);
        board[4][4]=new SimpleDisc(player1);
        Disc [][]b=copyBoard(board);
        boardHistory.add(b);
        turn=1;
        curFlipped=new ArrayList<>();
        setPlayers(player1,player2);
    }

    @Override
    public void undoLastMove() {
        Position p;
        if(!moveHistory.isEmpty()) {
            p=moveHistory.pop().position();
            board[p.row()][p.col()]=null;
            for(int i=0;i<lastFlipped.size();i++)
            {
                if(isFirstPlayerTurn())
                    lastFlipped.get(i).setOwner(player1);
                else
                    lastFlipped.get(i).setOwner(player2);
            }
            turn--;
        }
    }
    public Disc [][] copyBoard(Disc [][] board)
    {
        Disc [][] b= new Disc[8][8];
        for(int i=0;i<8;i++)
            for(int j=0;j<8;j++)
            {
                b[i][j]=board[i][j];
            }
        return b;
    }

}
