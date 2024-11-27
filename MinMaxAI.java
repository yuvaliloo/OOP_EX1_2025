import java.util.List;

public class MinMaxAI extends AIPlayer {
    public MinMaxAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    // Implementation of makeMove from AIPlayer
    @Override
    public Move makeMove(Object gameStatus) {
        // Cast the gameStatus to the actual type (GameLogic)
        GameLogic game = (GameLogic) gameStatus;

        // Get the best move for the AI
        Position bestMove = getBestMove(game);

        // If bestMove is valid, return it as a Move
        if (bestMove != null) {
            Disc d = new SimpleDisc(game.curPlayer);
            return new Move(d , bestMove);  // Return a Move object
        }

        // If no valid move (unlikely), return null
        return null;
    }

    public Position getBestMove(GameLogic game) {
        List<Position> validMoves = game.ValidMoves();
        int bestScore = Integer.MIN_VALUE;
        Position bestMove = null;

        for (Position move : validMoves) {
            // Simulate the move
            game.locate_disc(move, new SimpleDisc(this));  // Assuming AI is 'this' player

            // Evaluate the move using minimax
            int score = minimax(game, 3,Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            // Undo the move
            game.undoLastMove();

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int minimax(GameLogic game, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || game.isGameFinished()) {
            return evaluateBoard(game);
        }

        List<Position> validMoves = game.ValidMoves();

        if (maximizingPlayer) {  // AI's turn (maximize score)
            int maxEval = Integer.MIN_VALUE;
            for (Position move : validMoves) {
                // Simulate the move
                game.locate_disc(move, new SimpleDisc(game.curPlayer));  // Corrected syntax

                // Evaluate this move
                int eval = minimax(game, depth - 1, alpha, beta, false);

                // Undo the move
                game.undoLastMove();

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;  // Alpha-beta pruning
            }
            return maxEval;
        } else {  // Opponent's turn (minimize score)
            int minEval = Integer.MAX_VALUE;
            for (Position move : validMoves) {
                // Simulate the move for the opponent
                Player opponent = game.isFirstPlayerTurn() ? game.getSecondPlayer() : game.getFirstPlayer();
                game.locate_disc(move, new SimpleDisc(opponent));  // Place opponent's disc

                // Evaluate this move
                int eval = minimax(game, depth - 1, alpha, beta, true);

                // Undo the move
                game.undoLastMove();

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;  // Alpha-beta pruning
            }
            return minEval;
        }
    }

    private int evaluateBoard(GameLogic game) {
        int score = 0;

        // Score based on the number of discs
        int playerDiscs = 0;
        int opponentDiscs = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Disc disc = game.getDiscAtPosition(new Position(i, j));
                if (disc != null) {
                    if (disc.getOwner().equals(this)) {
                        playerDiscs++;
                    } else {
                        opponentDiscs++;
                    }
                }
            }
        }
        score = playerDiscs - opponentDiscs;

        // Score based on the corners (corners are the most valuable positions)
        score += evaluateCorners(game);

        // Score based on the stability of the discs (edge and corner stability)
        score += evaluateStability(game);

        // Score based on the mobility (valid moves)
        score += evaluateMobility(game);

        return score;
    }

    private int evaluateCorners(GameLogic game) {
        int score = 0;
        Position[] corners = {new Position(0, 0), new Position(0, 7), new Position(7, 0), new Position(7, 7)};
        for (Position corner : corners) {
            Disc disc = game.getDiscAtPosition(corner);
            if (disc != null) {
                if (disc.getOwner().equals(this)) {
                    score += 100;  // AI gets a large bonus for controlling a corner
                } else {
                    score -= 100;  // Opponent gets a penalty for controlling a corner
                }
            }
        }
        return score;
    }

    private int evaluateStability(GameLogic game) {
        int score = 0;

        // Consider discs on the edges (but not corners) as more stable
        for (int i = 0; i < 8; i++) {
            if (game.getDiscAtPosition(new Position(i, 0)) != null) {
                score += 10;
            }
            if (game.getDiscAtPosition(new Position(i, 7)) != null) {
                score += 10;
            }
            if (game.getDiscAtPosition(new Position(0, i)) != null) {
                score += 10;
            }
            if (game.getDiscAtPosition(new Position(7, i)) != null) {
                score += 10;
            }
        }

        return score;
    }

    private int evaluateMobility(GameLogic game) {
        int score = 0;
        int aiMoves = game.ValidMoves().size();
        int opponentMoves = game.getOpponentValidMoves().size();  // Get opponent's valid moves

        score += (aiMoves - opponentMoves);
        return score;
    }
}
