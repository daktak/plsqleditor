/**
 * 
 */
package plsqleditor.stores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import plsqleditor.parsers.AbstractPlSqlParser;
import plsqleditor.parsers.ParseType;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 *          Created on 8/03/2005
 * 
 */
public class PlSqlPackage
{
	private String myName;
	private List<Segment> mySegments;
	private PlSqlSchema mySchema;

	/**
	 * This is a list of {@link Source}s that are indexed by their
	 * {@link ParseType}.
	 */
	private TreeMap<ParseType, Source> mySources = new TreeMap<ParseType, Source>();

	/**
	 * This field contains the modification timestamps of the file from which
	 * the last segment modification to this object came.
	 */
	private Map<ParseType,Long> myLatestChanges = new HashMap<ParseType,Long>();

	public PlSqlPackage(PlSqlSchema owner, String name, Source source,
			ParseType parseType)
	{
		mySchema = owner;
		myName = name;
		mySources.put(parseType, source);
		mySegments = new ArrayList<Segment>();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object)
	{
		if (!(object instanceof PlSqlPackage))
		{
			return false;
		}
		PlSqlPackage rhs = (PlSqlPackage) object;
		return new EqualsBuilder().append(this.myName, rhs.myName).append(
				this.mySegments, rhs.mySegments).append(this.mySources,
				rhs.mySources).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return new HashCodeBuilder(23, 397).append(this.myName).append(
				this.mySegments).append(this.mySources).toHashCode();
	}

	/**
	 * A standard comparTo implementation.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o)
	{
		PlSqlPackage rhs = (PlSqlPackage) o;
		return new CompareToBuilder().append(this.myName, rhs.myName).append(
				this.mySegments, rhs.mySegments).append(this.mySources,
				rhs.mySources).toComparison();
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
	 * This method returns the segments.
	 * 
	 * @return {@link #mySegments}.
	 */
	public List<Segment> getSegments()
	{
		return mySegments;
	}

	/**
	 * This method sets the segments for this package.
	 * 
	 * @param segments
	 *            The segments to set.
	 */
	public void setSegments(List<Segment> segments, long timestamp)
	{
		mySegments = segments;
		if (mySegments.size() > 0)
		{
			Segment first = (Segment) mySegments.get(0);
			setTimestamp(first, timestamp);
		}
	}

	/**
	 * This method sets the timestamp of the last modification of the contained
	 * segments in this package from a particular source type (derived from the
	 * supplied <code>segment</code>).
	 * 
	 * @param segment
	 *            The segment whose derived segment type will be used to
	 *            determine the type of plsql code whose segments are being
	 *            updated.
	 * 
	 * @param timestamp
	 *            The modification time of the supplied <code>segment</code>.
	 */
	private void setTimestamp(Segment segment, long timestamp)
	{
		SegmentType segmentType = segment.deriveSourceSegmentType();
		ParseType parseType = segmentType == SegmentType.Package ? ParseType.Package
				: segmentType == SegmentType.Package_Body ? ParseType.Package_Body
						: null;
		if (parseType != null)
		{
			Long stamp = (Long) myLatestChanges.get(parseType);
			if (stamp == null || timestamp > stamp.longValue())
			{
				stamp = new Long(timestamp);
				myLatestChanges.put(parseType, stamp);
			}
		}
	}

	/**
	 * This method returns the sourceType.
	 * 
	 * @return {@link #mySource}.
	 */
	public Source getPrimarySource()
	{
		Source toReturn = null;
		ParseType toReturnType = null;
		for (Iterator<ParseType> it = mySources.keySet().iterator(); it
				.hasNext();)
		{
			ParseType type = (ParseType) it.next();
			Source source = mySources.get(type);
			if (toReturn == null)
			{
				toReturn = source;
				toReturnType = type;
			}
			if (toReturn != null && toReturn.getType() == PersistenceType.File
					&& toReturnType == ParseType.Package_Body)
			{
				break;
			}
			if (source.getType() == PersistenceType.File)
			{
				if (type == ParseType.Package_Body)
				{
					return source;
				}
				toReturn = source;
			}
		}
		return toReturn;
	}

	public Source getSource(ParseType parseType)
	{
		return (Source) mySources.get(parseType);
	}

	/**
	 * This method adds another source to the package, identifying its type.
	 * Currently it is assumed there will be at most two sources, a package body
	 * and a package header.
	 * 
	 * @param source
	 * @param parseType
	 */
	public void addSource(Source source, ParseType parseType)
	{
		mySources.put(parseType, source);
	}

	public boolean add(Segment segment, long timestamp)
	{
		setTimestamp(segment, timestamp);
		return mySegments.add(segment);
	}

	/**
	 * This method determines whether the supplied <code>segment</code> is
	 * contained in the list of <code>segments</code>.
	 * 
	 * @param segment
	 *            The segment whose membership in the list of
	 *            <code>segments</code> is being checked.
	 * 
	 * @param segments
	 *            The list of segments which may contain the supplied
	 *            <code>segment</code>.
	 * 
	 * @return <code>true</code> if the <code>segment</code> is contained in the
	 *         list of <code>segments</code>.
	 */
	private boolean contains(Segment segment, List<Segment> segments)
	{
		String signature = segment.getSignature();
		for (Segment toCheck : segments)
		{
			if (toCheck.getSignature().equals(signature))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * This method checks to see if {@link #mySegments} contains the supplied
	 * <code>segment</code>.
	 * 
	 * @param segment
	 *            The segment whose membership in the list of
	 *            <code>segments</code> is being checked.
	 * 
	 * @return <code>true</code> if the <code>segment</code> is contained in
	 *         {@link #mySegments}.
	 */
	public boolean contains(Segment segment)
	{
		return contains(segment, mySegments);
	}

	/**
	 * This method returns the schema.
	 * 
	 * @return {@link #mySchema}.
	 */
	public PlSqlSchema getSchema()
	{
		return mySchema;
	}

	/**
	 * This method sets the schema. It should only ever get called by a schema
	 * when this object is being added to it.
	 * 
	 * @param schema
	 *            The schema to set.
	 */
	void setSchema(PlSqlSchema schema)
	{
		mySchema = schema;
	}

	/**
	 * This method returns the latestChange.
	 * 
	 * @return {@link #myLatestChange}.
	 */
	public long getLatestChangeForType(ParseType type)
	{
		Long stored = (Long) myLatestChanges.get(type);
		long retval = -2;
		if (stored != null)
		{
			retval = stored.longValue();
		}
		return retval;
	}

	/**
	 * This method replaces a segment stored in the list
	 * <code>toSubstituteInto</code> with the segment <code>toSubstitute</code>.
	 * If there is no segment of the same name, nothing is done.
	 * 
	 * @param toSubstitute
	 *            The segment that will replace the previously stored
	 *            corresponding segment. It will replace a segement with the
	 *            same signature, as returned by a call to
	 *            {@link Segment#getSignature()}.
	 * 
	 * @param toSubstituteInto
	 *            The list to substitute the segment into.
	 * 
	 * @return <code>true</code> if a segment was actually replaced, and
	 *         <code>false</code> otherwise.
	 */
	private boolean replaceSameSegment(Segment toSubstitute,
			List<Segment> toSubstituteInto)
	{
		String signature = toSubstitute.getSignature();
		int size = toSubstituteInto.size();
		for (int i = 0; i < size; i++)
		{
			Segment toCheck = (Segment) toSubstituteInto.get(i);
			if (toCheck.getSignature().equals(signature))
			{
				toSubstituteInto.remove(i);
				toSubstituteInto.add(i, toSubstitute);
				return true;
			}
		}
		return false;
	}

	/**
	 * This method adds the supplied segments to the package, choosing whether
	 * to set them directly (because there are no existing segments) or add only
	 * the extra values (because the parsetype is not a package body, so only
	 * the differences should be added). If the segments to be added contain a
	 * package segment, then this will be used to obtain the list of segments to
	 * add instead. These segments will be added to the corresponding package
	 * level segment inside this package.
	 * 
	 * @param segments
	 *            The list of segments to reconcile with this package.
	 * 
	 * @param parseType
	 *            The type of the file from which these segments came.
	 * 
	 * @param timestamp
	 *            The timestamp of the file from which the <code>segments</code>
	 *            were obtained.
	 */
	public synchronized void reconcile(List<Segment> segments, ParseType parseType, long timestamp)
	{
		if (mySegments == null)
		{
			mySegments = segments;
		}
		else
		{
			Segment thisPackageSegment = AbstractPlSqlParser.getPackageSegment(
					mySegments, myName);
			List<Segment> thisPackagesSegments = mySegments;
			if (thisPackageSegment != null)
			{
				thisPackagesSegments = thisPackageSegment
						.getContainedSegments();
			}
			Segment packageSegment = AbstractPlSqlParser.getPackageSegment(
					segments, myName);

			List<Segment> segmentsToProcess = segments;
			if (packageSegment != null)
			{
				segmentsToProcess = packageSegment.getContainedSegments();
			}

			clearSegments(parseType, thisPackagesSegments);

			if (parseType != ParseType.Package_Body)
			{
				for (Iterator<Segment> it = segmentsToProcess.iterator(); it.hasNext();)
				{
					Segment s = it.next();
					if (!contains(s, thisPackagesSegments))
					{
						thisPackagesSegments.add(s);
						setTimestamp(s, timestamp);
					}
				}
			}
			else
			{
				// fix for 1441828 - Package constants not shown in completion
				// proposals
				for (Iterator<Segment> it = segmentsToProcess.iterator(); it.hasNext();)
				{
					Segment s = it.next();
					if (contains(s, thisPackagesSegments))
					{
						replaceSameSegment(s, thisPackagesSegments);
					}
					else
					{
						thisPackagesSegments.add(s);
					}
					setTimestamp(s, timestamp);
				}
			}
		}
	}

	/**
	 * This method removes segments of a particular type from the supplied list
	 * of <code>segmentsToClear</code>.
	 * 
	 * @param parseType
	 *            The type of source whose segments are being cleared (for
	 *            update).
	 * 
	 * @param segmentsToClear
	 *            The list to clear of segments of the type associated with the
	 *            supplied <code>parseType</code>.
	 */
	private void clearSegments(ParseType parseType, List<Segment> segmentsToClear)
	{
		SegmentType toCompare = parseType == ParseType.Package ? SegmentType.Package
				: parseType == ParseType.Package_Body ? SegmentType.Package_Body
						: SegmentType.Code;
		for (int i = segmentsToClear.size() - 1; i >= 0; i--)
		{
			Segment segment = (Segment) segmentsToClear.get(i);
			if (segment.deriveSourceSegmentType() == toCompare)
			{
				segmentsToClear.remove(i);
			}
		}
	}

	/**
	 * This method indicates whether a particular type of information source has
	 * been used to populate segment data in this package.
	 * 
	 * @param parseType
	 *            The type of information source being checked.
	 * 
	 * @return <code>true</code> if the information source with the supplied
	 *         <code>parseType</code> has been used to store segments on this
	 *         object previously.
	 */
	public boolean isTypeStored(ParseType parseType)
	{
		return myLatestChanges.containsKey(parseType);
	}
}
