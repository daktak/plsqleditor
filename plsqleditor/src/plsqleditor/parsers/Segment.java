package plsqleditor.parsers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Position;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.preferences.PreferenceConstants;


public class Segment implements Comparable, Cloneable
{
    public static final String  PRIVATE   = "Private";
    public static final String  PUBLIC    = "Public";
    private static final String PROCEDURE = AbstractPlSqlParser.PROCEDURE;
    private static final String FUNCTION  = AbstractPlSqlParser.FUNCTION;
    private static final String PRAGMA    = AbstractPlSqlParser.PRAGMA;
    private static final String TYPE      = "[Tt][Yy][Pp][Ee]";

    protected List<Parameter>     myParameterList   = new ArrayList<Parameter>();
    protected List              myFieldList       = new ArrayList();
    protected String            myName;
    protected Position          myPosition;
    protected Position          myLastPosition;
    
    protected SegmentType myType;
    
    /**
     * This is the type of the segment (which is a return type for methods).
     */
    protected String      myReturnType;
    
    /**
     * This is the pldoc/comment information for this segment.
     */
    protected String      myDocumentation;
    
    /**
     * This indicates whether the segment is public or private.
     */
    protected boolean     myIsPublic        = true;

    /**
     * This indicates whether the segment is a public synonym or not.
     */
    private boolean       myIsPublicSynonym = false;

    /**
     * This is the list of lines that are not the parameter declaration, the
     * return declaration, the IS/AS or END statement, but everything in between
     * (including the locals declaration).
     */
    protected List        myLines           = new ArrayList();
    
    /**
     * This is the segment that owns this segment. It may be null if no segment owns this segment.
     */
    protected Segment      myParent;

    /**
     * Created for bug 1387877.
     */
    private String        myTmpReturnType   = "";

    /**
     * This contains information that is used by the {@link plsqleditor.editors.PlSqlTextHover} class.
     */
    private Object        myReferredData;
    
    protected static class Parameter implements Comparable
    {
        public String myParameter;
        public String myInOut;
        public String myParamType;
        public String myExtraDetails;
        public int    myOffset;

        Parameter(String param, String inout, String type, String extraDetails, int offset)
        {
            myParameter = param;
            myInOut = (inout == null ? "" : inout);
            myParamType = type;
            myExtraDetails = extraDetails;
            myOffset = offset;
        }

        public String toString()
        {
            return toString(false);
        }

        public String toString(boolean overrideParameterSettings)
        {
            StringBuffer sb = new StringBuffer();
            if (overrideParameterSettings || isShowingParameterNames())
            {
                sb.append(String.valueOf(myParameter).toLowerCase());
            }
            if (overrideParameterSettings || isShowingInOut())
            {
                if (overrideParameterSettings || isShowingParameterNames())
                {
                    sb.append(" ");
                }
                if (myInOut == null || myInOut.trim().length() == 0)
                {
                    sb.append("IN");
                }
                else
                {
                    sb.append(myInOut);
                }
            }
            if (overrideParameterSettings || isShowingParameterTypes())
            {
                if (overrideParameterSettings || isShowingParameterNames() || isShowingInOut())
                {
                    sb.append(" ");
                }
                sb.append(myParamType);
                if (overrideParameterSettings)
                {
                    if (myExtraDetails.length() > 0)
                    {
                        sb.append(" ").append(myExtraDetails);
                    }
                }
            }

            return sb.toString();
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object object)
        {
            if (!(object instanceof Segment))
            {
                return false;
            }
            Parameter rhs = (Parameter) object;
            return new EqualsBuilder().append(this.myParameter, rhs.myParameter)
                    .append(this.myInOut, rhs.myInOut).append(this.myParamType, rhs.myParamType)
                    .append(this.myOffset, rhs.myOffset).isEquals();
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return new HashCodeBuilder(23, 397).append(this.myParameter).append(this.myInOut)
                    .append(this.myParamType).append(this.myOffset).toHashCode();
        }

        /**
         * A standard comparTo implementation.
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o)
        {
            Parameter rhs = (Parameter) o;
            return new CompareToBuilder().append(this.myParameter, rhs.myParameter)
                    .append(this.myInOut, rhs.myInOut).append(this.myParamType, rhs.myParamType)
                    .append(this.myOffset, rhs.myOffset).toComparison();
        }

        /**
         * This method identifies whether the Segment is showing the in/out indicator.
         * 
         * @return whether or not to show the in out type.
         */
        protected boolean isShowingInOut()
        {
            return prefs().getBoolean(PreferenceConstants.P_IS_SHOWING_PARAMETER_IN_OUT);
        }

        /**
         * This method identifies whether the Segment is showing the parameter types. 
         * 
         * @return whether or not to show the parameter types.
         */
        protected boolean isShowingParameterTypes()
        {
            return prefs().getBoolean(PreferenceConstants.P_IS_SHOWING_PARAMETER_TYPE);
        }

        /**
         * This method
         * 
         * @return whether or not to show the parameter names.
         */
        boolean isShowingParameterNames()
        {
            return prefs().getBoolean(PreferenceConstants.P_IS_SHOWING_PARAMETER_NAME);
        }
    }

    public Object clone()
    {
        Segment clone = new Segment(getName(), getPosition(), getType());
        clone.setDocumentation(getDocumentation());
        clone.setLastPosition(myLastPosition);
        clone.myFieldList = myFieldList;
        clone.myIsPublic = myIsPublic;
        clone.myParameterList = myParameterList;
        clone.myReturnType = myReturnType;
        clone.myLines = myLines;
        clone.myParent = myParent;
        return clone;
    }

    public Segment(String name, Position position)
    {
        String tmpName = name.toUpperCase();
        if (tmpName.indexOf("FUNCTION") != -1)
        {
            myType = SegmentType.Function;
        }
        else if (tmpName.indexOf("PROCEDURE") != -1)
        {
            myType = SegmentType.Procedure;
        }
        else if (tmpName.indexOf("TYPE") != -1)
        {
            myType = SegmentType.Type;
        }
        else if (tmpName.indexOf("PRAGMA") != -1)
        {
            myType = SegmentType.Pragma;
        }
        else
        {
            myType = SegmentType.Field;
        }
        this.myName = name.replaceFirst("(" + FUNCTION + "|" + PROCEDURE + ") +(\\w+)", "$2");
        this.myName = this.myName.replaceFirst("(" + TYPE + ")? +(\\w+).*", "$2");
        this.myName = this.myName.replaceFirst("(" + PRAGMA + ")? +(\\w+).*", "$2");
        this.myPosition = position;
        this.myLastPosition = position;
        this.myReturnType = "";
        this.myDocumentation = "";
    }

    public Segment(String name, Position position, SegmentType type)
    {
        this.myName = name;
        this.myPosition = position;
        this.myLastPosition = position;
        this.myType = type;
        this.myReturnType = "";
        this.myDocumentation = "";
    }

    public String getPresentationName(boolean isShowingParameterList,
                                      boolean isShowingReturnType,
                                      boolean overrideParameterSettings)
    {
        return myName.toLowerCase()
                + (isShowingParameterList
                        ? getParameterListAsString(overrideParameterSettings)
                        : "")
                + (isShowingReturnType && isValidReturnTypeType() ? " : " + myReturnType : "");
    }

    /**
     * @return whether this segment should have a return type definition.
     */
    private boolean isValidReturnTypeType()
    {
        return myType == SegmentType.Function || myType == SegmentType.Type
                || myType == SegmentType.SubType || myType == SegmentType.Field
                || myType == SegmentType.Constant;
    }

    public String toString()
    {
        return getPresentationName(isShowingParameterList(), isShowingReturnType(), false);
    }

    /**
     * This method gets all the parameters from the segment.
     * 
     * @return The parameter list in the format (p1,p2,p3).
     */
    public String getParameterListAsString(boolean overrideParameterSettings)
    {
        if (myParameterList.isEmpty())
        {
            return "";
        }
        StringBuffer sb = new StringBuffer("(");
        for (Iterator it = myParameterList.iterator(); it.hasNext();)
        {
            Parameter p = (Parameter) it.next();
            sb.append(p.toString(overrideParameterSettings));
            if (it.hasNext())
            {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * This method gets all the parameters from the segment.
     * 
     * @return The parameter list (as {@link Segment}s) in the format
     *         (p1,p2,p3).
     */
    public List<? extends Segment> getParameterList()
    {
        List<Segment> toReturn = new ArrayList<Segment>();

        for (Iterator<Parameter> it = myParameterList.iterator(); it.hasNext();)
        {
            Parameter p = it.next();
            Position pos = getPosition();
            Segment s = new Segment(p.myParameter, pos);
            s.myPosition = new Position(p.myOffset);
            s.myPosition.length = p.myParameter.length();
            s.setParent(this);
            s.setReturnType(p.myParamType);
            if (p.myExtraDetails.length() > 0)
            {
                s.addLine(p.myExtraDetails, new Position(pos.offset + p.myParameter.length()
                        + p.myParamType.length() + 2, p.myExtraDetails.length()));
            }
            String doc = getDocumentation();
            Pattern pattern = Pattern.compile("@param\\s+" + p.myParameter + "([^@]*)");
            Matcher m = pattern.matcher(doc);
            if (m.find())
            {
                String documentation = m.group(1);
                documentation = documentation.replaceAll("\\s+\\*\\s+(.*)", "\n $1");
                s.setDocumentation(documentation);
            }
            toReturn.add(s);
        }
        return toReturn;
    }

    /**
     * This method gets all the fields from the segment.
     * 
     * @return {@link #myFieldList}.
     */
    public List getFieldList()
    {
        return myFieldList;
    }

    /**
     * This method
     * 
     * @return The preferences store.
     */
    static IPreferenceStore prefs()
    {
        return PlsqleditorPlugin.getDefault().getPreferenceStore();
    }

    /**
     * This method
     * 
     * @return whether or not to show the return type in the parameter list.
     */
    protected boolean isShowingReturnType()
    {
        return prefs().getBoolean(PreferenceConstants.P_IS_SHOWING_RETURN_TYPE);
    }

    /**
     * This method
     * 
     * @return whether or not to show the parameter list.
     */
    boolean isShowingParameterList()
    {
        return prefs().getBoolean(PreferenceConstants.P_IS_SHOWING_PARAMETER_LIST);
    }

    /**
     * This method
     * 
     * @return {@link #myPosition}.
     */
    public Position getPosition()
    {
        return myPosition;
    }

    /**
     * This method
     * 
     * @return {@link #myType}.
     */
    public SegmentType getType()
    {
        return myType;
    }

    /**
     * This method adds a parameter to this segment. This should only be called
     * if this is a Procedure or Function.
     * 
     * @param paramName
     * @param paramInOut
     * @param paramType
     */
    public void addParameter(String paramName,
                             String paramInOut,
                             String paramType,
                             String extraDetails,
                             int offset)
    {
        myParameterList.add(new Parameter(paramName, paramInOut, paramType, extraDetails, offset));
    }

    /**
     * This method adds a field to this segment. This should only be called if
     * this is a Function or Procedure.
     * 
     * @param field The segment field to add to the list of fields.
     */
    public void addLocalField(Segment field)
    {
        myFieldList.add(field);
    }

    /**
     * This method sets the return type. It is only valid if the segment is for
     * a function.
     * 
     * @param returnType
     */
    public void setReturnType(String returnType)
    {
        myReturnType = returnType;
    }

    public boolean contains(int documentOffset)
    {
        return myPosition.getOffset() <= documentOffset
                && myLastPosition.getOffset() + myLastPosition.getLength() >= documentOffset;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object)
    {
        if (!(object instanceof Segment))
        {
            return false;
        }
        Segment rhs = (Segment) object;
        return new EqualsBuilder().append(this.myType, rhs.myType).append(this.myName, rhs.myName)
                .append(this.myParameterList, rhs.myParameterList).append(this.myPosition,
                                                                          rhs.myPosition)
                .append(this.myPosition.getOffset(), rhs.myPosition.getLength())
                .append(this.myLastPosition.getOffset(), rhs.myLastPosition.getLength())
                .append(this.myReturnType, rhs.myReturnType).append(this.myDocumentation,
                                                                    rhs.myDocumentation)
                .append(this.myIsPublic, rhs.myIsPublic).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(this.myType).append(this.myName)
                .append(this.myParameterList).append(this.myPosition.getOffset())
                .append(this.myPosition.getLength()).append(this.myLastPosition.getOffset())
                .append(this.myLastPosition.getLength()).append(this.myReturnType)
                .append(this.myDocumentation).append(this.myIsPublic).toHashCode();
    }

    /**
     * A standard comparTo implementation.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        Segment rhs = (Segment) o;
        return new CompareToBuilder().append(this.myType, rhs.myType).append(this.myName,
                                                                             rhs.myName)
                .append(this.myParameterList.toArray(new Parameter[myParameterList.size()]),
                        rhs.myParameterList.toArray(new Parameter[rhs.myParameterList.size()]))
                .append(this.myPosition.getOffset(), rhs.myPosition.getLength())
                .append(this.myLastPosition.getOffset(), rhs.myLastPosition.getLength())
                .append(this.myReturnType, rhs.myReturnType).append(this.myDocumentation,
                                                                    rhs.myDocumentation)
                .append(this.myIsPublic, rhs.myIsPublic).toComparison();
    }

    /**
     * This method returns the name.
     * 
     * @return {@link #myName}.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * This method returns the returnType.
     * 
     * @return {@link #myReturnType}.
     */
    public String getReturnType()
    {
        return myReturnType;
    }

    /**
     * This method sets the last position.
     * 
     * @param lastPosition The lastPosition to set.
     */
    public void setLastPosition(Position lastPosition)
    {
        myLastPosition = lastPosition;
    }

    /**
     * This method returns whether this segment is a public synonym or not.
     * 
     * @return {@link #myIsPublicSynonym}.
     */
    public boolean isPublicSynonym()
    {
        return myIsPublicSynonym;
    }

    /**
     * This method sets the public synonym status of the Segment. It can be used
     * to represent whether a package is a public synonym or not.
     * 
     * @param isPublicSynonym Whether this segment is a public synonym or not.
     */
    public void setPublicSynonym(boolean isPublicSynonym)
    {
        myIsPublicSynonym = isPublicSynonym;
    }

    /**
     * This method returns whether this segment is private or public in terms of
     * whether it appears in a header or not.
     * 
     * @return {@link #myIsPublic}.
     */
    public boolean isPublic()
    {
        return myIsPublic;
    }

    /**
     * This method sets the public status of the Segment. It can be used to
     * represent whether a package is a public synonym or not.
     * 
     * @param isPublic The isPublic to set.
     */
    public void setPublic(boolean isPublic)
    {
        myIsPublic = isPublic;
    }

    /**
     * @return Returns the documentation.
     */
    public String getDocumentation(boolean includeName)
    {
        return includeName ? getName() + "\n" + myDocumentation : myDocumentation;
    }

    /**
     * @return Returns the documentation.
     */
    public String getDocumentation()
    {
        return getDocumentation(false);
    }

    /**
     * @param documentation The documentation to set.
     */
    public void setDocumentation(String documentation)
    {
        myDocumentation = documentation;
        if (myDocumentation == null)
        {
            setPublic(false);
        }
        else if (myDocumentation.trim().startsWith("/**"))
        {
            if (myDocumentation.indexOf("@private") != -1)
            {
                setPublic(false);
            }
            else
            {
                setPublic(true);
            }
        }
        else
        {
            setPublic(false);
        }
    }

    /**
     * This method indicates whether a parameter of the supplied
     * <code>paramName</code> is already stored in this Segment.
     * 
     * @param paramName The name of the parameter being sought.
     * 
     * @return <code>true</code> if there is a parameter called
     *         <code>paramName</code> already stored in this segment, and
     *         <code>false</code> otherwise.
     */
    public boolean containsParameter(String paramName)
    {
        for (Iterator it = myParameterList.iterator(); it.hasNext();)
        {
            Parameter p = (Parameter) it.next();
            if (p.myParameter.equals(paramName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method adds the supplied <code>line</code> to the list of
     * {@link #myLines}.
     * 
     * @param line
     */
    public void addLine(String line, Position p)
    {
        myLines.add(new Segment(line, p, SegmentType.Code));
    }

    /**
     * @return The formatted string representing the method.
     */
    public String format()
    {
        StringBuffer sb = new StringBuffer(getDocumentation(false));

        SegmentType type = getType();
        if (type == SegmentType.Code || type == SegmentType.Label)
        {
            sb.append(getName()).append("\n");
        }
        else if (type == SegmentType.Package)
        {
            sb.append("CREATE OR REPLACE PACKAGE BODY ");
            sb.append(getName()).append(" ");
            sb.append("IS").append("\n");
        }
        else if (type == SegmentType.Field)
        {
            sb.append(getName()).append(" ").append(getReturnType());
        }
        else if (type == SegmentType.Constant)
        {
            sb.append(getName()).append(" ").append(type).append(" ").append(getReturnType());
        }
        else if (type == SegmentType.Type || type == SegmentType.SubType)
        {
            sb.append(type).append(" ").append(getName()).append(" IS ").append(getReturnType());
        }
        else if (type == SegmentType.Procedure || type == SegmentType.Function)
        {
            sb.append("    ").append(type.toString().toUpperCase()).append(" ");
            sb.append(getName());
            List l = myParameterList;
            if (l.size() > 0)
            {
                sb.append("(");
                for (Iterator it = l.iterator(); it.hasNext();)
                {
                    Parameter param = (Parameter) it.next();
                    sb.append(param.myParameter).append(" ").append(param.myInOut).append(" ")
                            .append(param.myParamType);
                    if (param.myExtraDetails.length() > 0)
                    {
                        sb.append(" ").append(param.myExtraDetails);
                    }
                    if (it.hasNext())
                    {
                        sb.append("\n    ,");
                    }
                }
                sb.append(")");
            }
            sb.append("\n");
            String returnType = getReturnType();
            if (returnType != null && returnType.length() > 0)
            {
                sb.append("    ").append("RETURN ").append(returnType).append("\n");
            }
            sb.append("    ").append("IS\n");
            sb.append("    ").append("    ").append("\n");
        }
        parseLines(sb, "    ");
        if (type == SegmentType.Field || type == SegmentType.Constant || type == SegmentType.Type
                || type == SegmentType.SubType)
        {
            sb.append(";");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * @param sb
     * @param string
     */
    private void parseLines(StringBuffer sb, String string)
    {
        for (Iterator it = myLines.iterator(); it.hasNext();)
        {
            Segment line = (Segment) it.next();
            // String line = (String) it.next();
            sb.append(line.getName());
            if (it.hasNext())
            {
                sb.append("\n");
            }
        }
    }

    // private CodeBlock parseBlock(Iterator it, String blockStarter, Segment
    // currentLine, Matcher
    // currentMatcher)
    // {
    // final String WORD = "[\\w\\._]+";
    // final String OPERATOR = "[+-*/]";
    // final String ASSIGNMENT = ":=";
    // final String STATEMENT_SEPARATOR = ";";
    // final String BRACKET = "[()]";
    // final String STRING_CONCATENATOR = "\\|\\|";
    //        
    // final String TOKEN =
    // "(" + WORD + "|" +
    // OPERATOR + "|" +
    // ASSIGNMENT + "|" +
    // STATEMENT_SEPARATOR + "|" +
    // BRACKET + "|" +
    // STRING_CONCATENATOR + ")";
    //        
    // while (it.hasNext())
    // {
    // Segment line = (Segment) it.next();
    // Pattern p = Pattern.compile(TOKEN);
    // Matcher m = p.matcher(line);
    // while (m.matches())
    // {
    // String token = m.group(1);
    // if (myCurrentBlock != null)
    // {
    // if (token.equals(STATEMENT_SEPARATOR))
    // {
    // // set start position, length, store statement
    // myCode.addStatement(myCurrentBlock);
    // myCurrentBlock = null;
    // }
    // }
    // else
    // {
    // if (isTokenBlockStarter(token, line))
    // {
    // CodeBlock cb = parseBlock(it, token, line, m);
    // myCode.addStatement(cb);
    // // get new end point from cb
    // }
    // else
    // {
    // myCurrentBlock = new CodeBlock(line.getPosition());
    // myCurrentBlock.addToken(token, m.start(1), m.end(1));
    // }
    // }
    // if (myCurrentBlock != null)
    // {
    // myCurrentBlock.incrementLine();
    // }
    // }
    // }
    // }

    /**
     * @return the signature that determines whether this segment is
     *         equivalently represented by another segment.
     */
    public String getSignature()
    {
        return getType() + " " + getPresentationName(true, true, true);
    }

    public List<? extends Segment> getContainedSegments()
    {
        List<? extends Segment> containedSegments = getParameterList();
        containedSegments.addAll(myFieldList);
        return containedSegments;
    }

    public Segment getParent()
    {
        return myParent;
    }

    protected void setParent(Segment parent)
    {
        myParent = parent;
    }

    protected Position getLastPosition()
    {
        return myLastPosition;
    }

    public String getImageKey()
    {
        return getType().toString() + (isPublic() ? Segment.PUBLIC : Segment.PRIVATE);
    }

    public void setTmpReturnType(String rtType)
    {
        myTmpReturnType = rtType;
    }

    public String getTmpReturnType()
    {
        return myTmpReturnType;
    }

    /**
     * This method sets the name of the package. WARNING do not use this method -
     * it is dangerous.
     * 
     * @param name The new name of the package.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * This method sets the referenced object, which will be the actual data
     * that the segment is referring to. It is used in cases such as type
     * information such as table data.
     * 
     * @param referredData The referred data to set.
     */
    public void setReferredData(Object referredData)
    {
        myReferredData = referredData;
    }

    /**
     * This method returns the referred data.
     * 
     * @return {@link #myReferredData}
     */
    public Object getReferredData()
    {
        return myReferredData;
    }
    
    /**
     * This method determines what type of source this segment comes from.
     * Essentially it is determining whether this source comes from a package body
     * or header.
     * 
     * @return the source type of this segment - either <br>
     * <ul>
     * <li>SegmentType.Package</li>
     * <li>SegmentType.Package_Body</li>
     * <li>SegmentType.Code - if the parent type could not be determined.</li>
     * </ul>
     */
    public SegmentType deriveSourceSegmentType()
    {
        if (myType == SegmentType.Package ||
                myType == SegmentType.Package_Body)
        {
            return myType;
        }
        else if (myParent != null)
        {
            return myParent.deriveSourceSegmentType();
        }
        else
        {
            // TODO check what the best "default" return type is
            return SegmentType.Code;
        }
    }
 }
