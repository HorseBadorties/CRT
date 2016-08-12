package de.toto.game;

import java.util.ArrayList;
import java.util.List;

import de.toto.game.Rules.Piece;
import de.toto.game.Rules.PieceType;

public class Position {
	
	private Square[][] squares;	
	private Position previous = null;
	private List<Position> next = new ArrayList<Position>(); 
	private int variationLevel = 0; //0 = main line, 1 = variation, 2 = variation of variation ...
	private String fen = null;
	private String move = null;
	private String comment = null; // may contain graphics token such as [%csl Ge5][%cal Ge5b2]
	private List<String> nags = new ArrayList<String>(); // !, ?, ?? ...
		
	// Startposition
	public Position() {		
		setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", true);
	}

	public Position(Position previous, String move) {
		this(previous, move, null);
	}
	
	public Position(Position previous, String move, String fen) {
		this(previous, move, fen, false);
	}
	
	public Position(Position previous, String move, String fen, boolean asVariation) {
		this.previous = previous;
		previous.addNextPosition(this, asVariation);		
		this.variationLevel = asVariation ? previous.variationLevel+1 : previous.variationLevel;
		if (fen != null) {
			setMove(move, false);
			setFen(fen, true);		
		} else if (move != null) {
			setMove(move, true);
		}
	}	
	
	
	// Compute the hashCode using it's FEN, ignoring the Halfmove and Fullmove fields	
	@Override
	public int hashCode() {
		if (fen == null) {
			return super.hashCode();
		} else {
			String[] fenFields = fen.split(" ");
			return (fenFields[0]+fenFields[1]+fenFields[2]+fenFields[3]).hashCode();
		}
	}
	
	@Override
	public boolean equals(Object obj) {		
		if (!(obj instanceof Position)) return false;
		return this.hashCode() == obj.hashCode();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public void addNag(String value) {
		nags.add(value);
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
		addNextPosition(variation, true);
		variation.setPrevious(this);
		variation.setVariationLevel(this.variationLevel+1);		
	}
	
	public boolean hasPrevious() {
		return previous == null;
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
			if (aVariation.equals(variation)) return aVariation;
		}
		return null;
	}
	
	public String getFen() {
		return fen;
	}

	public String getMove() {
		return move;
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
	
	public boolean isMate() {
		return move != null && move.endsWith("#");
	}
	
	public int getMoveNumber() {		
		return previous != null ? Integer.parseInt(previous.getFen().split(" ")[5]) : 0;
		
	}
		
	private void addNextPosition(Position nextPosition, boolean asVariation) {
		next.add(asVariation ? next.size() : 0, nextPosition);			
	}
		
	private void initSquares() {
		squares = new Square[8][8];
		boolean isWhite;
		for (int rank = 1; rank <= 8; rank++) {
			isWhite = rank % 2 == 0 ? true : false;
			for (int file = 1; file <= 8; file++) {
				squares[rank - 1][file - 1] = new Square((byte)rank, (byte)file);
				isWhite = !isWhite;
			}
		}
	}
	
	public Square getSquare(int rank, int file) {
		return squares[rank - 1][file - 1];
	}

	// e.g. "a1"
	private Square getSquare(String squarename) {
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
				int rank = isWhiteToMove() ? 8 : 1;
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
		if (castleField.length() == 0) castleField = "-";
		fen.append(" ").append(castleField);
		// En passant square field
		fen.append(" ").append(getEnPassantFenField());
		// TODO Halfmove clock field
		fen.append(" 0");
		// Fullmove number field		
		int moveNumber = previousFenFields != null ? Integer.parseInt(previousFenFields[5]) : 1;
		if (whiteToMove && previous != null) moveNumber++;
		fen.append(" ").append(moveNumber);
		this.fen = fen.toString();
	}
	
	// TODO En passant square field
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
	private void setMove(String move, boolean setupPosition) {
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
				move = sanToLan(move);
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
		if (move != null && !("--".equals(move))) {
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
	
	@Override
	public String toString() {
		return move + "\n" + dumpSquares();
	}
	
	private String dumpSquares() {
		StringBuilder result = new StringBuilder();
		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++) {
				String s = squares[rank][file].getName();
				if (squares[rank][file].piece != null) {
					s = squares[rank][file].piece.fenChar + s;
				}
				if (s.length() == 2) {
					result.append(" ");
				}
				result.append(s).append(" ");
				
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	// construct a LAN move out of a SAN for this position 
	private String sanToLan(String san) {
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
		boolean whiteMoved = whiteMoved();
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
		return from.getNameWithPieceSuffix() + (isCapture ? "x" : "-") + to.getName() + suffix;
	}
	
	private List<Square> getSquaresWithPiece(Piece piece) {
		List<Square> matchingSquares = new ArrayList<Square>();
		for (int rank = 1; rank <= 8; rank++) {
			for (int file = 1; file <= 8; file++) {
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
		Piece ourKing = whiteMoved() ? Piece.WHITE_KING : Piece.BLACK_KING;
		for (int _rank = 1; _rank <= 8; _rank++) {
			for (int _file = 1; _file <= 8; _file++) {
				if (squares[_rank - 1][_file - 1].piece == ourKing) {
					return squares[_rank - 1][_file - 1];
				}
			}
		}
		return null;	
	}
		
	public static void main(String[] args) {		
		Position p = new Position();
		System.out.println(p.isLanMove("wxd5+"));
	}
		
}
