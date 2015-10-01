package org.ispeech.viseme;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class VisemeSAXParser extends DefaultHandler {
	private String _curr;
	private boolean _inViseme;
	private VisemeHolder _holder;

	private int _len;
	private String _text;
	private String _voice;

	private int _start;
	private int _end;

	private ParserCallback _callback;

	public VisemeSAXParser(ParserCallback callback) {
		_inViseme = false;

		_len = 0;
		_text = "";
		_voice = "";

		_start = 0;
		_end = 0;

		_callback = callback;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		_curr = new String(ch, start, length);
	}

	public void endDocument() throws SAXException {
		while (_holder == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		_callback.setHolder(_holder);
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equalsIgnoreCase("viseme")) {
			_inViseme = true;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (_inViseme) {
			if (qName.equalsIgnoreCase("start")) {
				_start = Integer.parseInt(_curr);
			} else if (qName.equalsIgnoreCase("end")) {
				_end = Integer.parseInt(_curr);
			} else if (qName.equalsIgnoreCase("mouth")) {
				Viseme temp = new Viseme(_start, _end, Integer.parseInt(_curr));
				_holder.addViseme(temp);
			}
		} else {
			if (qName.equalsIgnoreCase("text")) {
				_text = _curr;
			} else if (qName.equalsIgnoreCase("voice")) {
				_voice = _curr;
			} else if (qName.equalsIgnoreCase("length")) {
				_len = Integer.parseInt(_curr);
			} else if (qName.equalsIgnoreCase("frames")) {
				_holder = new VisemeHolder(_len, Integer.parseInt(_curr));
			}
		}
		_curr = "";
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

	}

	public void processingInstruction(String target, String data) throws SAXException {

	}

	public void setDocumentLocator(Locator locator) {

	}

	public void skippedEntity(String name) throws SAXException {

	}

	public void startDocument() throws SAXException {

	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}

	public void notationDecl(String name, String publicId, String systemId) throws SAXException {

	}

	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {

	}
}
