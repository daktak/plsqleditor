/*
 * Created on 27/02/2005
 *
 * @version $Id$
 */
package plsqleditor.parsers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Position;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.preferences.PreferenceConstants;


public class Segment implements Comparable
{
    public enum SegmentType
    {
        Schema, Package, Type, Field, Function, Procedure, Label
    }

    static class Parameter implements Comparable
    {
        public String myParameter;
        public String myInOut;
        public String myParamType;

        Parameter(String param, String inout, String type)
        {
            myParameter = param;
            myInOut = inout;
            myParamType = type;
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
                sb.append(myParameter);
            }
            if (overrideParameterSettings || isShowingInOut())
            {
                if (overrideParameterSettings || isShowingParameterNames())
                {
                    sb.append(" ");
                }
                sb.append(myInOut);
            }
            if (overrideParameterSettings || isShowingParameterTypes())
            {
                if (overrideParameterSettings || isShowingParameterNames() || isShowingInOut())
                {
                    sb.append(" ");
                }
                sb.append(myParamType);
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
                    .isEquals();
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return new HashCodeBuilder(23, 397).append(this.myParameter).append(this.myInOut)
                    .append(this.myParamType).toHashCode();
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
                    .toComparison();
        }

        /**
         * This method
         * 
         * @return whether or not to show the in out type.
         */
        boolean isShowingInOut()
        {
            return prefs().getBoolean(PreferenceConstants.P_IS_SHOWING_PARAMETER_IN_OUT);
        }

        /**
         * This method
         * 
         * @return whether or not to show the parameter types.
         */
        boolean isShowingParameterTypes()
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


    List<Parameter> myParameterList = new ArrayList<Parameter>();
    List<Segment>   myFieldList     = new ArrayList<Segment>();
    String          myName;
    Position        myPosition;
    Position        myLastPosition;
    SegmentType     myType;
    String          myReturnType;
    boolean         myIsPublic = false;

    public Segment(String name, Position position)
    {
        if (name.contains("FUNCTION"))
        {
            myType = SegmentType.Function;
        }
        else if (name.contains("PROCEDURE"))
        {
            myType = SegmentType.Procedure;
        }
        else if (name.contains("TYPE"))
        {
            myType = SegmentType.Type;
        }
        else
        {
            myType = SegmentType.Field;
        }
        this.myName = name.replaceFirst("(FUNCTION|PROCEDURE) +(\\w+)", "$2");
        this.myName = this.myName.replaceFirst("(TYPE)? +(\\w+).*", "$2");
        this.myPosition = position;
        this.myLastPosition = position;
        this.myReturnType = "";
    }

    public Segment(String name, Position position, SegmentType type)
    {
        this.myName = name;
        this.myPosition = position;
        this.myLastPosition = position;
        this.myType = type;
        this.myReturnType = "";
    }

    public String getPresentationName(boolean isShowingParameterList,
                                      boolean isShowingReturnType,
                                      boolean overrideParameterSettings)
    {
        return myName
                + (isShowingParameterList
                        ? getParameterListAsString(overrideParameterSettings)
                        : "")
                + (isShowingReturnType
                        && (myType == SegmentType.Function || myType == SegmentType.Type || myType == SegmentType.Field)
                        ? " : " + myReturnType
                        : "");
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
    String getParameterListAsString(boolean overrideParameterSettings)
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
     * @return The parameter list in the format (p1,p2,p3).
     */
    public List<Segment> getParameterList()
    {
        List<Segment> toReturn = new ArrayList<Segment>();

        for (Parameter p : myParameterList)
        {
            Segment s = new Segment(p.myParameter, getPosition());
            s.setReturnType(p.myParamType);
            toReturn.add(s);
        }
        return toReturn;
    }

    /**
     * This method gets all the fields from the segment.
     * 
     * @return {@link #myFieldList}.
     */
    public List<Segment> getFieldList()
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
    boolean isShowingReturnType()
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
     * This method adds a parameter to this segment. This should only be called if this is a
     * Procedure or Function.
     * 
     * @param paramName
     * @param paramInOut
     * @param paramType
     */
    public void addParameter(String paramName, String paramInOut, String paramType)
    {
        myParameterList.add(new Parameter(paramName, paramInOut, paramType));
    }

    /**
     * This method adds a field to this segment. This should only be called if this is a Function or
     * Procedure.
     * 
     * @param name
     * @param paramInOut
     * @param paramType
     */
    public void addLocalField(Segment field)
    {
        myFieldList.add(field);
    }

    /**
     * This method sets the return type. It is only valid if the segment is for a function.
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
                .append(this.myReturnType, rhs.myReturnType).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(this.myType).append(this.myName)
                .append(this.myParameterList).append(this.myPosition.getOffset())
                .append(this.myPosition.getLength()).append(this.myLastPosition.getOffset())
                .append(this.myLastPosition.getLength()).append(this.myReturnType).toHashCode();
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
                .append(this.myReturnType, rhs.myReturnType).toComparison();
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
     * This method sets the ...
     * 
     * @param lastPosition The lastPosition to set.
     */
    public void setLastPosition(Position lastPosition)
    {
        myLastPosition = lastPosition;
    }

    /**
     * This method returns whether this segment is public or not.  It can
     * be used to represent whether a package is a public synonym or not.
     * 
     * @return {@link #myIsPublic}.
     */
    public boolean isPublic()
    {
        return myIsPublic;
    }

    /**
     * This method sets the public status of the Segment.  It can
     * be used to represent whether a package is a public synonym or not.
     *
     * @param isPublic The isPublic to set.
     */
    public void setPublic(boolean isPublic)
    {
        myIsPublic = isPublic;
    }
}
