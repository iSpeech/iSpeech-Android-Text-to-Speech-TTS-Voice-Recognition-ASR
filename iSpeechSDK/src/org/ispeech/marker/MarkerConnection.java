package org.ispeech.marker;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;


public class MarkerConnection implements Runnable, ParserMarkerCallback
{
		
	private String _url;	
	private MarkerHolder _holder;
	private MarkerMovements _markerMovements;
	
	private final String TAG="VisemeConnection";
	
    public MarkerConnection(String url,MarkerMovements mouthMovements)
    {
        super();
        _markerMovements = mouthMovements;
        _url = url;
    }

    public void run()
    {
    	try
    	{
			getXml();
			_markerMovements.gotMarkers(getHolder());
		}
    	catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Sets up the connections and the parser, and calls them
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void getXml() throws IOException, ParserConfigurationException, SAXException
    {
    	_url=_url.replaceAll("%3D", "=");
    	URL url = new URL(_url);
    	URLConnection urlConnection = url.openConnection();
    	InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
    	
    	MarkerSAXParser viz = new MarkerSAXParser(this);
    	SAXParserFactory factory = SAXParserFactory.newInstance();
    	factory.setFeature("http://xml.org/sax/features/namespaces", false);            
    	factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    	SAXParser parser = factory.newSAXParser();
    	parser.parse(inputStream, viz);
    }
    
    /**
     * callback for the parser to set the holder object
     */
	public void setHolder(MarkerHolder h) {
		_holder=h;
	}
	
	/**
	 * @return the filled out holder object
	 */
	public MarkerHolder getHolder()
	{
		return _holder;
	}
}
