package plsqleditor.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class provides sqlplus execute functionality via the O/S's native sqlplus application.
 * 
 * @version $Id$
 * 
 * Created on 16/03/2005
 */
public class SqlPlusProcessExecutor
{
    /**
     * This class
     * 
     * @author Toby Zines
     * 
     * @version $Id$
     * 
     * Created on 21/03/2005
     * 
     */
    public class NegativeResponseFoundException extends Exception
    {

        /**
         * This field represents the serial version uid.
         */
        private static final long serialVersionUID = 3978144339853783345L;

        /**
         * @param message
         */
        public NegativeResponseFoundException(String message)
        {
            super(message);
        }

        /**
         * @param message
         * @param cause
         */
        public NegativeResponseFoundException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }


    private static final int REREAD_DELAY = 500;

    /**
     * This interface represents a function to check a positive response from a series of lines
     * output from the sqlplus processor.
     * 
     * @author Toby Zines
     * 
     * @version $Id$
     * 
     * Created on 16/03/2005
     * 
     */
    private interface MatchExpression
    {

        /**
         * This method returns true if a positive response is found in the supplied
         * <code>line</code>.
         * 
         * @param line The line being checked for a positive response.
         * 
         * @return <code>true</code> if the supplied <code>line</code> contains a positive
         *         response.
         */
        boolean positiveResponse(String line);

        /**
         * This method returns true if a negative response is found in the supplied
         * <code>line</code>.
         * 
         * @param line The line being checked for a negative response.
         * 
         * @return <code>true</code> if the supplied <code>line</code> contains a negative
         *         response.
         */
        boolean negativeResponse(String line);
    }


    /**
     * Defines the time to wait before giving up on the sqlplus call.
     */
    private static final int    SQLPLUS_TIMEOUT      = 10;

    /**
     * Logger for this class
     */
    private static final Logger logger               = Logger
                                                             .getLogger(SqlPlusProcessExecutor.class
                                                                     .getName());

    private String              myExecutable;
    private String              mySchema;
    private String              myPassword;
    private String              mySid;

    private StringBuffer        myResultText         = new StringBuffer();

    private Process             myProcess            = null;

    private Date                myApplicationEndTime = null;

    private Date                myProcessStartTime   = null;

    private Date                myProcessEndTime     = null;

    private boolean             myIsValid            = false;

    /**
     * @param executable
     * @param mySchema
     * @param myPassword
     * @param mySid
     */
    public SqlPlusProcessExecutor(String executable,
                                  String mySchema,
                                  String myPassword,
                                  String mySid)
    {
        this.myExecutable = executable;
        this.mySchema = mySchema;
        this.myPassword = myPassword;
        this.mySid = mySid;
    }

    /**
     * This method provides
     * 
     * @throws CannotCompleteException
     */
    public void execute(String codeToLoad) throws CannotCompleteException
    {
        String systemCall = constructSystemCall(myExecutable, mySchema, myPassword, mySid);
        BufferedWriter bw = null;
        try
        {
            BufferedReader br = executeSystemCall(systemCall);
            bw = new BufferedWriter(new OutputStreamWriter(getProcess()
                    .getOutputStream()));
            this.setValid(parseResponse(codeToLoad, br, bw));
        }
        finally
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine("Tidying up system process call");
            }

            releaseProcess(bw);
        }
    }

    /**
     * This method releases the process by forcibly killing it if it is still running.
     * 
     * @param bw The buffered writer writing to the process.
     */
    private void releaseProcess(BufferedWriter bw)
    {
        Process p = this.getProcess();
        if (p != null && processIsAlive(p))
        {
            try
            {
                if (bw != null)
                {
                    bw.write("exit");
                    bw.newLine();
                    bw.flush();
                    waitN(200);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if (p != null && processIsAlive(p))
        {
            p.destroy();
            this.setProcessEndTime(new Date());
        }
    }

    /**
     * This is a private method that executes a wait, synchronising on this object and ignoring the
     * interrupted exception.
     * 
     * @param waitTime The number of milliseconds to wait.
     */
    private synchronized void waitN(int waitTime)
    {
        try
        {
            wait(waitTime);
        }
        catch (InterruptedException e)
        {
            // do nothing
        }
    }

    /**
     * This method executes a given command line as a system call.
     * 
     * @param systemCall The command line to run
     * 
     * @return The BufferedReader reading from the input of the process created.
     * 
     * @throws CannotCompleteException when the process dies or an IOException is caught.
     */
    private BufferedReader executeSystemCall(String systemCall) throws CannotCompleteException
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Entered executeSystemCall(" + systemCall + " )");
        }
        System.out.println("Executed " + systemCall);

        BufferedReader in = null;
        boolean isStartedUp = false;
        int resultTextIndex[] = {0};
        setResultText(new StringBuffer());
        StringBuffer resultText = getResultText();
        Runtime r = Runtime.getRuntime();
        MatchExpression expression = new MatchExpression()
        {
            public boolean positiveResponse(String line)
            {
                return matchPositiveStartupResponse(line);
            }

            public boolean negativeResponse(String line)
            {
                return line.indexOf("ERROR:") != -1;
            }
        };
        try
        {
            this.setProcess(r.exec(systemCall));
            this.setProcessStartTime(new Date());
            long timeoutDate = calculateTimeoutDate();
            in = new BufferedReader(new InputStreamReader(this.getProcess().getInputStream()));

            while (isStillReading(isStartedUp, timeoutDate))
            {
                isStartedUp = readNext(in, resultText, resultTextIndex, expression);
                if (isStartedUp)
                {
                    break;
                }
            }
        }
        catch (IOException e)
        {
            logger.warning("exception: " + e);
            throw new CannotCompleteException(e.getMessage(), e);
        }
        catch (NegativeResponseFoundException e)
        {
            final String msg = "Caught a negative response while trying "
                    + "to start up the sqlplus session: " + e.getMessage();
            logger.warning(msg);
            throw new CannotCompleteException(msg, e);
        }
        finally
        {
            System.out.println(resultText);
        }

        if (this.getProcess() == null || !isStartedUp)
        {
            logger.warning("Exec failed for system call '" + systemCall + "'.");
            throw new CannotCompleteException("Exec failed for system call '" + systemCall + "'.");
        }

        logger.info("SqlPlus process completed startup at " + new Date());
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Leaving executeSystemCall normally.");
        }
        return in;
    }

    /**
     * @param line
     * @return <code>true</code> if the supplied <code>line</code> matches JServer Release .* Production
     */
    boolean matchPositiveStartupResponse(String line)
    {
        // JServer Release 9.2.0.6.0 - Production
        return line.indexOf("JServer Release ") != -1 && line.indexOf("Production") != -1;
    }

    /**
     * Given a particular <code>executable</code>, <code>schema</code>, <code>password</code>
     * and <code>sid</code>, this method creates a string that will be used as the executable.
     * 
     * @param executable
     * 
     * @param schema
     * 
     * @param password
     * 
     * @param sid
     * 
     * @return The string to execute.
     */
    protected String constructSystemCall(String executable,
                                         String schema,
                                         String password,
                                         String sid)
    {
        return executable + " " + schema + "/" + password + "@" + sid;
    }

    /**
     * Given a sqlplus process that is currently running this method does one of the following
     * things:
     * <ul>
     * <li> Waits until a successful response is parsed. </li>
     * <li> Waits until SQLPLUS_TIMEOUT seconds have passed. </li>
     * <li> Waits for normal termination of the process. </li>
     * </ul>
     * It then parses any response from the process to see if the ping system call met the success
     * criteria. <myProcess>The ping may hang (depending on the O/S implementation of ping) so this
     * method must be careful not to make a blocking read on the response stream. <myProcess>As the
     * native ping command waits at least 1 second between successive ping calls it does not make
     * sense for continuous polling of the response stream. Instead, the parser pauses for
     * approximatly 1 second before retrying the response stream. In order to perform this pause the
     * method must be syncronised in order for the Thread to obtain this objects monitor.
     * 
     * @return true if the response was positive.
     * 
     * @throws CannotCompleteException
     */
    private boolean parseResponse(String input, BufferedReader in, BufferedWriter out)
            throws CannotCompleteException
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Entering response parsing block.");
        }
        setResultText(new StringBuffer());
        StringBuffer resultText = this.getResultText();

        boolean isPositiveResponse = false;
        MatchExpression expression = new MatchExpression()
        {
            public boolean positiveResponse(String thisLine)
            {
                return matchPositiveResponse(thisLine);
            }

            public boolean negativeResponse(String line)
            {
                return false;
            }
        };
        try
        {
            out.write(input);
            out.flush();
            long timeoutDate = calculateTimeoutDate();
            int resultTextIndex[] = {0};
            while (isStillReading(isPositiveResponse, timeoutDate))
            {
                // If the buffer is not ready: sleep then continue
                if (!(isPositiveResponse = readNext(in, resultText, resultTextIndex, expression)))
                {
                    continue;
                }
                // Min num of successes reached so stop looping
                if (isPositiveResponse)
                {
                    releaseProcess(out);
                    in.close();
                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.fine("Leaving response parsing block with positive response.");
                        logger.fine("ResultText=" + resultText);
                    }
                    return true;
                }

                // Wait for a second so we can poll the output in an efficient
                // manner.
                if (logger.isLoggable(Level.FINE))
                {
                    logger.fine("Waiting 1 second for call to finish.");
                }
                waitN(REREAD_DELAY);
            }
            // Kill process if not already dead
            releaseProcess(out);
            // If the process ended of its own accord then ensure the
            // approximate end date is recorded for timing purposes.
            if (this.getPingProcessEndTime() == null)
            {
                this.setProcessEndTime(new Date());
            }

            isPositiveResponse = readNext(in, resultText, resultTextIndex, expression);
            // Close stream
            in.close();
            // Check for minimum number of successes
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine("Leaving response parsing block with result=" + isPositiveResponse);
                logger.fine("ResultText=" + resultText);
            }
            return isPositiveResponse;
        }
        catch (IOException e)
        {
            throw new CannotCompleteException(e.getMessage());
        }
        catch (NegativeResponseFoundException e)
        {
            e.printStackTrace();
            // should never happen because this readNext has a MatchExpression
            // that always returns false.
            return false;
        }
        finally
        {
            System.out.println(resultText);
        }
    }

    /**
     * This method checks whether we should still be reading from a loop.
     * 
     * @param breakAnyway if this is <code>true</code>, we will return <code>false</code>,
     *            indicating that we should break regardless of other circumstances.
     * 
     * @param timeoutDate The time after which we should no longer read.
     * 
     * @return <code>true</code> if we should re-enter a read loop and <code>false</code>
     *         otherwise.
     */
    private boolean isStillReading(boolean breakAnyway, long timeoutDate)
    {
        return processIsAlive(this.getProcess())
        // Quit looping after the timeout has expired or the response is ok
                && (new Date()).getTime() < timeoutDate && !breakAnyway;
    }

    /**
     * This method reads the next available bytes from the supplied <code>input</code> reader and
     * if there is any new data, stores it in the <code>resultText</code> and checks it against
     * the <code>matcher</code> to see if any termination conditions are met. If they are, the
     * method returns in the case of {@link MatchExpression#positiveResponse(String)} or throws an
     * exception in the case of {@link MatchExpression#negativeResponse(String)}. The current
     * location from which to parse the <code>resultText</code> is stored and updated in the
     * <code>resultTextIndex</code>.
     * <p>
     * This method is non blocking, but will wait for a period of {@link #REREAD_DELAY} milliseconds
     * before returning from an attempt to read no data.
     * 
     * @param input The input from which this method is reading the next.
     * 
     * @param resultText The full text that has been read from the input.
     * 
     * @param resultTextIndex The current index of the <code>resultText</code> that has already
     *            been parsed.
     * 
     * @param matcher The expression matcher that checks for termination conditions in the
     *            <code>resultText</code>.
     * 
     * @return <code>true</code> if the <code>matcher</code> finds a positive response and
     *         <code>false</code> otherwise.
     * 
     * @throws IOException When reading the input fails.
     * 
     * @throws NegativeResponseFoundException When the <code>matcher</code> matches a negative
     *             response.
     */
    private synchronized boolean readNext(BufferedReader input,
                                          StringBuffer resultText,
                                          int[] resultTextIndex,
                                          MatchExpression matcher) throws IOException,
            NegativeResponseFoundException
    {
        if (!input.ready())
        {
            this.waitN(REREAD_DELAY);
            return false;
        }

        // Read as much as possible from the buffer (non-blocking)
        while (input.ready())
        {
            int i = input.read();
            if (i == -1)
            {
                break;
            }
            this.getResultText().append((char) i);
        }

        // Parse each line in result for success (if any)
        int indexOfCr = resultText.toString().indexOf('\n', resultTextIndex[0]);
        while (indexOfCr != -1)
        {
            String thisLine = resultText.substring(resultTextIndex[0], indexOfCr + 1);
            resultTextIndex[0] = indexOfCr + 1;

            // Do match on the line and increment if a positive response
            // is found
            if (matcher.positiveResponse(thisLine))
            {
                System.out.println("Positive Response on " + thisLine);
                return true;
            }
            if (matcher.negativeResponse(thisLine))
            {
                throw new NegativeResponseFoundException("The line [" + thisLine
                        + "] contains a negative response");
            }

            indexOfCr = resultText.toString().indexOf('\n', resultTextIndex[0]);
        }
        return false;
    }

    /**
     * This method determines the timeout date of the current request by checking the 
     * process start time against the {@link #SQLPLUS_TIMEOUT} (times 1000 for milliseconds).
     * 
     * @return The time after which no more reading should be done for this call.
     */
    private long calculateTimeoutDate()
    {
        return this.getProcessStartTime().getTime() + (SQLPLUS_TIMEOUT * 1000);
    }

    /**
     * This method takes a line from a response and verifies that it contains a successful created
     * message.
     * 
     * @param response A line from a sqlplus response. The line is expected to terminate with an
     *            end-of-line character
     * 
     * @return true if the response line represents a successful creation.
     */
    protected boolean matchPositiveResponse(String response)
    {
        if (response.indexOf("Package") != -1 && response.indexOf("created") != -1)
        {
            return true;
        }
        return false;
    }

    /**
     * This method determines if the system call process has stopped running. It checks the exit
     * value of the process and catches an exception if the process has not yet returned an exit
     * value (i.e. the process is still running)
     * 
     * @return true if the process is still running, false otherwise.
     */
    static private boolean processIsAlive(Process p)
    {
        if (p == null) return false;

        try
        {
            p.exitValue();
        }
        catch (IllegalThreadStateException e)
        {
            return true;
        }
        return false;
    }

    public StringBuffer getResultText()
    {
        return myResultText;
    }

    public void setResultText(StringBuffer myResultText)
    {
        this.myResultText = myResultText;
    }

    private Process getProcess()
    {
        return myProcess;
    }

    private void setProcess(Process p)
    {
        this.myProcess = p;
    }

    public Date getProcessStartTime()
    {
        return myProcessStartTime;
    }

    private void setProcessStartTime(Date myProcessStartTime)
    {
        this.myProcessStartTime = myProcessStartTime;
    }

    public Date getPingProcessEndTime()
    {
        return myProcessEndTime;
    }

    private void setProcessEndTime(Date myProcessEndTime)
    {
        this.myProcessEndTime = myProcessEndTime;
    }

    public Date getApplicationEndTime()
    {
        return myApplicationEndTime;
    }

    public long getProcessDuration()
    {
        return this.getPingProcessEndTime().getTime() - this.getProcessStartTime().getTime();
    }

    /**
     * This method returns the result.
     * 
     * @return {@link #myIsValid}.
     */
    public boolean isValid()
    {
        return myIsValid;
    }


    /**
     * This method sets the status of the result of this execution.
     * 
     * @param result The result to set.
     */
    public void setValid(boolean result)
    {
        myIsValid = result;
    }

}
