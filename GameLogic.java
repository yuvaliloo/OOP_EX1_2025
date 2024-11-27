import java.util.*;
import java.util.List;
public class GameLogic  implements PlayableLogic {
    private Disc [][] board=new Disc[8][8];
    private Player player1;
    private Player player2;
    protected Player curPlayer;
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
                System.out.println("\nPlayer 1 placed a "+disc.getType()+" in "+"("+a.row()+","+a.col()+")");
            else
                System.out.println("\nPlayer 2 placed a "+disc.getType()+" in "+"("+a.row()+","+a.col()+")");
            saveFlipped(a);
            if(isFirstPlayerTurn())
                for(int i=0;i<curFlipped.size();i++)
                {
                    curFlipped.get(i).setOwner(player1);
                    System.out.println("Player 1 flipped a "+curFlipped.get(i).getType()+" in "+"("+curFlippedPos.get(i).row()+","+curFlippedPos.get(i).col()+")");
                }
            else
                for(int i=0;i<curFlipped.size();i++)
                {
                    curFlipped.get(i).setOwner(player2);
                    System.out.println("Player 2 flipped a "+curFlipped.get(i).getType()+" in "+"("+curFlippedPos.get(i).row()+","+curFlippedPos.get(i).col()+")");
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
    /**
     * saves the discs that will be flipped by a move.
     * @param a is the current position of the tile.
     */
    public void saveFlipped(Position a)
    {
        bombFlips.clear();
        int [][] directions={{-1,-1},{-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1}};
        for(int [] direction : directions)
        {
            saveFlippedInDirection(a.row(),a.col(),direction[0],direction[1]);
        }

    }
    /**
     * Checks a specific direction (up, down, diagonal) from a position to determine which discs can be flipped.
     * @param row pointing on the row of the tile that is being checked.
     * @param col pointing on the column of the tile that is being checked.
     * @param deltaRow is dictating the direction (row-wise) we want to check from the current tile.
     * @param deltaCol is dictating the direction (column-wise) we want to check from the current tile.
     */
    public void saveFlippedInDirection(int row, int col, int deltaRow, int deltaCol)
    {
        ArrayList<Disc> temp=new ArrayList<>();
        ArrayList<Position> tempPos=new ArrayList<>();
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
                            else {
                                temp.add(board[curRow][curCol]);
                                tempPos.add(new Position(curRow,curCol));
                            }
                        }
                    if (isFirstPlayerTurn() == board[curRow][curCol].getOwner().isPlayerOne) {
                        for(Position p:isBombSet)
                            setOffBomb(p);
                        curFlipped.addAll(temp);
                        curFlippedPos.addAll(tempPos);
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
    /**
     * check the amount of discs that can be flipped in all valid directions from the current tile.
     * @param row pointing on the row of the tile that is being checked.
     * @param col pointing on the column of the tile that is being checked.
     * @param deltaRow is dictating the direction (row-wise) we want to check from the current tile.
     * @param deltaCol is dictating the direction (column-wise) we want to check from the current tile.
     */
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
    /**
     * Checks if the given disc d is already in the list "lst".
     * @param lst the list we want to check if the disc is contained in.
     * @param d the disc we want to check if contained on the list.
     * @return true if he is on the list
     */
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
    /**
     * Counts the number of enemy discs (and possible bomb effects) around the bomb at position p.
     * @param p is the position we want to check from.
     * @return the amount of enemy discs that will be flipped on the current move.
     */
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
    /**
     * Activates the bomb and flipping the surrounding enemy discs.
     * @param p the position of the bomb that is being set off.
     */
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
                                if (((isFirstPlayerTurn()) && !(board[i][j].getOwner().isPlayerOne))) {
                                    board[i][j].setOwner(player1);
                                    System.out.println("Player 1 flipped a "+board[i][j].getType()+" in "+"("+i+","+j+")");
                                }
                                else if (((!isFirstPlayerTurn()) && (board[i][j].getOwner().isPlayerOne))) {
                                    board[i][j].setOwner(player2);
                                    System.out.println("Player 2 flipped a "+board[i][j].getType()+" in "+"("+i+","+j+")");
                                }
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
        if(ValidMoves().isEmpty()) {
            int p1 = 0, p2 = 0;
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++) {
                    if(board[i][j]!=null)
                        if (board[i][j].getOwner().isPlayerOne)
                            p1++;
                        else
                            p2++;
                }
            if (p1 > p2)
                player1.wins++;
            else if (p1 < p2)
                player2.wins++;
            return true;
        }
        return false;
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
        if(!player1.isHuman() || !player2.isHuman())
            if(turn>1) {
                boardHistory.pop();
                board=copyBoard(boardHistory.peek());
                turn--;
            }
    }
    /**
     * Creates a copy of the game board.
     * @param board the board we want to copy.
     * @return the copy of the board.
     */
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
    public List<Position> getOpponentValidMoves() {
        // Determine the opponent based on whose turn it is
        Player opponent = this.isFirstPlayerTurn() ? this.getSecondPlayer() : this.getFirstPlayer();

        // Switch to the opponent's turn and get their valid moves
        // Assuming `ValidMoves` returns the valid moves for the current turn's player
        return this.ValidMoves();  // Since `ValidMoves()` already knows whose turn it is internally
    }



}
