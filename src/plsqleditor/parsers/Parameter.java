package plsqleditor.parsers;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import plsqleditor.preferences.PreferenceConstants;

public class Parameter implements Comparable<Object>
{
	public String myParameter;
	public String myInOut;
	public String myParamType;
	public String myExtraDetails;
	public int myOffset;

	Parameter(String param, String inout, String type, String extraDetails,
			int offset)
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
			if (overrideParameterSettings || isShowingParameterNames()
					|| isShowingInOut())
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
		return new EqualsBuilder()
				.append(this.myParameter, rhs.myParameter)
				.append(this.myInOut, rhs.myInOut)
				.append(this.myParamType, rhs.myParamType)
				.append(this.myOffset, rhs.myOffset).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return new HashCodeBuilder(23, 397).append(this.myParameter)
				.append(this.myInOut).append(this.myParamType)
				.append(this.myOffset).toHashCode();
	}

	/**
	 * A standard comparTo implementation.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o)
	{
		Parameter rhs = (Parameter) o;
		return new CompareToBuilder()
				.append(this.myParameter, rhs.myParameter)
				.append(this.myInOut, rhs.myInOut)
				.append(this.myParamType, rhs.myParamType)
				.append(this.myOffset, rhs.myOffset).toComparison();
	}

	/**
	 * This method identifies whether the Segment is showing the in/out
	 * indicator.
	 * 
	 * @return whether or not to show the in out type.
	 */
	protected boolean isShowingInOut()
	{
		return Segment.prefs().getBoolean(
				PreferenceConstants.P_IS_SHOWING_PARAMETER_IN_OUT);
	}

	/**
	 * This method identifies whether the Segment is showing the parameter
	 * types.
	 * 
	 * @return whether or not to show the parameter types.
	 */
	protected boolean isShowingParameterTypes()
	{
		return Segment.prefs().getBoolean(
				PreferenceConstants.P_IS_SHOWING_PARAMETER_TYPE);
	}

	/**
	 * This method
	 * 
	 * @return whether or not to show the parameter names.
	 */
	boolean isShowingParameterNames()
	{
		return Segment.prefs().getBoolean(
				PreferenceConstants.P_IS_SHOWING_PARAMETER_NAME);
	}
}