/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 22/02/2005
 */
public class PlSqlDoubleClickStrategy2 implements ITextDoubleClickStrategy
{
    protected ITextViewer fText;
    protected int         fPos;
    protected int         fStartPos;
    protected int         fEndPos;
    protected static char fgBrackets[] = {'{', '}', '(', ')', '[', ']', '"', '"'};


    public PlSqlDoubleClickStrategy2()
    {
        //
    }

    public void doubleClicked(ITextViewer text)
    {
        fPos = text.getSelectedRange().x;
        if (fPos < 0) return;
        fText = text;
        if (!selectBracketBlock()) selectWord();
    }

    protected boolean matchBracketsAt()
    {
        int bracketIndex1 = fgBrackets.length;
        int bracketIndex2 = fgBrackets.length;
        fStartPos = -1;
        fEndPos = -1;
        try
        {
            IDocument doc = fText.getDocument();
            char prevChar = doc.getChar(fPos - 1);
            char nextChar = doc.getChar(fPos);
            for (int i = 0; i < fgBrackets.length; i += 2)
                if (prevChar == fgBrackets[i])
                {
                    fStartPos = fPos - 1;
                    bracketIndex1 = i;
                }

            for (int i = 1; i < fgBrackets.length; i += 2)
                if (nextChar == fgBrackets[i])
                {
                    fEndPos = fPos;
                    bracketIndex2 = i;
                }

            if (fStartPos > -1 && bracketIndex1 < bracketIndex2)
            {
                fEndPos = searchForClosingBracket(fStartPos,
                                                  prevChar,
                                                  fgBrackets[bracketIndex1 + 1],
                                                  doc);
                if (fEndPos > -1) return true;
                fStartPos = -1;
            }
            else if (fEndPos > -1)
            {
                fStartPos = searchForOpenBracket(fEndPos,
                                                 fgBrackets[bracketIndex2 - 1],
                                                 nextChar,
                                                 doc);
                if (fStartPos > -1) return true;
                fEndPos = -1;
            }
        }
        catch (BadLocationException _ex)
        {
            //
        }
        return false;
    }

    protected boolean matchWord()
    {
        IDocument doc = fText.getDocument();
        try
        {
            int pos;
            for (pos = fPos; pos >= 0; pos--)
            {
                char c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) break;
            }

            fStartPos = pos;
            pos = fPos;
            for (int length = doc.getLength(); pos < length; pos++)
            {
                char c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) break;
            }

            fEndPos = pos;
            return true;
        }
        catch (BadLocationException _ex)
        {
            return false;
        }
    }

    protected int searchForClosingBracket(int startPosition,
                                          char openBracket,
                                          char closeBracket,
                                          IDocument document) throws BadLocationException
    {
        int stack = 1;
        int closePosition = startPosition + 1;
        for (int length = document.getLength(); closePosition < length && stack > 0; closePosition++)
        {
            char nextChar = document.getChar(closePosition);
            if (nextChar == openBracket && nextChar != closeBracket) stack++;
            else if (nextChar == closeBracket) stack--;
        }

        if (stack == 0) return closePosition - 1;
        else
            return -1;
    }

    protected int searchForOpenBracket(int startPosition,
                                       char openBracket,
                                       char closeBracket,
                                       IDocument document) throws BadLocationException
    {
        int stack = 1;
        int openPos;
        for (openPos = startPosition - 1; openPos >= 0 && stack > 0; openPos--)
        {
            char nextChar = document.getChar(openPos);
            if (nextChar == closeBracket && nextChar != openBracket) stack++;
            else if (nextChar == openBracket) stack--;
        }

        if (stack == 0) return openPos + 1;
        else
            return -1;
    }

    protected boolean selectBracketBlock()
    {
        if (matchBracketsAt())
        {
            if (fStartPos == fEndPos) fText.setSelectedRange(fStartPos, 0);
            else
                fText.setSelectedRange(fStartPos + 1, fEndPos - fStartPos - 1);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void selectWord()
    {
        if (matchWord()) if (fStartPos == fEndPos) fText.setSelectedRange(fStartPos, 0);
        else
            fText.setSelectedRange(fStartPos + 1, fEndPos - fStartPos - 1);
    }

}
