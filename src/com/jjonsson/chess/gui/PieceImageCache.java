package com.jjonsson.chess.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import com.jjonsson.chess.pieces.Piece;

public class PieceImageCache
{
	private static String myPath = "file://" + new File("").getAbsolutePath() + "/images/svg/piece_";
	
	private static SAXSVGDocumentFactory svgFactory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
	
	private static HashMap<String, SVGDocument> pieceCache = new HashMap<String, SVGDocument>();
	public static SVGDocument getSVGForPiece(Piece p)
	{
		SVGDocument cachedDocument = pieceCache.get(p.getIdentifier());
		if(cachedDocument == null)
		{
			try
			{
				cachedDocument = svgFactory.createSVGDocument(imagePathForPiece(p));
				//pieceCache.put(p.getIdentifier(), cachedDocument);
			}
			catch (FileNotFoundException fnfe)
			{
				System.out.println("PieceImageCache: Couldn't find image at " + imagePathForPiece(p));
				return null;
			}
			catch (IOException e)
			{
				System.out.println("Failed to load image for piece: " + p);
				e.printStackTrace();
			}
		}
		return cachedDocument;
	}
	
	private static String imagePathForPiece(Piece p)
	{
		return myPath + p.getIdentifier() + ".svg";
	}
}
