import java.util.*;
import java.util.List;
public class GameLogic  implements PlayableLogic {
    private Disc [][] board=new Disc[8][8];
    private Player player1;
    private Player player2;
    private Player curPlayer;
    private int turn;
    private ArrayList<Disc> curFlipped, bombFlips, eatFlips;
    private ArrayList<Position> curFlippedPos;
    private Stack<Disc [][]> boardHistory;
    public GameLogic()
    {
        boardHistory=new Stack<>();
        turn=1;
        player1=new HumanPlayer(true);
        player2= new HumanPlayer(false);
        curPlayer=player1;
        curFlipped=new ArrayList<>();
        curFlippedPos=new ArrayList<>();
        bombFlips=new ArrayList<>();
        eatFlips=new ArrayList<>();
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
        boolean isValid=false,hasBombs=curPlayer.getNumber_of_bombs()>0,hasUnflipp=curPlayer.getNumber_of_unflippedable()>0, nonLeft=false;
        if(disc.getType().equals("💣"))
            if(hasBombs)
                curPlayer.reduce_bomb();
            else
                nonLeft=true;
        else if(disc.getType().equals("⭕"))
            if(hasUnflipp)
                curPlayer.reduce_unflippedable();
            else
                nonLeft=true;
        if(!nonLeft)
            for(int i=0;i<ValidMoves().size();i++)
            {
               if(ValidMoves().get(i).row()==a.row() && ValidMoves().get(i).col()==a.col()) {
                   isValid = true;
                   i=64;
               }
            }
        if(isValid) {
            board[a.row()][a.col()]=disc;
            if(curPlayer.isPlayerOne)
                System.out.println("Player 1 placed a "+disc.getType()+" in "+"["+a.row()+"]"+"["+a.col()+"]");
            else
                System.out.println("Player 2 placed a "+disc.getType()+" in "+"["+a.row()+"]"+"["+a.col()+"]");
            saveFlipped(a);
            if(isFirstPlayerTurn())
                for(int i=0;i<curFlipped.size();i++)
                {
                    curFlipped.get(i).setOwner(player1);
                    System.out.println("Player 1 flipped a "+curFlipped.get(i).getType()+" in ");
                }
            else
                for(int i=0;i<curFlipped.size();i++)
                {
                    curFlipped.get(i).setOwner(player2);
                }
            if(isFirstPlayerTurn())
                curPlayer=player2;
            else
                curPlayer=player1;
            turn++;
            Disc [][]b=copyBoard(board);
            boardHistory.add(b);
            curFlipped=new ArrayList<>();
            return true;
        }
        return false;
    }
    public void saveFlipped(Position a)
    {
        bombFlips.clear();
        int [][] directions={{-1,-1},{-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1}};
        for(int [] direction : directions)
        {
            saveFlippedInDirection(a.row(),a.col(),direction[0],direction[1]);
        }

    }
    public void saveFlippedInDirection(int row, int col, int deltaRow, int deltaCol)
    {
        ArrayList<Disc> temp=new ArrayList<>();
        ArrayList<Position> isBombSet=new ArrayList<>();
        boolean isFirstNull=true;
        int curRow=row+deltaRow;
        int curCol=col+deltaCol;
        if(curRow<8 && curRow>=0 && curCol<8 && curCol>=0 && board[curRow][curCol]!=null)
            isFirstNull=false;
        if(!isFirstNull)
            while((curRow<8 && curRow>=0 && curCol<8 && curCol>=0))
            {
                if(board[curRow][curCol]!=null) {
                    if(!board[curRow][curCol].getType().equals("⭕"))
                        if (((isFirstPlayerTurn()) && !(board[curRow][curCol].getOwner().isPlayerOne)) || (!isFirstPlayerTurn()) && (board[curRow][curCol].getOwner().isPlayerOne)) {
                            if(board[curRow][curCol].getType().equals("💣"))
                                isBombSet.add(new Position(curRow,curCol));
                            else
                                temp.add(board[curRow][curCol]);
                        }
                    if (isFirstPlayerTurn() == board[curRow][curCol].getOwner().isPlayerOne) {
                        for(Position p:isBombSet)
                            setOffBomb(p);
                        curFlipped.addAll(temp);
                    }
                }
                curRow+=deltaRow;
                curCol+=deltaCol;
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
        bombFlips.clear();
        eatFlips.clear();
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
        ArrayList<Position> bombs=new ArrayList<>();
        ArrayList<Position> temp=new ArrayList<>();
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
                    if(!board[curRow][curCol].getType().equals("⭕"))
                        if (((isFirstPlayerTurn()) && !(board[curRow][curCol].getOwner().isPlayerOne)) || (!isFirstPlayerTurn()) && (board[curRow][curCol].getOwner().isPlayerOne)) {
                            if(board[curRow][curCol].getType().equals("💣")) {
                                bombs.add(new Position(curRow, curCol));
                            }
                            else
                                if(!containsDisc(bombFlips,board[curRow][curCol])){
                                    temp.add(new Position(curRow,curCol));
                                    flips++;
                                }
                        }
                    if (isFirstPlayerTurn() == board[curRow][curCol].getOwner().isPlayerOne) {
                        for(Position p : temp)
                            eatFlips.add(board[p.row()][p.col()]);
                        for(Position p : bombs)
                            flips+=countBomb(p);
                        return flips;
                    }
                }
                curRow+=deltaRow;
                curCol+=deltaCol;
            }
        return 0;
    }
    public boolean containsDisc(ArrayList<Disc> lst, Disc d)
    {
        if(lst!=null)
            for(int i=0;i<lst.size();i++)
            {
                if(lst.get(i).equals(d))
                    return true;
            }
        return false;
    }
    public int countBomb(Position p)
    {
        int enemyFlips=0;
        for(int i=p.row()-1;i<p.row()+2;i++)
        {
            for(int j=p.col()-1;j<p.col()+2;j++)
                if(i>=0 && i<8 && j>=0 && j<8)
                    if(board[i][j]!=null)
                        if( !board[i][j].getType().equals("⭕") && !containsDisc(eatFlips,board[i][j]) && !containsDisc(bombFlips,board[i][j])) {
                            if(((isFirstPlayerTurn())&& !(board[i][j].getOwner().isPlayerOne))||((!isFirstPlayerTurn())&& (board[i][j].getOwner().isPlayerOne))) {
                                if(board[i][j].getType().equals("💣") && !(i==p.row() && j==p.col()))
                                    enemyFlips+=countBomb(new Position(i,j));
                                else {
                                    enemyFlips++;
                                }
                                bombFlips.add(board[i][j]);
                            }
                        }
        }

        return enemyFlips;
    }
    public void setOffBomb(Position p)
    {
        for(int i=p.row()-1;i<p.row()+2;i++)
        {
            for(int j=p.col()-1;j<p.col()+2;j++)
                if(i>=0 && i<8 && j>=0 && j<8)
                    if(board[i][j]!=null)
                        if( !board[i][j].getType().equals("⭕") && !containsDisc(bombFlips,board[i][j])) {
                            if(board[i][j].getType().equals("💣") && !(i==p.row() && j==p.col()))
                                setOffBomb(new Position(i,j));
                            else {
                                if (((isFirstPlayerTurn()) && !(board[i][j].getOwner().isPlayerOne)))
                                    board[i][j].setOwner(player1);
                                else if (((!isFirstPlayerTurn()) && (board[i][j].getOwner().isPlayerOne)))
                                    board[i][j].setOwner(player2);
                            }
                            bombFlips.add(board[i][j]);
                        }
        }
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
        int p1=0,p2=0;
        for(int i=0;i<8;i++)
            for(int j=0;j<8;j++)
            {
                if(board[i][j].getOwner().isPlayerOne)
                    p1++;
                else
                    p2++;
            }
        if(p1>p2)
            player1.wins++;
        else if(p1<p2)
            player2.wins++;
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
        boardHistory=new Stack<>();
        Disc [][]b=copyBoard(board);
        boardHistory.add(b);
        turn=1;
        curFlipped=new ArrayList<>();
        curFlippedPos=new ArrayList<>();
        bombFlips=new ArrayList<>();
        eatFlips=new ArrayList<>();
        setPlayers(player1,player2);
        curPlayer=player1;
    }

    @Override
    public void undoLastMove() {
        if(turn>1) {
            boardHistory.pop();
            board=copyBoard(boardHistory.peek());
            turn--;
        }
    }
    public Disc [][] copyBoard(Disc [][] board)
    {
        Disc [][] b= new Disc[8][8];
        for(int i=0;i<8;i++)
            for(int j=0;j<8;j++)
            {
                b[i][j]=copyDisc(board[i][j]);
            }
        return b;
    }
    public Disc copyDisc(Disc d)
    {
        Disc disc=null;
        if(d!=null)
            if(d instanceof SimpleDisc)
                disc=new SimpleDisc(d.getOwner());
            else if(d instanceof BombDisc)
                disc=new BombDisc(d.getOwner());
            else
                disc=new UnflippableDisc(d.getOwner());
        return disc;
    }

}
