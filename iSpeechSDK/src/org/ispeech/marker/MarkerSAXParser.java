package org.ispeech.marker;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MarkerSAXParser extends DefaultHandler {
	private String _curr;
	private boolean _inViseme;
	private MarkerHolder _holder;

	private int _len;
	private String _text;
	private String _voice;

	private int _start;
	private int _end;

	private ParserMarkerCallback _callback;

	public MarkerSAXParser(ParserMarkerCallback callback) {
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
		if (qName.equalsIgnoreCase("word")) {
			_inViseme = true;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (_inViseme) {
			if (qName.equalsIgnoreCase("start")) {
				_start = Integer.parseInt(_curr);
			} else if (qName.equalsIgnoreCase("end")) {
				_end = Integer.parseInt(_curr);
			} else if (qName.equalsIgnoreCase("text")) {
				Marker temp = new Marker(_start, _end, _curr);
				_holder.addMarker(temp);
			}
		} else {
			if (qName.equalsIgnoreCase("text")) {
				_text = _curr;
			} else if (qName.equalsIgnoreCase("voice")) {
				_voice = _curr;
			} else if (qName.equalsIgnoreCase("length")) {
				_len = Integer.parseInt(_curr);
			} else if (qName.equalsIgnoreCase("words")) {
				_holder = new MarkerHolder(_text, _voice, _len, Integer.parseInt(_curr));
			}
		}
		_curr = "";
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {

	}

	@Override
	public void setDocumentLocator(Locator locator) {

	}

	@Override
	public void skippedEntity(String name) throws SAXException {

	}

	@Override
	public void startDocument() throws SAXException {

	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId) throws SAXException {

	}

	@Override
	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {

	}
}
