//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\util\\IInput.java

package plsqleditor.parser.util;

public interface IInput
{
    public static class BadLocationException extends Exception
    {
        /**
         * This is the serial version id.
         */
        private static final long serialVersionUID = 719646247432136880L;

        public BadLocationException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    /**
     * Returns this document's complete text.
     * 
     * @return the document's complete text.
     * @roseuid 4319142E0290
     */
    public String get();

    /**
     * Returns this document's text for the specified range.
     * 
     * @param offset the document offset
     * @param length the length of the specified range
     * @return the document's text for the specified range
     * @exception BadLocationException if the range is invalid in this document
     * @roseuid 4319146C0242
     */
    public String get(int offset, int length) throws IInput.BadLocationException;

    /**
     * Returns the character at the given document offset in this document.
     * 
     * @param offset a document offset
     * @return the character at the offset
     * @exception BadLocationException if the offset is invalid in this documen
     * @roseuid 431914EC0157
     */
    public char getChar(int offset) throws IInput.BadLocationException;

    /**
     * Returns the number of characters in this document.
     * 
     * @return the number of characters in this document
     * @roseuid 4319151A02DE
     */
    public int getLength();
    
    /**
     * Returns the number of the line at which the character of the specified position is located.
     * The first line has the line number 0. A new line starts directly after a line
     * delimiter. <code>(offset == document length)</code> is a valid argument although there is no
     * corresponding character.
     *
     * @param offset the document offset
     * @return the number of the line
     * @exception BadLocationException if the offset is invalid in this document
     */
    public int getLineOfOffset(int offset) throws IInput.BadLocationException;
    
    /**
     * Determines the offset of the first character of the given line.
     *
     * @param line the line of interest
     * @return the document offset
     * @exception BadLocationException if the line number is invalid in this document
     */
    public int getLineOffset(int line) throws IInput.BadLocationException;
}
