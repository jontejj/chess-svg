package com.jjonsson.chess.gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import com.google.common.collect.Maps;
import com.jjonsson.chess.pieces.Piece;

public class PieceImageCache
{

	private static SAXSVGDocumentFactory svgFactory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
	
	private static HashMap<String, SVGDocument> pieceCache = Maps.newHashMap();
	public static SVGDocument getSVGForPiece(Piece p)
	{
		SVGDocument cachedDocument = pieceCache.get(p.getIdentifier());
		if(cachedDocument == null)
		{
			cachedDocument = imageForPiece(p);
			//pieceCache.put(p.getIdentifier(), cachedDocument);
		}
		return cachedDocument;
	}
	/**
	 * Note: This function requires that the "images" directory is added to the class path
	 */
	private static SVGDocument imageForPiece(Piece p)
	{
		SVGDocument document = null;
		String image = "/images/svg/Piece_" + p.getIdentifier() + ".svg";
		try
		{
			URL resource = PieceImageCache.class.getResource(image);
			document = svgFactory.createSVGDocument(resource.toString());
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("PieceImageCache: Couldn't find resource for image at: " + image);
			return null;
		}
		catch (IOException e)
		{
			System.out.println("Failed to load image for piece: " + p);
			e.printStackTrace();
		}
		return document;
	}
}
