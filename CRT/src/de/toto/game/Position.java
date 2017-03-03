package de.toto.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.toto.game.Piece;
import de.toto.game.Piece.PieceType;

public class Position {
	
	private static Logger log = Logger.getLogger("Position");
	
	private int dbId;
	private Square[][] squares;	
	private Position previous = null;
	private List<Position> next = new ArrayList<Position>(); 
	private int variationLevel = 0; //0 = main line, 1 = variation, 2 = variation of variation ...
	private String fen = null;
	private String move = null; //in Long Algebraic Notation, or "" for the starting position or "--" for a null move  
	private String comment = null; // may contain graphics comments such as [%csl Ge5][%cal Ge5b2]
	private List<String> nags = new ArrayList<String>(); // !, ?, ?? ...
	
	private static final Pattern GRAPHICS_COMMENT_PATTERN = Pattern.compile("\\[(.*?)\\]");
	
	public static final String FEN_STARTPOSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
		
	public static final Position EMPTY_BOARD = new Position(null, "", "8/8/8/8/8/8/8/8 w KQkq - 0 1");
	
	// Startposition
	public Position() {		
		setFen(FEN_STARTPOSITION, true);
		move = "";
	}

	public Position(Position previous, String move) {
		this(previous, move, null);
	}
	
	public Position(Position previous, String move, String fen) {
		this(previous, move, fen, false);
	}
	
	
	public Position(Position previous, String move, String fen, boolean asVariation) {
		this(previous, move, fen, asVariation, true);
	}
	
	public Position(Position previous, String move, String fen, boolean asVariation, boolean checkMateOrCheck) {
		this.previous = previous;			
		if (previous != null) {
			this.variationLevel = asVariation ? previous.variationLevel+1 : previous.variationLevel;
		}
		if (fen != null) {			
			setFen(fen, true);
			setMove(move, false, false);
		} else if (move != null) {
			setMove(move, true, checkMateOrCheck);
		}
		if (previous != null) {
			previous.addNextPosition(this, asVariation);
		}
	}	
				
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {		
		if (!(obj instanceof Position)) return false;
		return this.hashCode() == obj.hashCode();
	}
	
	public boolean isSamePositionAs(Position other) {
		return fen != null ? fen.equals(other.fen) : equals(other);
	}
	
	public boolean isStartPosition() {
		return FEN_STARTPOSITION.equals(fen) ;
	}

	public String getComment() {
		return comment;
	}
	
	public String getCommentText() {
		return comment != null ?  GRAPHICS_COMMENT_PATTERN.matcher(comment).replaceAll("") : null;
	}
	
	public List<GraphicsComment> getGraphicsComments() {
		List<GraphicsComment> result = new ArrayList<GraphicsComment>();
		if (comment != null && !comment.isEmpty()) {			
			Matcher matcher = GRAPHICS_COMMENT_PATTERN.matcher(comment);
			while (matcher.find()) {				
				String graphicsComment = matcher.group(1);
				if (graphicsComment.startsWith("%csl")) {
					graphicsComment = graphicsComment.split(" ")[1];
					for (String squareHighlight : graphicsComment.split(",")) {
						Color c = null;
						switch (squareHighlight.charAt(0)) {
						case 'G' : c = Color.GREEN; break;
						case 'R' : c = Color.RED; break;
						case 'Y' : c = Color.YELLOW; break;
						}
						Square square = getSquare(squareHighlight.substring(1));
						result.add(new GraphicsComment(square, null, c));
					}
				} else if (graphicsComment.startsWith("%cal")) {
					graphicsComment = graphicsComment.split(" ")[1];
					for (String arrow : graphicsComment.split(",")) {
						Color c = null;
						switch (arrow.charAt(0)) {
						case 'G' : c = Color.GREEN; break;
						case 'R' : c = Color.RED; break;
						case 'Y' : c = Color.YELLOW; break;
						}
						Square squareFrom = getSquare(arrow.substring(1,3));
						Square squareTo = getSquare(arrow.substring(3,5));
						result.add(new GraphicsComment(squareFrom, squareTo, c));
					}
				} //TODO add other like Scid / Scid vs PC ...
			}
		}
		return result;
	}
		

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public void addNag(String value) {
		nags.add(value);
	}
	
	public String getNagsAsString() {
		if (nags.isEmpty()) return "";
		StringBuilder result = new StringBuilder();
		for (String nag : nags) {
			result.append(NAG.getByNag(nag).toString());
		}
		return result.toString();
	}
	
	public String getPositionNagsAsString() {
		if (nags.isEmpty()) return "";
		StringBuilder result = new StringBuilder();
		for (String nag : nags) {
			NAG nAG = NAG.getByNag(nag);
			if (nAG.isPositionEval()) {
				result.append(nAG.toString());
			}
		}
		return result.toString();
	}

	public int getVariationLevel() {
		return variationLevel;
	}
	
	private void setVariationLevel(int level) {
		variationLevel = level;
		for (int i = 0; i < next.size(); i++) {
			int newChildLevel = i == 0 ? level : level+1;
			next.get(i).setVariationLevel(newChildLevel);
		}
	}
	
	public Position getPrevious() {
		return previous;
	}
	
	void setPrevious(Position newPrevious) {
		previous = newPrevious;
	}
	
	public void addVariation(Position variation) {
		boolean asVariation = hasNext();
		addNextPosition(variation, asVariation);
		variation.setPrevious(this);
		variation.setVariationLevel(asVariation ? this.variationLevel+1 : this.variationLevel);		
	}
	
	public boolean hasPrevious() {
		return previous != null;
	}

	public Position getNext() {
		return next.get(0);
	}
	
	public boolean hasNext() {
		return !next.isEmpty();
	}
	
	public int getVariationCount() {
		return next.isEmpty() ? 0 : next.size() - 1;
	}
	
	public boolean hasVariations() {
		return getVariationCount() > 0;
	}	
	
	public List<Position> getVariations() {
		return next;
	}
	
	public boolean hasVariation(Position variation) {
		return getVariation(variation) != null;
	}
	
	public Position getVariation(Position variation) {
		for (Position aVariation : next) {
			if (aVariation.isSamePositionAs(variation)) return aVariation;
		}
		return null;
	}
	
	public String getFen() {
		return fen;
	}

	/**
	 * 
	 * @return the move that led to this Position as LAN
	 */
	public String getMove() {
		return move;
	}
	
	/**
	 * 
	 * @return the move that led to this Position as SAN
	 */
	public String getMoveAsSan() {
		if (move == null || "".equals(move) || "--".equals(move)) return "";
		if (move.startsWith("0-0")) return move;		
		StringBuilder result = new StringBuilder();
		String[] moveParts = move.split(wasCapture() ? "x" : "-");
		Position previous = getPrevious();
		Square from = previous.getSquare(moveParts[0].substring(moveParts[0].length()-2, moveParts[0].length()));
		Square to = previous.getSquare(moveParts[1].substring(0, 2));
		Piece piece = from.piece;
		
		if (piece != null && piece.type != PieceType.PAWN) {
			result.append(piece.pgnChar);
		}
		if (wasCapture() && piece.type == PieceType.PAWN) {
			result.append(from.getFileName());	
		}
		if (piece.type != PieceType.PAWN && piece.type != PieceType.KING) {
			List<Square> squares = previous.getAllPiecesByColor(whiteMoved(), piece.type);
			if (squares.size() > 1) {
				//check if more than one Piece of this PieceType could have moved to "to"
				List<Square> possible = new ArrayList<Square>();
				for (Square s : squares) {
					if (s.attacks(to, previous, null)) {
						possible.add(s);
					}
				}
				if (possible.size() > 1) {
					Map<Integer, Integer> _ranks = new HashMap<Integer, Integer>();
					Map<Integer, Integer> _files = new HashMap<Integer, Integer>();					
					for (Square s : possible) {
						Integer _rank = new Integer(s.rank);
						if (_ranks.containsKey(_rank)) {
							_ranks.put(_rank, _ranks.get((int)s.rank).intValue()+1);
						} else {
							_ranks.put(_rank, 1);
						}
						Integer _file = new Integer(s.file);
						if (_files.containsKey(_file)) {
							_files.put(_file, _files.get((int)s.file).intValue()+1);
						} else {
							_files.put(_file, 1);
						}
					}
					boolean distinctRanks = true;
					for (int rankCount : _ranks.values()) {
						distinctRanks &= rankCount == 1;
					}
					boolean distinctFiles = true;
					for (int fileCount : _files.values()) {
						distinctFiles &= fileCount == 1;
					}
					if (distinctFiles) {
						result.append(from.getFileName());
					} else if (distinctRanks) {
						result.append(from.rank);
					} else {
						result.append(from.getName());
					}					
				}
			}			
		}
		if (wasCapture()) {
			result.append("x");
		}
		result.append(moveParts[1]);
		return result.toString();
	}
	
	/**
	 * 
	 * @return the move including possible NAGs
	 */
	public String getMoveNotation(boolean leadingMoveNumber) {
		String result = getMoveAsSan() + getNagsAsString();
		if (leadingMoveNumber) {
			result = getMoveNumber() + (whiteMoved() ? "." : "...") + result;
		}
		return result;
	}
	
	/**
	 * 
	 * @return the move in UCI engine syntax, i.e. only start square followed by target square and possibly a promotion piece
	 */
	public String getMoveAsEngineMove() {
		if (move == null || "".equals(move) || "--".equals(move)) return null;
		if (move.startsWith("0-0-0")) {
			return whiteMoved() ? "e1c1" : "e8c8";
		} else if (move.startsWith("0-0")) {
			return whiteMoved() ? "e1g1" : "e8g8";
		} else {
			Square from = null;
			Square to = null;
			String[] moveParts = move.split(wasCapture() ? "x" : "-");
			from = getSquare(moveParts[0].substring(moveParts[0].length()-2, moveParts[0].length()));
			to = getSquare(moveParts[1].substring(0, 2));
			return from.getName() + to.getName() + (wasPromotion() ? String.valueOf(getPromotionPiece().fenChar).toLowerCase() : "");
		}	
	}

	/**
	 * 
	 * @return true, if White is to move to reach the next Position
	 */
	public boolean isWhiteToMove() {
		if (fen != null) {
			return "w".equals(fen.split(" ")[1]);
		} else if (previous != null) {
			return !previous.isWhiteToMove();
		} else {
			return true;
		}
	}
	
	/**
	 * 
	 * @return true, if White moved to reach this Position
	 */
	public boolean whiteMoved() {
		return !isWhiteToMove();
	}

	public Square[][] getSquares() {
		return squares;
	}

	public boolean wasPawnMove() {
		return  move != null && move.charAt(0) >= 'a' && move.charAt(0) <= 'h';		
	}
	
	public boolean wasCapture() {
		return move != null && move.contains("x");
	}
	
	public boolean wasCastling() {
		return move != null && move.startsWith("0-0");
	}
	
	private boolean wasKingMove() {
		return move != null && move.startsWith("K");
	}
	
	private boolean wasRookMove() {
		return move != null && move.startsWith("R");
	}
	
	public boolean wasPromotion() {
		return move != null && move.contains("=");
	}
	
	private Piece getPromotionPiece() {
		if (wasPromotion()) {
			boolean checkOrMate = isCheck() || isMate();
			int promotionPiecePosition = checkOrMate ? move.length() - 2 : move.length() - 1;
			String promotionPiece = move.substring(promotionPiecePosition, move.length());
			if (!whiteMoved()) promotionPiece = promotionPiece.toLowerCase();
			return Piece.getByFenChar(promotionPiece.charAt(0));			
		}		
		return null;
	}
	
	public boolean isCheck() {
		return move != null && move.endsWith("+");
	}
	
	private boolean checkIfCheck() {		
		return findKing(!whiteMoved()).isAttacked(this);
	}
	
	public boolean isMate() {
		return move != null && move.endsWith("#");
	}
	
	public int getMoveNumber() {		
		return previous != null ? Integer.parseInt(previous.getFen().split(" ")[5]) : 0;
		
	}
		
	private void addNextPosition(Position nextPosition, boolean asVariation) {
		if (!next.contains(nextPosition)) {
			next.add(asVariation ? next.size() : 0, nextPosition);
		} else {
			log.warning(String.format("ignoring duplicate variation %s for position %s", nextPosition, this));
		}
	}
	
	public void removeNextPosition(Position nextPosition) {
		next.remove(nextPosition);
	}
		
	private void initSquares() {
		squares = Square.createEmpty8x8();
	}
	
	public Square getSquare(int rank, int file) {
		return squares[rank - 1][file - 1];
	}

	// e.g. "a1"
	public Square getSquare(String squarename) {
		int file = Square.filenumberForName(squarename);
		int rank = Character.getNumericValue(squarename.charAt(1));
		return getSquare(rank, file);
	}
	
	private void setFen(String fen, boolean setupPosition) {
		this.fen = fen;
		if (setupPosition) {
			try {
				String[] fenFields = fen.split(" ");
				initSquares();
				int rank = 8;
				int file = 1;			
				for (int i = 0; i < fenFields[0].length(); i++) {
					char fenChar = fen.charAt(i);			
					if ('/' == fenChar) {
						rank--;
						file = 1;
						continue;
					}
					int numericValue = Character.getNumericValue(fenChar);
					if (numericValue > 0 && numericValue <= 8) {
						file += numericValue;
						continue;
					}
					Piece piece = Piece.getByFenChar(fenChar);
					if (piece != null) {
						getSquare(rank, file).piece = piece;
						file++;
					} else {
						throw new IllegalArgumentException("failed to parse FEN: " + fen);
					}				
				}	
			} catch (Exception ex) {
				throw new IllegalArgumentException("failed to parse FEN: " + fen, ex);
			}
		}
	}

	private void createFen() {
		if (this.fen != null) return;
		StringBuilder fen = new StringBuilder();
		for (int rank = 8; rank >= 1; rank--) {
			int emptySquareCounter = 0;
			for (int file = 1; file <= 8; file++) {
				Square s = getSquare(rank, file);
				if (s.piece != null) {
					if (emptySquareCounter > 0) fen.append(emptySquareCounter);
					emptySquareCounter = 0;
					fen.append(s.piece.fenChar);
				} else emptySquareCounter++;
			}
			if (emptySquareCounter > 0) fen.append(emptySquareCounter);
			if (rank > 1) fen.append("/");	
		}
		String[] previousFenFields = previous != null ? previous.getFen().split(" ") : null;
		// move field
		fen.append(" ");
		boolean whiteToMove = isWhiteToMove();
		fen.append(whiteToMove ? "w" : "b");		
		// Castle field
		String castleField = previousFenFields != null ? previousFenFields[2] : "KQkq";		
		boolean couldCastle = castleField.contains(whiteToMove ? "k" : "K") || castleField.contains(whiteToMove ? "q" : "Q");		 
		if (couldCastle) {
			String regex = "K|Q";
			if (whiteToMove) regex = regex.toLowerCase();
			if (wasCastling() || wasKingMove()) {			
				castleField = castleField.replaceAll(regex, "");			
			} else if (wasRookMove()) {				
				int rank = whiteToMove ? 8 : 1;
				String kOrQ = null;
				if (getMoveSquareNames()[0].equals("a"+rank)) {
					kOrQ = "Q";
				} else if (getMoveSquareNames()[0].equals("h"+rank)) {
					kOrQ = "K";
				}
				if (kOrQ != null) {
					if (whiteToMove) kOrQ = kOrQ.toLowerCase();
					castleField = castleField.replaceAll(kOrQ, "");
				}
			}
		}
		//was an enemy rook captured?
		int enemyRank = whiteToMove ? 1 : 8;
		Piece enemyRook = whiteToMove ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
		if (castleField.contains(whiteToMove ? "K" : "k") && getSquare(enemyRank, 8).piece != enemyRook) {
			castleField = castleField.replaceAll(whiteToMove ? "K" : "k", "");
		}
		if (castleField.contains(whiteToMove ? "Q" : "q") && getSquare(enemyRank, 1).piece != enemyRook) {
			castleField = castleField.replaceAll(whiteToMove ? "Q" : "q", "");
		}
		if (castleField.length() == 0) castleField = "-";
		fen.append(" ").append(castleField);
		// En passant square field
		fen.append(" ").append(getEnPassantFenField());
		// Halfmove clock field
		int halfmoveClock = previousFenFields != null ? Integer.parseInt(previousFenFields[4]) : 1;
		if (!wasCapture() && !wasPawnMove()) {
			halfmoveClock++;			
		} else {
			halfmoveClock = 0;
		}
		fen.append(" ").append(halfmoveClock);
		// Fullmove number field		
		int moveNumber = previousFenFields != null ? Integer.parseInt(previousFenFields[5]) : 1;
		if (whiteToMove && previous != null) moveNumber++;
		fen.append(" ").append(moveNumber);
		this.fen = fen.toString();
	}
		
	private String getEnPassantFenField() {
		// an en passant move must be something like "d2-d4"
		String[] m = move.replaceAll("\\+|#", "").split("-");
		try {
			Square from = getSquare(m[0]);
			Square to = getSquare(m[1]);
			if (from.isEnPassantPossible(to, this)) {
				return getSquare((whiteMoved() ? from.rank + 1 : from.rank -1), from.file).getName();
			}			
		} catch (Exception ignore) {}
		return "-";
		
	}
	
	// move in LAN, e.g. "Ng1-f3"
	private void setMove(String move, boolean setupPosition, boolean checkMateOrCheck) {
		// ChessBase PGNs contain "O-O" rather than "0-0"...
		move = move.replaceAll("O-O-O", "0-0-0").replaceAll("O-O", "0-0");
		if (setupPosition) {
			initSquares();
			for (int rank = 1; rank <= 8; rank++) {
				for (int file = 1; file <= 8; file++) {
					squares[rank - 1][file - 1].piece = previous.getSquare(rank, file).piece;
				}
			}
			if ("--".equals(move)) { // handle null move
				this.move = move;
				createFen();
				return;
			}
			if (!isLanMove(move)) {
				move = sanToLan(move, whiteMoved());
			}
			this.move = move.trim();
			if (move != null) {
				if (wasCastling()) {
					String[] castlingSquareNames = getCastlingSquareNames();				
					getSquare(castlingSquareNames[0]).piece = null;
					getSquare(castlingSquareNames[1]).piece = previous.getSquare(castlingSquareNames[3]).piece;
					getSquare(castlingSquareNames[2]).piece = previous.getSquare(castlingSquareNames[0]).piece;
					getSquare(castlingSquareNames[3]).piece = null;		
				} else {
					String[] m = move.split("x|-");
					String from = m[0];
					if (from.length() > 2) {
						from = from.substring(from.length()-2, from.length());
					}
					Square fromSquare = getSquare(from);
					Square toSquare = getSquare(m[1]);
					Piece piece = fromSquare.piece;
					fromSquare.piece = null;
					// En Passant?
					if (wasCapture() && piece.type == PieceType.PAWN) {
						if (toSquare.piece == null) {
							getSquare(whiteMoved() ? toSquare.rank-1 : toSquare.rank+1, toSquare.file).piece = null;
						}
					}
					if (wasPromotion()) {
						toSquare.piece = getPromotionPiece();
					} else {
						toSquare.piece = piece;
					}					
				}
				if (checkMateOrCheck) {
					createFen(); //getPossiblePositions() needs the FEN...
					// check or mate?  
					if (!this.move.endsWith("#") && !this.move.endsWith("+")) {
						if (checkIfCheck()) {
							this.move += getPossiblePositions().isEmpty() ? "#" : "+";
						}
					}
				}
			}			
			createFen();
		}
	}
	
	private boolean isLanMove(String move) {
		if (move.startsWith("0-0")) return true;
		String[] m = move.split("x|-");
		if (m.length < 2) return false;
		if (m[0].length() == 3) return true;
		return m[0].length() == 2 && Character.isDigit(m[0].charAt(1)) && m[0].charAt(0) >= 'a' && m[0].charAt(0) <= 'h';
	}
	
	public String[] getMoveSquareNames() {
		String[] result = null;
		if (move != null && !move.isEmpty() && !("--".equals(move))) {
			if (wasCastling()) {
				String[] castlingSquareNames = getCastlingSquareNames();
				result = new String[2];
				result[0] = castlingSquareNames[0];
				result[1] = castlingSquareNames[2];
			} else {
				result = move.split("x|-");		
				if (result[0].length() > 2) { //e.g. "Ng1"
					result[0] = result[0].substring(result[0].length()-2, result[0].length());
				}
				if (result[1].length() > 2) { //e.g. "d8=Q"
					result[1] = result[1].substring(0, 2);
				}
			}
		}	
		return result;
	}
	
	/*
	 * result[0] = king position before castling, e.g. "e1" 
	 * result[1] = rook position after castling, e.g. "f1"
	 * result[2] = king position after castling, e.g. "g1"
	 * result[3] = rook position before castling, e.g. "h1"
	 * 
	 */
	private String[] getCastlingSquareNames() {
		String[] result = new String[4];
		int rank = isWhiteToMove() ? 8 : 1;
		boolean longCastles = move.startsWith("0-0-0");
		result[0] = "e" + rank;
		result[1] = (longCastles ? "d" : "f") + rank;
		result[2] = (longCastles ? "c" : "g") + rank;
		result[3] = (longCastles ? "a" : "h") + rank;		
		return result;
	}
	
	public String[] getPossibleCastlingSquareNames() {
		String[] result = new String[2];
		int rank = isWhiteToMove() ? 1 : 8;
		String castleFenField = getFen().split(" ")[2];
		if (castleFenField.contains(isWhiteToMove() ? "K" : "k")) result[0] = "g" + rank;
		if (castleFenField.contains(isWhiteToMove() ? "Q" : "q")) result[1] = "c" + rank;
		return result;
	}
	
	@Override
	public String toString() {
		return getMoveNotation(true);
	}
		
	// construct a LAN move out of a SAN for this position 
	private String sanToLan(String san, boolean whiteMoved) {
		if (san.startsWith("0-0")) return san;
		String _san = san;
		// strip potential '+', '#' and promotion info
		_san = _san.replaceAll("\\+|#", "");		
		if (_san.charAt(_san.length()-2) == '=') {
			_san = _san.substring(0, _san.length()-2);
		}
		int suffixLength = san.length() - _san.length();
		String suffix = san.substring(san.length() - suffixLength, san.length());
		// get target square and strip from san
		Square targetSquare = getSquare(_san.substring(_san.length()-2, _san.length()));
		_san = _san.substring(0, _san.length()-2);
		// get Piece to move and strip from san		
		Piece piece = whiteMoved ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
		if (_san.length() > 0) {
			switch (_san.charAt(0)) {
				case 'K' : piece = whiteMoved ? Piece.WHITE_KING : Piece.BLACK_KING; break;
				case 'Q' : piece = whiteMoved ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN; break;
				case 'R' : piece = whiteMoved ? Piece.WHITE_ROOK : Piece.BLACK_ROOK; break;
				case 'B' : piece = whiteMoved ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP; break;
				case 'N' : piece = whiteMoved ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT; break;
			}
		}
		if (piece.type != PieceType.PAWN) {
			_san = _san.substring(1, _san.length());
		}
		// strip potential 'x'
		boolean isCapture = _san.contains("x");
		_san = _san.replace("x", "");
		// parse potential source square info 
		List<Square> potentialMatches = null;
		if (_san.length() == 2) {
			return buildLanMove(getSquare(_san), targetSquare, isCapture, suffix);
		} else if (_san.length() == 1) {
			if (Character.isDigit(_san.charAt(0))) {
				potentialMatches = getSquaresWithPieceOnRank(piece, Integer.parseInt(_san));
			} else {
				potentialMatches = getSquaresWithPieceOnFile(piece,  Square.filenumberForName(_san));
			}
		} else if (_san.length() == 0) {
			potentialMatches = getSquaresWithPiece(piece);
		} else throw new IllegalArgumentException("failed to parse SAN: " + san);
		
		
		if (potentialMatches.size() == 1) {
			Square match = potentialMatches.get(0);
			if (!match.attacks(targetSquare, this, null)) throw new IllegalArgumentException("failed to parse SAN: " + san);
			return buildLanMove(match, targetSquare, isCapture, suffix);
		} else {
			for (Square s : potentialMatches) {
				if (s.attacks(targetSquare, this, null)) return buildLanMove(s, targetSquare, isCapture, suffix);
			}			
		}
		throw new IllegalArgumentException("failed to parse SAN: " + san);		
	}
	
	private String buildLanMove(Square from, Square to, boolean isCapture, String suffix) {
		return (from.getNameWithPieceSuffix() + (isCapture ? "x" : "-") + to.getName() + suffix).trim();
	}
	
	public List<Square> getSquaresWithPiecesByColor(boolean white) {
		List<Square> matchingSquares = new ArrayList<Square>();
		for (int file = 1; file <= 8; file++) {
			for (int rank = 1; rank <= 8; rank++) {			
				Square s = squares[rank - 1][file - 1]; 
				if (s.piece != null && s.piece.isWhite == white) {
					matchingSquares.add(squares[rank - 1][file - 1]);
				}
			}
		}
		return matchingSquares;
	}
	
	private List<Square> getSquaresWithPiece(Piece piece) {
		List<Square> matchingSquares = new ArrayList<Square>();
		for (int file = 1; file <= 8; file++) {
			for (int rank = 1; rank <= 8; rank++) {			
				if (squares[rank - 1][file - 1].piece == piece) {
					matchingSquares.add(squares[rank - 1][file - 1]);
				}
			}
		}
		return matchingSquares;
	}
	
	private List<Square> getSquaresWithPieceOnRank(Piece piece, int rank) {
		List<Square> matchingSquares = new ArrayList<Square>();		
		for (int file = 1; file <= 8; file++) {
			if (squares[rank - 1][file - 1].piece == piece) {
				matchingSquares.add(squares[rank - 1][file - 1]);
			}
		}		
		return matchingSquares;
	}
	
	private List<Square> getSquaresWithPieceOnFile(Piece piece, int file) {
		List<Square> matchingSquares = new ArrayList<Square>();		
		for (int rank = 1; rank <= 8; rank++) {
			if (squares[rank - 1][file - 1].piece == piece) {
				matchingSquares.add(squares[rank - 1][file - 1]);
			}
		}		
		return matchingSquares;
	}
	
	public Square findOurKing() {
		return findKing(whiteMoved());
	}
		
	public boolean isLegal() {
		// if our king is (still) in check after we moved it's illegal
		if (findKing(whiteMoved()).isAttacked(this)) return false;
		
		// TODO what else?
		return true;
	}
	
	/**
	 * 
	 * @return the positions after all legal moves in this position
	 */
	public List<Position> getPossiblePositions() {
		List<Position> result = new ArrayList<Position>();
		for (Square pieces : getAllPiecesByColor(isWhiteToMove())) {
			for (Square s : pieces.getPossibleTargetSquares(this)) {
				boolean isPromotion = pieces.piece.type == PieceType.PAWN && (s.rank == 1 || s.rank == 8);
				String move = pieces.getName() + s.getName() + (isPromotion ? "q" : "");
				move = translateMove(move);
				Position p = new Position(this, move, null, false, false);
				if (p.isLegal()) {
					result.add(p);
				}
				next.remove(p);
			}
		}
		return result;
	}
	
	public boolean isPossibleMove(String lan) {		
		if (!isLanMove(lan)) {
			lan = sanToLan(lan, isWhiteToMove());
		}		 
		for (Position p : getPossiblePositions()) {
			if (p.getMove().startsWith(lan)) return true;
		}
		return false;
	}
	
	private List<Square> getAllPiecesByColor(boolean white) {
		return getAllPiecesByColor(white, (PieceType[])null);
	}
	
	private List<Square> getAllPiecesByColor(boolean white, PieceType... types) {
		List<Square> result = new ArrayList<Square>(); 
		List<PieceType> _types = types != null ? Arrays.asList(types) : Arrays.asList(PieceType.values());
		for (int _rank = 1; _rank <= 8; _rank++) {
			for (int _file = 1; _file <= 8; _file++) {
				Square s = squares[_rank - 1][_file - 1];
				if (s.piece != null && s.piece.isWhite == white && _types.contains(s.piece.type)) {
					result.add(s);
				}
			}
		}
		return result;
	}
	
	public Square findKing(boolean white) {
		return getAllPiecesByColor(white, PieceType.KING).get(0);			
	}
	
	// translates an engine move like "g1f3" into a LAN move like "Ng1xf3"
	public String translateMove(String engineMove) {
		Square from = getSquare(engineMove.substring(0,2));
		Square to = getSquare(engineMove.substring(2,4));
		StringBuilder result = new StringBuilder();
		if (from.piece == null) return null; 
		// Consider castling 
		if (from.piece.type == PieceType.KING && (to.file == from.file + 2 || to.file == from.file - 2)) {
			if (to.file == 7) return "0-0";
			if (to.file == 3) return "0-0-0";
		}
		result.append(from.getNameWithPieceSuffix());
		if (to.piece != null) {
			result.append("x");
		} else if (from.piece.type == PieceType.PAWN && from.file != to.file) {
			// Consider en passant
			result.append("x");
		} else {
			result.append("-");
		}
		result.append(to.getName());
		// Consider promotion
		if (engineMove.length() == 5) {
			result.append("=").append(engineMove.substring(4,5).toUpperCase());
		}
		return result.toString().trim();
	}
	
	/**
	 * Describes this Position as a human would do it.
	 */
	public String describe() {
		StringBuilder result = new StringBuilder();
		
		String castleFenField = getFen().split(" ")[2];
		boolean castleLong = false, castleShort = false;
	    	
    	// White
		result.append("White");
		castleLong = castleFenField.contains("Q");
		castleShort = castleFenField.contains("K");		
		if (castleLong || castleShort) {
			result.append(" [");
			if (castleShort) {
				result.append("0-0").append(castleLong ? ", " : "");
			}
			if (castleLong) {
				result.append("0-0-0");
			}
			result.append("] ");
		}
		result.append("\n");
		// White pawns 
		if (describe(getSquaresWithPiecesByColor(true, PieceType.PAWN), result)) {
			result.append("\n");
		};		
		// White pieces 
		if (describe(getSquaresWithPiecesByColor(true, PieceType.KING), result)) {
			result.append(", ");
		};
		if (describe(getSquaresWithPiecesByColor(true, PieceType.QUEEN), result)) {
			result.append(", ");
		};
		if (describe(getSquaresWithPiecesByColor(true, PieceType.ROOK), result)) {
			result.append(", ");
		};
		if (describe(getSquaresWithPiecesByColor(true, PieceType.BISHOP), result)) {
			result.append(", ");
		};
		if (describe(getSquaresWithPiecesByColor(true, PieceType.KNIGHT), result)) {
			result.append(", ");
		};
		if (result.charAt(result.length()-2) == ',') {
			result.setLength(result.length()-2);
    	}
		result.append("\n");		
		
    	// Black
		result.append("\n");
		result.append("Black");
		castleLong = castleFenField.contains("q");
		castleShort = castleFenField.contains("k");		
		if (castleLong || castleShort) {
			result.append(" [");
			if (castleShort) {
				result.append("0-0").append(castleLong ? ", " : "");
			}
			if (castleLong) {
				result.append("0-0-0");
			}
			result.append("] ");
		}
		result.append("\n");
		// Black pawns 
		if (describe(getSquaresWithPiecesByColor(false, PieceType.PAWN), result)) {
			result.append("\n");
		};		
		// Black pieces 
		if (describe(getSquaresWithPiecesByColor(false, PieceType.KING), result)) {
			result.append(", ");
		};
		if (describe(getSquaresWithPiecesByColor(false, PieceType.QUEEN), result)) {
			result.append(", ");
		};
		if (describe(getSquaresWithPiecesByColor(false, PieceType.ROOK), result)) {
			result.append(", ");
		};
		if (describe(getSquaresWithPiecesByColor(false, PieceType.BISHOP), result)) {
			result.append(", ");
		};
		if (describe(getSquaresWithPiecesByColor(false, PieceType.KNIGHT), result)) {
			result.append(", ");
		};
		if (result.charAt(result.length()-2) == ',') {
			result.setLength(result.length()-2);
    	}
		result.append("\n");

		// TODO Castle rights...
    	
    	// Who's to move?
		result.append("\n");
    	result.append(isWhiteToMove() ? "White" : "Black").append(" to move!\n\n");
    	
    	return result.toString();
	
	}
	
	public List<Piece> getMaterialImbalance(boolean whiteFirst) {
		List<Piece> result = new ArrayList<Piece>();		
		int imbalance = getMaterialImbalanceFor(PieceType.PAWN);
		for (int i = 0; i < Math.abs(imbalance); i++) {
			result.add(imbalance > 0 ? Piece.BLACK_PAWN : Piece.WHITE_PAWN);
		}
		imbalance = getMaterialImbalanceFor(PieceType.KNIGHT);
		for (int i = 0; i < Math.abs(imbalance); i++) {
			result.add(imbalance > 0 ? Piece.BLACK_KNIGHT : Piece.WHITE_KNIGHT);
		}
		imbalance = getMaterialImbalanceFor(PieceType.BISHOP);
		for (int i = 0; i < Math.abs(imbalance); i++) {
			result.add(imbalance > 0 ? Piece.BLACK_BISHOP : Piece.WHITE_BISHOP);
		}
		imbalance = getMaterialImbalanceFor(PieceType.ROOK);
		for (int i = 0; i < Math.abs(imbalance); i++) {
			result.add(imbalance > 0 ? Piece.BLACK_ROOK : Piece.WHITE_ROOK);
		}
		imbalance = getMaterialImbalanceFor(PieceType.QUEEN);
		for (int i = 0; i < Math.abs(imbalance); i++) {
			result.add(imbalance > 0 ? Piece.BLACK_QUEEN : Piece.WHITE_QUEEN);
		}
		result.sort(new MaterialImbalancePieceComparator(whiteFirst));
		return result; 
	}
	
	private static class MaterialImbalancePieceComparator implements Comparator<Piece> {

		private boolean whiteFirst;
		
		public MaterialImbalancePieceComparator(boolean whiteFirst) {
			this.whiteFirst = whiteFirst;
		}
		
		@Override
		public int compare(Piece p1, Piece p2) {
			if (p1.isWhite != p2.isWhite) return p1.isWhite && whiteFirst ? 1 : -1;
			return p1.type.compareTo(p2.type);
		}
		
	}
	
	// positive = white has a plus, negative = black has a plus;
	private int getMaterialImbalanceFor(PieceType pieceType) {
		List<Square> white, black;
		white = getAllPiecesByColor(true, pieceType);
		black = getAllPiecesByColor(false, pieceType);
		return white.size() - black.size();
	}
	
	private boolean describe(List<Square> pieces, StringBuilder string) {
		boolean result = false;
		for (Square s : pieces) {
			result = true; 
    		string.append(s.getNameWithPieceSuffix()).append(", ");
    	}
    	if (result) {
    		string.setLength(string.length()-2);
    	}
    	return result;
	}
	    
    private List<Square> getSquaresWithPiecesByColor(boolean white, PieceType... pieceTypes) {
    	List<Square> result = new ArrayList<Square>();
    	for (Square s : getSquaresWithPiecesByColor(white)) {
    		if (Arrays.asList(pieceTypes).contains(s.piece.type)) result.add(s);
    	}
    	return result;
    }
    
    /**
     * Can any of my pieces move to a certain Square?
     */
    public boolean canMoveTo(Square targetSquare) {
    	for (Square s : getSquaresWithPiecesByColor(isWhiteToMove())) {
    		if (s.canMoveTo(targetSquare, this, null)) return true;
    	}
    	return false;
    }
	
	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public static class GraphicsComment {
		public Square firstSquare;
		public Square secondSquare;
		public Color color;
		
		public GraphicsComment(Square firstSquare, Square secondSquare,	Color color) {
			this.firstSquare = firstSquare;
			this.secondSquare = secondSquare;
			this.color = color;
		}

		@Override
		public String toString() {
			return firstSquare + "; " + secondSquare + "; " + color;
		}	
		
	}
		

		
}
