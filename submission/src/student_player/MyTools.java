package student_player;
import java.util.ArrayList;
import java.util.function.UnaryOperator;
import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;
import pentago_twist.PentagoBoardState.Piece;
import java.util.AbstractMap;

public class MyTools {
  public static double getSomething() {
    return Math.random();
  }

  private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX(), c.getY()+1);
  private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX()+1, c.getY());
  private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
  private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);

  private static final int UtilityFor4 = 30;
  private static final int UtilityFor3 = 20;
  private static final int UtilityFor2 = 5;

  private static final int OppoUtilityFor4 = -1000;
  private static final int OppoUtilityFor3 = -20;
  private static final int OppoUtilityFor5 = Integer.MIN_VALUE / 2;
  // Heuristic policy: Number of 4's in a row or 3's in a row, 2's in a row.
  // Making the opponent 4 in a row is a -1000
  // Making the opponent 3 in a row is a -20
  // Making the opponent 5 in a row is a Math.Min_Value
  //
  // Making the student 5 in a row is a Math.Max_Value
  // Making the student 3 in a row in a same quard + 30 （to be considered）
  // Making the student 4 in a row is a plus. Each + 30
  // Making the student 3 in a row is a plus. Each + 20
  // Making the student 2 in a row is a plus. Each + 5
  // Placing the center is a plus. Each +2   
  public static int heuristic(PentagoBoardState boardState, int player ) {
    int utility = 0;
    if (boardState.getWinner() == player) {
      return Integer.MAX_VALUE;
    }
    if (boardState.getWinner() == 1- player) {
      return Integer.MIN_VALUE;
    }

    ArrayList<PentagoCoord> myPieces = getAllPiece(boardState, player);
    ArrayList<PentagoCoord> opponentPieces = getAllPiece(boardState, 1 - player);
    utility += InQuardantCenter(myPieces);
    utility += piecesInARow(player, myPieces, boardState);
    //System.out.println("Utility1: "+ utility);

    utility += opponentPiecesInARow(1 - player, opponentPieces, boardState);
    //System.out.println("Utility2: "+ utility);

    return utility;
  }   

  /**
   * alpha-beta pruning
   * @param depth
   * @param player
   * @param max_min true if this is a max node, false is this is a min node
   * @param boardState
   * @param alpha
   * @param beta
   * @return
   */
  public static AbstractMap.SimpleEntry<Integer, PentagoMove> a_b_pruning(int depth, int player, boolean max_min, 
      PentagoBoardState boardState, int alpha, int beta) {

    // if it is max node, initilize bestScore to min.
    // max node => max_min is true
    // min node => max_min is false
    ArrayList<PentagoMove> allMoves = boardState.getAllLegalMoves();

    int bestUtility;
    PentagoMove bestMove;
    try {
      bestMove = allMoves.get(0);
    }catch(IllegalArgumentException e) {
      bestMove = null;
    }
    
    // if it is the bottom level
    if (allMoves.isEmpty() || depth==0) {
      bestUtility = heuristic(boardState, player); //evaluate the state
      return new AbstractMap.SimpleEntry<>(bestUtility, bestMove); //return
    }
    else {
      for (PentagoMove move: allMoves){
        PentagoBoardState nextState = (PentagoBoardState)boardState.clone();
        nextState.processMove(move);   

        //        if (alpha >= beta) {
        //          break;
        //        }

        if (max_min == true) {  //max node
          //update if larger than alpha
          bestUtility = a_b_pruning(depth -1, player, !max_min, nextState, alpha, beta).getKey(); //recursive
          if(bestUtility > alpha) {
            // updating
            alpha = bestUtility;
            bestMove = move;
          }
        }
        else {  // min node
          // update if less than beta
          bestUtility = a_b_pruning(depth -1, player, !max_min, nextState, alpha, beta).getKey(); //recursive
          if (bestUtility < beta) {
            //updating
            beta = bestUtility;
            bestMove = move;
          }
        }
        
        if (alpha >= beta) {  // pruning remove the entire node
          break;
        }

      }
      return new AbstractMap.SimpleEntry<>((max_min == true) ? alpha: beta, bestMove);  // return alpha if max return beta if min
    }
  }

  /**
   * Recursive minimax with respect to depth
   * @param depth
   * @param max_min if max node it is true. if min node it is false
   * @param boardState
   * @return
   */
  // testing: depth max at 2. Timeout for depth of 3
  public static AbstractMap.SimpleEntry<Integer, PentagoMove> minimax(int depth, int player, boolean max_min, PentagoBoardState boardState) {
    ArrayList<PentagoMove> allMoves = boardState.getAllLegalMoves();
    // if it is max node, initilize bestScore to min.
    // max node => max_min is true
    // min node => max_min is false
    int bestUtility = (max_min == true) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    int currentUtility;
    PentagoMove bestMove;
    try {
      bestMove = allMoves.get(0);
    }catch(IllegalArgumentException e) {
      bestMove = null;
    }
    
    // if it is the bottom level
    if (allMoves.isEmpty() || depth == 0) {
      bestUtility = heuristic(boardState, player);  //evaluate the state

    } else {
      for (PentagoMove move: allMoves){
        PentagoBoardState tmp = (PentagoBoardState)boardState.clone();
        tmp.processMove(move);
        if (max_min == true) {
          // must invert max_min because max and min alternative
          currentUtility = minimax(depth -1, player, !max_min, tmp).getKey();
          if(currentUtility > bestUtility) { //update 
            bestUtility = currentUtility;
            bestMove = move;
          }
        }
        else {
          // for a min node, try to get the lowest utility
          currentUtility = minimax(depth -1, player, !max_min, tmp).getKey();
          if (currentUtility < bestUtility) {   //update 
            bestUtility = currentUtility;   
            bestMove = move;
          }
        }
      }
    }
    return new AbstractMap.SimpleEntry<>(bestUtility, bestMove);
  }

  public static int opponentPiecesInARow(int player,  ArrayList<PentagoCoord> allPieces, PentagoBoardState boardState) {
    int utility = 0;
    int numberOfGroupOf4 = 0;
    int numberOfGroupOf3 = 0;

    for (PentagoCoord coord : allPieces) {
      int piecesVertical    = inARow(player, coord, boardState, getNextVertical);
      int piecesHorizontal  = inARow(player, coord, boardState, getNextHorizontal); 
      int piecesDiagRight   = inARow(player, coord, boardState, getNextDiagRight); 
      int piecesDiagLeft    = inARow(player, coord, boardState, getNextDiagLeft); 

      switch(piecesVertical) {
        case 5:
          return Integer.MIN_VALUE / 2;
        case 4:
          numberOfGroupOf4++;
          break;
        case 3:
          numberOfGroupOf3++;
          break;
      }

      switch(piecesHorizontal) {
        case 5:
          return Integer.MIN_VALUE / 2;
        case 4:
          numberOfGroupOf4++;
          break;
        case 3:
          numberOfGroupOf3++;
          break;       
      }

      switch(piecesDiagRight) {
        case 5:
          return Integer.MIN_VALUE / 2;
        case 4:
          numberOfGroupOf4++;
          break;
        case 3:
          numberOfGroupOf3++;
          break;         
      }

      switch(piecesDiagLeft) {
        case 5:
          return Integer.MIN_VALUE / 2;
        case 4:
          numberOfGroupOf4++;
          break;
        case 3:
          numberOfGroupOf3++;
          break;
      } 
    }   // End of for loop
    return numberOfGroupOf4 * OppoUtilityFor4 + numberOfGroupOf3 * OppoUtilityFor3;
  }

  //Count number of groups of connected pieces
  /**
   * 
   * @param player
   * @param allPieces for a player
   * @param boardState
   * @param direction
   * @return number of utility
   */
  public static int piecesInARow(int player,  ArrayList<PentagoCoord> allPieces, PentagoBoardState boardState) {
    int utility = 0;
    int numberOfGroupOf4 = 0;
    int numberOfGroupOf3 = 0;
    int numberOfGroupOf2 = 0;

    for (PentagoCoord coord : allPieces) {
      int piecesVertical    = inARow(player, coord, boardState, getNextVertical);
      int piecesHorizontal  = inARow(player, coord, boardState, getNextHorizontal); 
      int piecesDiagRight   = inARow(player, coord, boardState, getNextDiagRight); 
      int piecesDiagLeft    = inARow(player, coord, boardState, getNextDiagLeft); 

      switch(piecesVertical) {
        case 5:
          return Integer.MAX_VALUE / 2;
        case 4:
          numberOfGroupOf4++;
          break;
        case 3:
          numberOfGroupOf3++;
          break;
        case 2:
          numberOfGroupOf2++;
          break;
      }

      switch(piecesHorizontal) {
        case 5:
          return Integer.MAX_VALUE / 2;
        case 4:
          numberOfGroupOf4++;
          break;
        case 3:
          numberOfGroupOf3++;
          break;
        case 2:
          numberOfGroupOf2++;
          break;
      }

      switch(piecesDiagRight) {
        case 5:
          return Integer.MAX_VALUE / 2;
        case 4:
          numberOfGroupOf4++;
          break;
        case 3:
          numberOfGroupOf3++;
          break;
        case 2:
          numberOfGroupOf2++;
          break;
      }

      switch(piecesDiagLeft) {
        case 5:
          return Integer.MAX_VALUE / 2;
        case 4:
          numberOfGroupOf4++;
          break;
        case 3:
          numberOfGroupOf3++;
          break;
        case 2:
          numberOfGroupOf2++;
          break;
      } 
    }   // End of for loop
    //    System.out.println("numberOfGroupOf4: "+ numberOfGroupOf4);
    //    System.out.println("numberOfGroupOf3: "+ numberOfGroupOf3);
    //    System.out.println("numberOfGroupOf2: "+ numberOfGroupOf2);
    //    System.out.println("-------------------------------------");
    return numberOfGroupOf4 * UtilityFor4 + numberOfGroupOf3 * UtilityFor3 + numberOfGroupOf2 * UtilityFor2;
  }

  // Count numerb of pieces in a row
  public static int inARow(int player,  PentagoCoord start, PentagoBoardState boardState, UnaryOperator<PentagoCoord> direction) {
    int pieces = 0;
    Piece currColour = player == 0 ? Piece.WHITE : Piece.BLACK;
    PentagoCoord current = start;
    while(true) {
      try {
        if (currColour == boardState.getPieceAt(current)) {
          pieces++;
          current = direction.apply(current);
        } else {
          break;
        }
      } catch (IllegalArgumentException e) { //We have run off the board
        break;
      }
    }
    return pieces;
  }

  //    private boolean checkWin(int player, PentagoCoord start, UnaryOperator<PentagoCoord> direction) {
  //    int winCounter = 0;
  //    Piece currColour = player == 0 ? Piece.WHITE : Piece.BLACK;
  //    PentagoCoord current = start;
  //    while(true) {
  //        try {
  //            if (currColour == this.board[current.getX()][current.getY()]) {
  //                winCounter++;
  //                current = direction.apply(current);
  //            } else {
  //                break;
  //            }
  //        } catch (IllegalArgumentException e) { //We have run off the board
  //            break;
  //        }
  //    }
  //    return winCounter >= 5;
  //}

  // Check if a piece is in center of quardant
  // 1,1
  // 1,4
  // 4,4
  // 4,1
  public static int InQuardantCenter(ArrayList<PentagoCoord> pieces) {
    int utility = 0;
    for (PentagoCoord coord : pieces) {
      if (coord.getX() == 1 && coord.getY() == 1) {
        utility += 2;
      }
      if (coord.getX() == 1 && coord.getY() == 4) {
        utility += 2;
      }
      if (coord.getX() == 4 && coord.getY() == 1) {
        utility += 2;
      }
      if (coord.getX() == 4 && coord.getY() == 4) {
        utility += 2;
      }
    }
    return utility;      
  }

  //Find the pieces of a color
  private static ArrayList<PentagoCoord> getAllPiece(PentagoBoardState boardState, int player ) {
    Piece currColour = player == 0 ? Piece.WHITE : Piece.BLACK;
    ArrayList<PentagoCoord> myPieces = new ArrayList<PentagoCoord>();
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        if (currColour ==  boardState.getPieceAt(i, j)) {
          myPieces.add(new PentagoCoord(i, j));
        }
      }
    }  
    return myPieces;
  }


  // one level min-max
  public static Move simpleStrategy(int depth, PentagoBoardState boardState) {
    ArrayList<PentagoMove> allLegalMoves = boardState.getAllLegalMoves();
    ArrayList<Integer> utilities = new ArrayList<Integer>();
    int size     = allLegalMoves.size();
    int maxIndex = 0;   // index of max utility
    for (int i = 0; i < size; i++) {
      PentagoMove move = allLegalMoves.get(i);
      PentagoBoardState tmp = (PentagoBoardState) boardState.clone();
      tmp.processMove(move);
      //System.out.println("index: "+ i);
      int utility = heuristic(tmp, boardState.getTurnPlayer());
      utilities.add(utility);
      if (utilities.get(i)>= utilities.get(maxIndex)) {
        maxIndex = i;
      }
    }
    //System.out.println("max index: "+ maxIndex);
    //System.out.println("utilities: "+ utilities.get(maxIndex));
    return allLegalMoves.get(maxIndex);
  }

  public  ArrayList<Move> filter(PentagoBoardState boardState) {
    PentagoBoardState tmp = (PentagoBoardState) boardState.clone();
    ArrayList<PentagoMove> allLegalMoves = tmp.getAllLegalMoves();

    int bestScore;

    return null;
  }


  // Check if there is a wining move.
  public static PentagoMove getWinningMove(PentagoBoardState boardState) {
    ArrayList<PentagoMove> allLegalMoves = boardState.getAllLegalMoves();
    for(PentagoMove move : allLegalMoves) {
      PentagoBoardState tmp = (PentagoBoardState) boardState.clone();
      tmp.processMove(move);
      if (tmp.getWinner() == boardState.getTurnPlayer()) { // this is a winning move
        return move;  
      }
    }
    return null;
  }

  
//  public static boolean quardClear(int quard) {
//    switch(quard) {
//      case 0:
//        
//        break;
//      case 1:
//        
//      break;
//      
//      case 2:
//        
//        break;
//      case 3:
//        
//        break;
//      defalut:
//        return true;
//    }
//  }
  //
  public static PentagoMove firstTurn(PentagoBoardState boardState, int player) {
    if (boardState.getPieceAt(1, 1) == Piece.EMPTY) {
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          
        }
      }
      return new PentagoMove(1,1,0,0,player);
    } else if (boardState.getPieceAt(1, 4) == Piece.EMPTY) {
      return new PentagoMove(1,4,1,0,player);
    } else if (boardState.getPieceAt(4, 1) == Piece.EMPTY) {
      return new PentagoMove(4,1,2,0,player);
    } else if (boardState.getPieceAt(4, 4) == Piece.EMPTY){
      return new PentagoMove(4,4,3,0,player);
    }
    return null;
  }


}
