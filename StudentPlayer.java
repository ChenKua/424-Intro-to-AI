package student_player;

import boardgame.Move;

import pentago_twist.PentagoPlayer;
import pentago_twist.PentagoBoardState;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260856888");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
        // MyTools.getSomething();
        long start = System.currentTimeMillis();
        
        
        // Is random the best you can do?
        Move myMove = boardState.getRandomMove();
        
        // If there is a wining move
        Move winningMove = MyTools.getWinningMove(boardState);    
        // if no winning move, use a-b-pruning
        if (winningMove != null) {
          return winningMove;
        }
        
        // if first term
//        if (boardState.getTurnNumber() == 0) {
//          Move best = MyTools.firstTurn(boardState, boardState.getTurnPlayer());
//          return best;
//        }
        
        //Move bestMove = MyTools.simpleStrategy(boardState.getTurnPlayer(), boardState);
        
        //max depth 2
        //Move nextMove = MyTools.minimax(2, boardState.getTurnPlayer(), true, boardState).getValue();
        
        Move abpMove = MyTools.a_b_pruning(2, boardState.getTurnPlayer(), true, boardState, Integer.MIN_VALUE, Integer.MAX_VALUE).getValue();
        float timeElapsed = (System.currentTimeMillis() - start) / 1000f;
        System.out.println(String.format("Time consumed for Move (s): %f", timeElapsed));
        System.out.println("   ");
        // Return your move to be processed by the server.
        return abpMove;
    }
}