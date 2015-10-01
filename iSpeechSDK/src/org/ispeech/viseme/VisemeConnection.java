package org.ispeech.viseme;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class VisemeConnection implements Runnable, ParserCallback
{
		
	private String _url;	
	private VisemeHolder _holder;
	private MouthMovements _mouthMovements;
	
	private final String TAG="VisemeConnection";
	
    public VisemeConnection(String url,MouthMovements mouthMovements)
    {
        super();
        _mouthMovements = mouthMovements;
        _url = url;
    }

    public void run()
    {
    	try
    	{
			getXml();
			_mouthMovements.gotVisimes(getHolder());
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
    	
    	
    	VisemeSAXParser viz = new VisemeSAXParser(this);
    	SAXParserFactory factory = SAXParserFactory.newInstance();
    	factory.setFeature("http://xml.org/sax/features/namespaces", false);            
    	factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    	SAXParser parser = factory.newSAXParser();
    	parser.parse(inputStream, viz);
    }
    
    /**
     * callback for the parser to set the holder object
     */
	public void setHolder(VisemeHolder h) {
		_holder=h;
	}
	
	/**
	 * @return the filled out holder object
	 */
	public VisemeHolder getHolder()
	{
		return _holder;
	}
}
