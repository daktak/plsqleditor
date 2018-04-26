package plsqleditor.preferences.entities;

import plsqleditor.parsers.ParseType;

public class PackageLocation
{
    private String myLocation;
    private ParseType myParseType;
    
    public PackageLocation(String location, ParseType parseType)
    {
        myLocation = location;
        myParseType = parseType;
    }

    public String getLocation()
    {
        return myLocation;
    }

    public ParseType getParseType()
    {
        return myParseType;
    }
    
    public void setParseType(ParseType type)
    {
        myParseType = type;
    }
}
