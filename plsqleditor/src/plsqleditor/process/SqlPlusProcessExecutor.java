/**
 * 
 */
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
         * @param thisLine
         * @return
         */
        boolean positiveResponse(String thisLine);

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
     * This method provides ping functionality via the O/S's native ping application.
     * <p>
     * A ping timeout and success criteria may be specified to configure how the call to ping is
     * performed and how the results are interpreted. The success criteria specifies the number of
     * successful pings before a timeout period is reached. If the timeout is reached or too many
     * pings are unresponsive then a failure result is returned.
     * <p>
     * The success or failure of the operation may be determined by a call to getResult() and the
     * text returned from the ping may be retrieved by a call to getResultText(). This method also
     * gathers timing statistics that may be examined by calls to getApplicationDuration() and
     * getPingProcessDuration().
     * 
     * @param ipAddress The IP address to ping. The IP address must be a fully qualified, 4 octet
     *            address. Hostnames are not accepted.
     * @param pingTimeout The length of time to give the ping system call to self terminate before
     *            killing the process.
     * @param successThreshold The minimum number of successful pings that are required for a
     *            successful result.
     * @throws CannotCompleteException
     * @throws CannotCompleteException
     */
    public void execute(String codeToLoad) throws CannotCompleteException
    {
        String systemCall = constructSystemCall(myExecutable, mySchema, myPassword, mySid);
        BufferedReader br = executeSystemCall(systemCall);

        try
        {
            this.setValid(parseResponse(codeToLoad, br));
        }
        finally
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine("Tidying up system process call");
            }

            releaseProcess();
        }
    }

    /**
     * This method releases the process by forcibly killing it if it is still running.
     * 
     * @param myProcess A non-null Process that may or may not be running.
     */
    private void releaseProcess()
    {
        Process p = this.getProcess();
        if (p != null && processIsAlive(p))
        {
            p.destroy();
            this.setProcessEndTime(new Date());
        }
    }

    /**
     * This method executes a given command line as a system call.
     * 
     * @param systemCall The command line to run
     * @return The Process created by the system call.
     * @throws CannotCompleteException
     */
    private BufferedReader executeSystemCall(String systemCall) throws CannotCompleteException
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Entered executeSystemCall.");
        }

        BufferedReader in = null;
        Runtime r = Runtime.getRuntime();
        try
        {
            MatchExpression expression = new MatchExpression()
            {
                public boolean positiveResponse(String thisLine)
                {
                    return matchPositiveStartupResponse(thisLine);
                }
            };

            this.setProcess(r.exec(systemCall));
            this.setProcessStartTime(new Date());
            long timeoutDate = calculateTimeoutDate();
            boolean isStartedUp = false;
            setResultText(new StringBuffer());
            StringBuffer resultText = getResultText();
            int resultTextIndex[] = {0};
            in = new BufferedReader(new InputStreamReader(this.getProcess().getInputStream()));

            while (processIsAlive(this.getProcess()) && (new Date()).getTime() < timeoutDate
                    && !isStartedUp)
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
        catch (InterruptedException e)
        {
            throw new CannotCompleteException(e.getMessage());
        }

        if (this.getProcess() == null)
        {
            logger.warning("Exec failed for system call '" + systemCall + "'.");
            throw new CannotCompleteException("Exec failed for system call '" + systemCall + "'.");
        }

        // If there is an exception from here on then a reference to the process
        // would be lost but it would keep running.
        // Therefore a try/catch block is required to prevent runtime exceptions
        // from causing a runaway process.
        try
        {
            logger.info("SqlPlus process completed startup at " + new Date());
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine("Leaving executeSystemCall normally.");
            }
        }
        catch (RuntimeException e)
        {
            releaseProcess();
            throw e;
        }
        return in;
    }

    /**
     * @param line
     * @return
     */
    boolean matchPositiveStartupResponse(String line)
    {
        // JServer Release 9.2.0.6.0 - Production
        return line.contains("JServer Release ") && line.contains("Production");
    }

    /**
     * Given a particular IP address this method constructs a native ping call. As this method is
     * O/S dependant it may be overridden by subclasses. The subclass can then define a native ping
     * call specific to the O/S. By default this method performs a ping under Solaris 9.
     * 
     * @param ipAddress
     * @return
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
     * @param myProcess
     * @param resultText
     * @param ipAddress
     * @return
     * @throws CannotCompleteException
     */
    private boolean parseResponse(String input, BufferedReader in) throws CannotCompleteException
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
        };
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(getProcess()
                    .getOutputStream()));
            bw.write(input);
            bw.flush();
            long timeoutDate = calculateTimeoutDate();
            int resultTextIndex[] = {0};
            while (processIsAlive(this.getProcess())
            // Quit looping after the timeout has expired or the response is ok
                    && (new Date()).getTime() < timeoutDate && !isPositiveResponse)
            {
                // If the buffer is not ready: sleep then continue
                if (!(isPositiveResponse = readNext(in, resultText, resultTextIndex, expression)))
                {
                    continue;
                }
                // Min num of successes reached so stop looping
                if (isPositiveResponse)
                {
                    releaseProcess();
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
                synchronized (this)
                {
                    wait(REREAD_DELAY);
                }
            }
            // Kill process if not already dead
            releaseProcess();
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
        catch (InterruptedException e)
        {
            throw new CannotCompleteException(e.getMessage());
        }
    }

    private synchronized boolean readNext(BufferedReader in,
                                          StringBuffer resultText,
                                          int[] resultTextIndex,
                                          MatchExpression match) throws IOException,
            InterruptedException
    {
        if (!in.ready())
        {
            this.wait(REREAD_DELAY);
            return false;
        }

        // Read as much as possible from the buffer (non-blocking)
        while (in.ready())
        {
            int i = in.read();
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
            if (match.positiveResponse(thisLine))
            {
                System.out.println("Positive Response on " + thisLine);
                return true;
            }

            indexOfCr = resultText.toString().indexOf('\n', resultTextIndex[0]);
        }
        return false;
    }

    /**
     * @return
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
        if (response.contains("Package") && response.contains("created"))
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
     * This method sets the ...
     * 
     * @param result The result to set.
     */
    public void setValid(boolean result)
    {
        myIsValid = result;
    }

}
