//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\util\\TokenGrabber.java

package plsqleditor.parser.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.IInput.BadLocationException;


/**
 * This class represents the tokeniser tool that divides a document up into its
 * individual tokens. Each producted ParseToken consists of the whitespace and
 * comments leading up to the token, and then the token itself. The token will
 * be either a single standalone string, or a delimiter. It is important that
 * the TokenGrabber be passed the list of delimiters because the token grabber
 * must be able to distinguish 4 types of text;
 * <ol>
 * <li> Comments </li>
 * <li> Whitespace </li>
 * <li> Delimiters </li>
 * <li> Tokens </li>
 * </ol>
 * Where Tokens can only be determined by NOT being one of the other three
 * cases.
 * <ol>
 * <li> Comments are determined by the -- and java comment commenting. </li>
 * <li> Whitespace is determined by Character.isWhiteSpace(). </li>
 * <li> Delimiters are determined by the static list of tokens passed on
 * construction. </li>
 * <li> Tokens are everything else. </li>
 * </ol>
 */
public class TokenGrabber
{
    private static final boolean      DEBUG = false;
    private int                       myCurrentLocation;
    private IInput                    myInput;

    /**
     * This is the list of delimiter specifications that are used to determine
     * whether the token being grabbed is going to be a delimiter or not.
     */
    private IDelimiterSpecification[] myDelimiterSpecifications;

    private Map                       myFirstCharacterToDelimiterSet;

    /**
     * @param input
     * @param startLocation
     * @param delimiterSpecs
     * @roseuid 431397980109
     */
    public TokenGrabber(IInput input, int startLocation, IDelimiterSpecification[] delimiterSpecs)
    {
        myInput = input;
        myCurrentLocation = startLocation;
        myDelimiterSpecifications = delimiterSpecs;
        myFirstCharacterToDelimiterSet = new HashMap();
        for (int i = 0; i < myDelimiterSpecifications.length; i++)
        {
            IDelimiterSpecification spec = myDelimiterSpecifications[i];
            Character c = new Character(spec.firstChar());
            List l = (List) myFirstCharacterToDelimiterSet.get(c);
            if (l == null)
            {
                l = new ArrayList();
                myFirstCharacterToDelimiterSet.put(c, l);
            }
            l.add(spec);
        }
    }

    /**
     * If the first non whitespace/comment is the beginning of a delimiter, this
     * will be parsed as a delimiter token.
     * 
     * @return plsqleditor.parser.util.ParseToken
     * @roseuid 431397B702DE
     */
    public ParseToken nextToken()
    {
        StringBuffer whitespaceText = new StringBuffer();
        StringBuffer tokenText = new StringBuffer();
        Position preTokenPosition = new Position(myCurrentLocation);
        Position tokenPosition = new Position(myCurrentLocation);
        boolean isStillProcessing = true;
        boolean isProcessingWhitespace = true;
        int startingLocation = myCurrentLocation;
        try
        {
            while (isStillProcessing)
            {
                char c = myInput.getChar(myCurrentLocation);
                Character ch = new Character(c);
                if (Character.isWhitespace(c))
                {
                    if (isProcessingWhitespace)
                    {
                        whitespaceText.append(c);
                        myCurrentLocation++;
                        continue;
                    }
                    else
                    {
                        return endOfParse(startingLocation,
                                          whitespaceText,
                                          preTokenPosition,
                                          tokenText,
                                          tokenPosition);
                    }
                }
                else if (c == '/')
                {
                    if (isProcessingWhitespace)
                    {
                        if (myInput.getChar(myCurrentLocation + 1) == '*')
                        {
                            whitespaceText.append("/*");
                            myCurrentLocation += 2;
                            while (true)
                            {
                                c = myInput.getChar(myCurrentLocation++);
                                if (c != '*')
                                {
                                    whitespaceText.append(c);
                                }
                                else if (myInput.getChar(myCurrentLocation) == '/')
                                {
                                    whitespaceText.append("*/");
                                    myCurrentLocation++;
                                    break;
                                }
                                else
                                {
                                    whitespaceText.append(c);
                                }
                            }
                            continue;
                        }
                    }
                    else if (myInput.getChar(myCurrentLocation + 1) == '*')
                    {
                        return endOfParse(startingLocation,
                                          whitespaceText,
                                          preTokenPosition,
                                          tokenText,
                                          tokenPosition);
                    }
                    else
                    {
                        // jump to not whitespace or comments section
                    }
                }
                else if (c == '-')
                {
                    if (isProcessingWhitespace)
                    {
                        if (myInput.getChar(myCurrentLocation + 1) == '-')
                        {
                            whitespaceText.append("--");
                            myCurrentLocation += 2;
                            while (c != '\n')
                            {
                                c = myInput.getChar(myCurrentLocation++);
                                whitespaceText.append(c);
                            }
                            continue;
                        }
                    }
                    else if (myInput.getChar(myCurrentLocation + 1) == '-')
                    {
                        return endOfParse(startingLocation,
                                          whitespaceText,
                                          preTokenPosition,
                                          tokenText,
                                          tokenPosition);
                    }
                    else
                    {
                        // jump to not whitespace or comments section
                    }
                }
                // not white space or comments
                if (isProcessingWhitespace)
                {
                    isProcessingWhitespace = false;
                    int wsLength = whitespaceText.length();
                    preTokenPosition.setLength(wsLength);
                }
                String delimiter = getDelimiter(ch, tokenPosition);
                if (delimiter != null)
                {
                    if (tokenText.length() == 0)
                    {
                        // there have been no non delimiter values, so it must
                        // be a delimiter
                        return new ParseToken(whitespaceText.toString(), preTokenPosition,
                                delimiter, tokenPosition, true);
                    }
                    else
                    {
                        // must reset myCurrentLocation
                        myCurrentLocation -= delimiter.length();
                        tokenPosition.setLength(tokenText.length());
                        tokenPosition.setOffset(startingLocation + whitespaceText.length());
                        return new ParseToken(whitespaceText.toString(), preTokenPosition,
                                tokenText.toString(), tokenPosition, true);
                    }
                }
                else
                {
                    // delimiter is null, so we are dealing with an actual token
                    // character
                    tokenText.append(c);
                    myCurrentLocation++;
                }
            }
        }
        catch (BadLocationException ble)
        {
            if (whitespaceText.length() == 0 && tokenText.length() == 0)
            {
                return null;
            }
            else
            {
                return endOfParse(startingLocation,
                                  whitespaceText,
                                  preTokenPosition,
                                  tokenText,
                                  tokenPosition);
            }
        }
        return null; // should never happen
    }

    private ParseToken endOfParse(int startingLocation,
                                  StringBuffer whitespaceText,
                                  Position preTokenPosition,
                                  StringBuffer tokenText,
                                  Position tokenPosition)
    {
        tokenPosition.setLength(tokenText.length());
        tokenPosition.setOffset(startingLocation + preTokenPosition.getLength());
        return new ParseToken(whitespaceText.toString(), preTokenPosition, tokenText.toString(),
                tokenPosition, false);
    }

    private String getDelimiter(Character c, Position toUpdate)
    {
        if (myFirstCharacterToDelimiterSet.containsKey(c))
        {
            List l = (List) myFirstCharacterToDelimiterSet.get(c);
            Collections.sort(l, new DelimiterSpecComparator());
            for (Iterator it = l.iterator(); it.hasNext();)
            {
                IDelimiterSpecification spec = (IDelimiterSpecification) it.next();
                int length = spec.length();
                try
                {
                    String txt = myInput.get(myCurrentLocation, length);
                    if (txt.toUpperCase().equals(spec.getString()))
                    {
                        int pastSpec = myCurrentLocation + length;
                        boolean lineEndsPastSpec = pastSpec >= myInput.getLength();
                        char charPastSpec = 0;
                        if (!lineEndsPastSpec)
                        {
                            charPastSpec = myInput.getChar(pastSpec);
                        }
                        boolean lineStartsHere = myCurrentLocation <= 0;
                        char charBeforeHere = 0;
                        if (!lineStartsHere)
                        {
                            charBeforeHere = myInput.getChar(myCurrentLocation - 1);
                        }

                        if (spec.isStandaloneDelimiter()
                                || ((lineEndsPastSpec || !Character
                                        .isJavaIdentifierPart(charPastSpec)) && (lineStartsHere || !Character
                                        .isJavaIdentifierPart(charBeforeHere))))
                        {
                            toUpdate.setLength(length);
                            toUpdate.setOffset(myCurrentLocation);
                            myCurrentLocation = pastSpec; // += length
                            return txt;
                        }
                    }
                }
                catch (BadLocationException e)
                {
                    if (DEBUG)
                    {
                        e.printStackTrace();// ready to return null;
                    }
                }
            }
        }
        return null;
    }
}
