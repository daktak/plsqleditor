package plsqleditor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import plsqleditor.PlsqleditorPlugin;

public class StreamProcessor extends Thread
{
    InputStream is;
    String      type;

    public StreamProcessor(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
            {
            	String msg = type + " Processor > " + line;
            	PlsqleditorPlugin.log(msg, null); // TODO remove this next release
                PlsqleditorPlugin.getDefault().sendMessageToConsole(msg);
            }
            PlsqleditorPlugin.getDefault().showConsole();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }


}
