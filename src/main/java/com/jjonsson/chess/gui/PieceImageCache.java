package com.jjonsson.chess.gui;

import static com.jjonsson.utilities.Logger.LOGGER;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import com.google.common.collect.Maps;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.utilities.Logger;

public final class PieceImageCache
{
	private PieceImageCache()
	{

	}

	private static SAXSVGDocumentFactory svgFactory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());

	private static Map<String, SVGDocument> pieceCache = Maps.newHashMap();
	public static SVGDocument getSVGForPiece(final Piece p)
	{
		SVGDocument cachedDocument = pieceCache.get(p.getIdentifier());
		if(cachedDocument == null)
		{
			cachedDocument = imageForPiece(p);
			//TODO(jontejj) can we actually cache this?
			//pieceCache.put(p.getIdentifier(), cachedDocument);
		}
		return cachedDocument;
	}
	/**
	 * Note: This function requires that the "images" directory is added to the class path
	 */
	private static SVGDocument imageForPiece(final Piece p)
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
			LOGGER.severe("PieceImageCache: Couldn't find resource for image at: " + image);
			return null;
		}
		catch (IOException e)
		{
			LOGGER.severe("Failed to load image for piece: " + p);
			LOGGER.severe(Logger.stackTraceToString(e));
		}
		return document;
	}
}
