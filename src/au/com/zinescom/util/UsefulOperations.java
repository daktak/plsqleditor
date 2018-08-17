package au.com.zinescom.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class provides useful generic methods that are not supplied by the JDK,
 * but could come in handy in testing, or debugging, or printing diagnostic
 * informatin among other things.
 *
 * @author Toby Zines
 * @version
 */
public class UsefulOperations
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(UsefulOperations.class.getName());

    private UsefulOperations()
    {
        // do nothing
    }

    /**
     * This field represents an empty array of objects.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY           = new Object[0];

    /**
     * This field represents a full stop (".") character.
     */
    public static final String   DOT                          = ".";
    /**
     * This field represents a comma (",") character.
     */
    public static final String   COMMA                        = ",";
    /**
     * This field represents a hat or caret ("^") character.
     */
    public static final String   CARET                        = "^";
    /**
     * This field represents a space (" ") character.
     */
    public static final String   SPACE                        = " ";
    /**
     * This field represents a new line character.
     */
    public static final String   NEWLINE                      = "\n";
    /**
     * This field represents a comma (",") or a space (" ") character.
     */
    private static final String  SEPARATORS                   = COMMA + SPACE;

    /**
     * This field represents the default static singleton accessor for any
     * class.
     */
    public static final String   DEFAULT_INSTANCE_METHOD_NAME = "instance";
    /**
     * This is the value returned when the index of a string/char/int etc is not
     * found inside another string.
     *
     * @see #constructClass(Class, String, Object[], String)
     */
    private static final int     STRING_NOT_FOUND             = -1;

    /**
     * This is the method name of the toString method.
     */
    private static final String  TO_STRING_METHOD_NAME        = "toString";

    /**
     * This field is a hashtable of hashtables. The first table maps classes to
     * a hashtable of methods keyed on their names. Key : Class Value :
     * Hashtable Key : method name Value : Method
     */
    private static Hashtable<Class<?>, Hashtable<String, Method>>     theClassToMethodTable        = new Hashtable<Class<?>, Hashtable<String, Method>>();

    /**
     * This class represents a filter on a file list.
     *
     * @author Toby Zines
     *
     * Created on 28/01/2005
     */
    public static class FileListFilter implements FilenameFilter
    {
        /** This is the list of valid file extensions */
        private String[] myFilters;

        /**
         * This constructor creates the filter with the supplied list of filters
         * that are allowed.
         *
         * @param filterListAry the list of valid extensions. This must not
         *            contain any nulls, or be null, but it may be empty,
         *            indicating nothing is valid.
         */
        public FileListFilter(String[] filterListAry)
        {
            myFilters = filterListAry;
        }

        /**
         * This constructor creates the filter with the supplied list of filters
         * that are allowed.
         *
         * @param filterList the list of comma separated valid extensions. This
         *            may be empty, indicating nothing is valid.
         */
        public FileListFilter(String filterList)
        {
            StringTokenizer st = new StringTokenizer(filterList, COMMA);
            String[] ary = new String[st.countTokens()];
            int count = 0;
            while (st.hasMoreTokens())
            {
                ary[count++] = st.nextToken();
            }
            myFilters = ary;
        }

        /**
         * This method returns true for any file that has an extension in
         * {@link #myFilters}.
         *
         * @param dir the directory in which the file was found.
         *
         * @param name the name of the file.
         *
         * @return <code>true</code> if the name should be included in the
         *         file list; <code>false</code> otherwise.
         */
        public boolean accept(File dir, String name)
        {
            for (int i = 0; i < myFilters.length; i++)
            {
                String suffix = myFilters[i];
                if (name.endsWith(suffix))
                {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * This method checks if two arrays contain the same data. The data does not
     * have to be in the same order, just the same number of objects, and each
     * one is "equal" according to the equals method.
     *
     * @param ary1 The first array.
     *
     * @param ary2 The second array
     *
     * @return Whether or not the two arrays contain the same data.
     */
    public static boolean arraysAreEqual(Object[] ary1, Object[] ary2)
    {
        if (ary1.length != ary2.length)
        {
            return false;
        }

        int size = ary1.length;

        Vector<Object> v1 = new Vector<Object>(size);
        Vector<Object> v2 = new Vector<Object>(size);

        for (int i = 0; i < size; i++)
        {
            v1.addElement(ary1[i]);
        }
        for (int i = 0; i < size; i++)
        {
            v2.addElement(ary2[i]);
        }

        for (int i = 0; i < size; i++)
        {
            int v2Size = v2.size();
            for (int j = 0; j < v2Size; j++)
            {
                if (v1.elementAt(i).equals(v2.elementAt(j)))
                {
                    v2.removeElementAt(j);
                    break;
                }
            }
        }
        return v2.isEmpty();
    }

    /**
     * This method turns an array of objects into a stringified format
     *
     * @param objs The objects being stringified. If this is null, or zero
     *            length, an empty string will be returned where the list would
     *            have been.
     *
     * @param isInBrackets This indicates whether you want square brackets
     *            around the string ( like this : [a,b,c] )
     *
     * @param isWithNewLines This indicates whether you want a new line for each
     *            entry or not.
     */
    public static String stringify(Object[] objs, boolean isInBrackets, boolean isWithNewLines)
    {
        StringBuffer sb = new StringBuffer();
        if (objs != null && objs.length > 0)
        {
            String endString = "";
            if (isInBrackets)
            {
                sb.append("[");
                endString = "]";
            }
            String toAppend = isWithNewLines ? ",\n" : ", ";

            sb.append(objs[0].toString());

            for (int i = 1; i < objs.length; i++)
            {
                sb.append(toAppend);
                sb.append(objs[i].toString());
            }
            sb.append(endString);
        }
        return sb.toString();
    }

    /**
     * This method adds one vector to another without duplication of elements
     *
     * @param toBeAdded the vector to be added. This may be null.
     * @param toAddTo the vector to add to. This MUST NOT be null.
     */
    public static final void addVectorToVector(Vector<? extends AbstractList<?>> toBeAdded, Vector<Object> toAddTo)
    {
        if (toBeAdded == null)
        {
            return;
        }

        int size = toBeAdded.size();

        for (int i = 0; i < size; i++)
        {
            Object object = toBeAdded.elementAt(i);
            if (!toAddTo.contains(object))
            // only store one instance of the object
            {
                toAddTo.addElement(object);
            }
        }
    }

    /**
     * This method adds one vector to another without duplication of elements
     *
     * @param toBeAdded the vector to be added. This may be null.
     * @param toAddTo the vector to add to. This MUST NOT be null.
     */
    public static final Vector<Object> addVectorToVectorE(Vector<?> toBeAdded, Vector<Object> toAddTo)
    {
        if (toBeAdded == null)
        {
            return null;
        }

        int size = toBeAdded.size();

        for (int i = 0; i < size; i++)
        {
            Object object = toBeAdded.elementAt(i);
            if (!toAddTo.contains(object))
            // only store one instance of the object
            {
                toAddTo.addElement(object);
            }
        }
        return toAddTo;
    }

    /**
     * This method converts an array of objects to a single string. It does this
     * by calling the supplied <code>method</code> on each object separating
     * each object with the given separator. If the supplied <code>method</code>
     * is not available, then toString() will be used.
     * <P>
     * If the separator is null, no separation is used.
     *
     * @param array the array of object to convert
     *
     * @param method The method to call in order to access the required data
     *            from the object. The accessed data will have toString() called
     *            on it. If this is null, toString will be used.
     *
     * @param separator the string to separate each object with
     *
     * @return a string representation of the array
     */
    public static String arrayToString(Object[] array, String method, String separator)
    {
        return UsefulOperations.arrayToString(array, method, separator, false);
    }

    /**
     * This method converts an array of objects to a single string. It does this
     * by calling the supplied <code>method</code> on each object separating
     * each object with the given separator. If the supplied <code>method</code>
     * is not available, then toString() will be used.
     * <P>
     * If the separator is null, no separation is used.
     *
     * @param array the array of object to convert
     *
     * @param method The method to call in order to access the required data
     *            from the object. The accessed data will have toString() called
     *            on it. If this is null, toString will be used.
     *
     * @param separator the string to separate each object with
     *
     * @return a string representation of the array
     */
    public static String arrayToString(Object[] array, String method, String separator, boolean sort)
    {
        if (array == null)
        {
            throw new IllegalArgumentException("array is null");
        }

        if (separator == null)
        {
            separator = "";
        }

        Method m = null;

        List<String> list = new ArrayList<String>();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < array.length; i++)
        {
            Object obj = array[i];
            if (obj == null)
            {
                continue;
            }
            if (method != null)
            {
                Class<?> clazz = obj.getClass();
                Hashtable<String, Method> methodTable = (Hashtable<String, Method>) theClassToMethodTable.get(clazz);
                if (methodTable != null)
                {
                    m = (Method) methodTable.get(method);
                }
                else
                {
                    methodTable = new Hashtable<String, Method>();
                    theClassToMethodTable.put(clazz, methodTable);
                }
                if (m == null)
                {
                    try
                    {
                        m = clazz.getMethod(method, new Class[0]);
                        methodTable.put(method, m);
                    }
                    catch (NoSuchMethodException nsme)
                    {
                        final String msg = "method [" + method + "] is not available for class ["
                                + clazz + "] (using toString()): " + nsme.getMessage();
                        if (logger.isLoggable(Level.FINEST))
                        {
                            logger.fine(msg);
                        }

                        try
                        {
                            m = clazz.getMethod(TO_STRING_METHOD_NAME, new Class[0]);
                        }
                        catch (NoSuchMethodException nsme2)
                        {
                            // there has to be a toString method
                            // it is defined on the Object class
                        }
                    }
                }
            }
            try
            {
                String value = m.invoke(obj, EMPTY_OBJECT_ARRAY).toString();
                buf.append(value);
                list.add(value);
            }
            catch (Exception e) // will catch NullPointer when m not set.
            {
                final String msg = "Unable to invoke method [" + m + "] on obj [" + obj + "]. : "
                        + e + ". Using toString() instead.";
                if (logger.isLoggable(Level.FINEST))
                {
                    logger.finest(msg);
                }
                String value = obj.toString();
                buf.append(value);
                list.add(value);
            }
            if (i < (array.length - 1))
            {
                buf.append(separator);
            }
        }
        if (sort)
        {
            Collections.sort(list);
            buf = new StringBuffer();
            for (Iterator<String> it = list.iterator(); it.hasNext();)
            {
                buf.append(it.next());
                if (it.hasNext())
                {
                    buf.append(separator);
                }
            }
        }
        return buf.toString();
    }

    /**
     * This method constructs a <code>Vector</code> from the specified array.
     *
     * @param array The source array.
     *
     * @return The <code>Vector</code> constructed from the array, or
     *         <code>null</code> if <code>array</code> is <code>null</code>.
     */
    public static Vector<Object> arrayToVector(Object[] array)
    {
        if (array == null)
        {
            return null;
        }

        int length = array.length;
        Vector<Object> v = new Vector<Object>();
        for (int i = 0; i < length; i++)
        {
            v.addElement(array[i]);
        }
        return v;
    }

    /**
     * This method adds one vector to another by doing a block append.
     *
     * @param toBeConcatenated the vector to be added. This may be null.
     *
     * @param toAddTo the vector to add to. This MUST NOT be null.
     */
    public static final void concatVectors(Vector<? extends AbstractList<?>> toBeConcatenated, Vector<Object> toAddTo)
    {
        if (toBeConcatenated == null)
        {
            return;
        }
        int size = toBeConcatenated.size();

        for (int i = 0; i < size; i++)
        {
            toAddTo.addElement(toBeConcatenated.elementAt(i));
        }
    }

    /**
     * This method returns a new instance of the desired object, instantiated
     * using the <code>constructionArgs</code> passed to it.
     *
     * @param requiredInstanceOf The type of class that this should be (either
     *            an interface, superinterface or class). Cannot be null,
     *            otherwise an IllegalArgumentException will be thrown.
     *
     * @param className The string name of the class. Cannot be null, otherwise
     *            an IllegalArgumentException will be thrown.
     *
     * @param constructionArgs The arguments to pass to the constructor - They
     *            must be in the correct order, and there must be a constructor
     *            with this signature. Cannot be null, otherwise an
     *            IllegalArgumentException will be thrown. They may be zero
     *            length.
     *
     * @param defaultPackageName This is the name of the package at which the
     *            className should be found if the search for the class name by
     *            itself fails. If this is null, there will be no second search.
     *
     * @return an instance of the object that needed to be constructed. It is
     *         guaranteed to be castable to the specified
     *         <code>requiredInstanceOf</code>.
     *
     * @throws CreationException when <br>
     *             <ul>
     *             <li>The class name cannot be found
     *             <li>When the constructor with the supplied args does not
     *             exist
     *             <li>When there is a security violation
     *             <li>When an exception is thrown by the underlying
     *             constructor
     *             <li>The constructed class is not of type
     *             <code>requiredInstanceOf</code>.
     *             </ul>
     *
     * @see #constructClass(Class, String, Object[], String[])
     */
    public static final Object constructClass(Class<?> requiredInstanceOf,
                                              String className,
                                              Object[] constructionArgs,
                                              String defaultPackageName) throws CreationException
    {
        return constructClass(requiredInstanceOf,
                              className,
                              constructionArgs,
                              new String[]{defaultPackageName});
    }

    /**
     * This method returns a new instance of the desired object, instantiated
     * using the <code>constructionArgs</code> passed to it.
     *
     * @param requiredInstanceOf The type of class that this should be (either
     *            an interface, superinterface or class). Cannot be null,
     *            otherwise an IllegalArgumentException will be thrown.
     *
     * @param className The string name of the class. Cannot be null, otherwise
     *            an IllegalArgumentException will be thrown.
     *
     * @param constructionArgs The arguments to pass to the constructor - They
     *            must be in the correct order, and there must be a constructor
     *            with this signature. Cannot be null, otherwise an
     *            IllegalArgumentException will be thrown. They may be zero
     *            length.
     *
     * @param defaultPackageNames This is a list of the package at which the
     *            className should be found if the search for the class name by
     *            itself fails. If this is null, there will be no extra
     *            searches.
     *
     * @return an instance of the object that needed to be constructed. It is
     *         guaranteed to be castable to the specified
     *         <code>requiredInstanceOf</code>.
     *
     * @throws CreationException when <br>
     *             <ul>
     *             <li>The class name cannot be found
     *             <li>When the constructor with the supplied args does not
     *             exist
     *             <li>When there is a security violation
     *             <li>When an exception is thrown by the underlying
     *             constructor
     *             <li>The constructed class is not of type
     *             <code>requiredInstanceOf</code>.
     *             </ul>
     */
    public static final Object constructClass(Class<?> requiredInstanceOf,
                                              String className,
                                              Object[] constructionArgs,
                                              String[] defaultPackageNames)
            throws CreationException
    {
        final String methodName = "constructClass(requiredInstanceOf,className,args,defaultPackage)";
        if (requiredInstanceOf == null || className == null || constructionArgs == null)
        {
            throw new IllegalArgumentException(methodName + " either requiredInstanceOf ["
                    + requiredInstanceOf + "] or className [" + className + "] or args ["
                    + constructionArgs + "] is null");
        }

        Object objectToReturn = null;
        Class<?>[] constructionClasses = null;

        Class<?> classToBeConstructed = null;
        try
        {
            classToBeConstructed = Class.forName(className);

            constructionClasses = new Class[constructionArgs.length];

            for (int i = 0; i < constructionArgs.length; i++)
            {
                constructionClasses[i] = constructionArgs[i].getClass();
            }

            Constructor<?> ctor = null;
            try
            {
                ctor = classToBeConstructed.getConstructor(constructionClasses);
            }
            catch (NoSuchMethodException nsme)
            {
                // try interfaces that match
                boolean isValidConstructor = false;
                Constructor<?>[] constructors = classToBeConstructed.getConstructors();
                for (int i = 0; i < constructors.length; i++)
                {
                    Constructor<?> currentCons = constructors[i];
                    Class<?>[] parameterTypes = currentCons.getParameterTypes();

                    if (parameterTypes.length == constructionClasses.length)
                    {
                        isValidConstructor = true;
                        // this could be valid, just need to check interfaces
                        for (int j = 0; j < parameterTypes.length; j++)
                        {
                            if (!parameterTypes[j].isAssignableFrom(constructionClasses[j]))
                            {
                                isValidConstructor = false;
                                break; // go to the next currentCons
                            }
                        }
                    }
                    if (isValidConstructor)
                    {
                        ctor = currentCons;
                        break;
                    }
                }
                if (!isValidConstructor)
                {
                    throw nsme;
                }
            }
            objectToReturn = ctor.newInstance(constructionArgs);

            if (!requiredInstanceOf.isInstance(objectToReturn))
            {
                throw new CreationException(classToBeConstructed.toString() + " is not of type "
                        + requiredInstanceOf);
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            if (defaultPackageNames != null)
            {
                int index = 0;

                // cheeky...
                while (index < defaultPackageNames.length && defaultPackageNames[index] == null)
                {
                    index++;
                }

                if (index < defaultPackageNames.length
                        && className.indexOf(defaultPackageNames[index]) == STRING_NOT_FOUND)
                {
                    // try prepending the class name prefix
                    String newClassName = defaultPackageNames[index].concat(DOT).concat(className
                            .substring(className.lastIndexOf(DOT) + 1));
                    defaultPackageNames[index] = null;
                    return constructClass(requiredInstanceOf,
                                          newClassName,
                                          constructionArgs,
                                          defaultPackageNames);
                }
            }
            // otherwise it is no good
            final String msg = "Can't find class " + className + " : " + cnfe.getMessage();
            logger.severe(msg);

            throw new CreationException(msg, cnfe);
        }
        catch (NoSuchMethodException nsme)
        {
            StringBuffer sb = new StringBuffer("The constructor with args (");
            if (constructionArgs.length > 0)
            {
                sb.append(constructionClasses[0].getName()).append(" ").append(constructionArgs[0]);
            }
            for (int i = 1; i < constructionArgs.length; i++)
            {
                sb.append(", ").append(constructionClasses[i].getName()).append(" ")
                        .append(constructionArgs[i].toString());
            }
            sb.append(") is not available : ");
            sb.append(nsme.getMessage()); // probably always null
            String msg = sb.toString();

            logger.severe(msg);
            throw new CreationException(msg, nsme);
        }
        catch (InvocationTargetException ite)
        {
            String msg = "Unable to construct " + className + " : "
                    + ite.getTargetException().toString();
            // we use toString() on the targetException because
            // otherwise we would not know what type of exception
            // is was - this provides the name and the message
            logger.severe(msg);
            throw new CreationException(msg, ite.getTargetException());
        }
        catch (Exception e)
        // probably SecurityException or a constructor exception
        {
            final String msg = "Unable to instantiate " + className + " : " + e.toString();
            logger.severe(msg);
            throw new CreationException(msg, e);
        }

        return objectToReturn;
    }

    /**
     * This method retrieves the elements from <code>superSet</code> that are
     * not in <code>subSet</code>.
     *
     * @param subset The Vector whose elements will be removed from
     *            <code>superset</code>. If this is null,
     *            IllegalArgumentException will be thrown.
     *
     * @param superSet The Vector whose objects will be removed, leaving those
     *            elements not in <code>subSet</code>. If this is null,
     *            IllegalArgumentException will be thrown.
     *
     * @return the set of objects that would be left over if the
     *         <code>subset</code> objects were removed from the
     *         <code>superSet</code> objects.
     */
    public static final Vector<Object> getDifference(Vector<Object> subset, Vector<Object> superSet)
    {
        return new Vector<Object>(getDifference((List<Object>) subset, (List<Object>) superSet));
    }

    /**
     * This method retrieves the elements from <code>superSet</code> that are
     * not in <code>subSet</code>.
     *
     * @param subset The Vector whose elements will be removed from
     *            <code>superset</code>. If this is null,
     *            IllegalArgumentException will be thrown.
     *
     * @param superSet The Vector whose objects will be removed, leaving those
     *            elements not in <code>subSet</code>. If this is null,
     *            IllegalArgumentException will be thrown.
     *
     * @return the set of objects that would be left over if the
     *         <code>subset</code> objects were removed from the
     *         <code>superSet</code> objects.
     */
    public static final List<Object> getDifference(List<Object> subset, List<Object> superSet)
    {
        if (subset == null || superSet == null)
        {
            final String msg = "At least one of subset [" + subset + "] or superSet [" + superSet
                    + "] is null";

            throw new IllegalArgumentException(msg);
        }

        List<Object> diff = new ArrayList<Object>(superSet);

        for (int i = 0; i < subset.size(); i++)
        {
            diff.remove(subset.get(i));
        }
        return diff;
    }

    /**
     * This method gets the set intersection of the two supplied sets.
     *
     * @param setOne
     * @param setTwo
     * @return those objects that are in both sets.
     */
    public static final Object[] getIntersection(Object[] setOne, Object[] setTwo)
    {
        List<Object> list = new ArrayList<Object>();

        Vector<Object> v2 = arrayToVector(setTwo);

        for (int i = 0; i < setOne.length; i++)
        {
            for (int j = 0; j < v2.size(); j++)
            {
                if (objectsAreEqual(setOne[i], v2.elementAt(j)))
                {
                    list.add(setOne[i]);
                    v2.remove(j);
                    break;
                }
            }
        }
        return list.toArray(new Object[list.size()]);
    }

    /**
     * This method subtracts the elements in <code>subset</code> from the
     * elements in <code>superSet</code> and returns the result.
     *
     * @param subset The array whose elements will be removed from
     *            <code>superset</code>. If this is null,
     *            IllegalArgumentException will be thrown.
     *
     * @param superSet The array whose objects will be removed, leaving those
     *            elements not in <code>subSet</code>. If this is null,
     *            IllegalArgumentException will be thrown.
     *
     * @return the set of objects that would be left over if the
     *         <code>subset</code> objects were removed from the
     *         <code>superSet</code> objects (as an array). The type of array
     *         that will be returned is the type of the <code>superSet</code>
     *         array.
     */
    public static final Object[] subtract(Object[] subset, Object[] superSet)
    {
        if (subset == null || superSet == null)
        {
            final String msg = "At least one of subset [" + subset + "] or superSet [" + superSet
                    + "] is null";

            throw new IllegalArgumentException(msg);
        }

        Vector<Object> vSubSet = arrayToVector(subset);
        Vector<Object> vSuperSet = arrayToVector(superSet);

        return UsefulOperations.vectorToArray(getDifference(vSubSet, vSuperSet), superSet
                .getClass().getComponentType());
    }

    /**
     * This method gets the first <code>size</code> elements out of the vector
     * <code>set</code> supplied. If there are not that many elements, it will
     * return all of them.
     *
     * @param set the vector you wish to extract up to <code>size</code>
     *            elements from. If this is null, an IllegalArgumentException
     *            will be thrown.
     *
     * @param size The number of elements you wish to retrieve from the set. If
     *            this is negative, an IllegalArgumentException will be thrown.
     *
     * @return A vector of up to <code>size</code> containing up to the first
     *         <code>size</code> elements of <code>set</code>.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Vector getFirstNElements(Vector<?> set, int size)
    {
        if (set == null)
        {
            throw new IllegalArgumentException("vectorOfVectors is null");
        }
        if (size < 0)
        {
            throw new IllegalArgumentException("size [" + size + "] cannot be negative");
        }

        int setSize = set.size();

        int sizeToUse = setSize > size ? size : setSize;

        Vector toReturn = new Vector(sizeToUse);

        for (int i = 0; i < sizeToUse; i++)
        {
            toReturn.addElement(set.elementAt(i));
        }

        return toReturn;
    }

    /**
     * This method gets the size of the largest Vector in a set of vectors.
     *
     * @param vectorOfVectors the vector containing a list of vectors. If this
     *            is null, an IllegalArgumentException will be thrown.
     *
     * @return The size of the largest vector in <code>vectorOfVectors</code>.
     */
    public static final int getMaxSize(Vector<?> vectorOfVectors)
    {
        if (vectorOfVectors == null)
        {
            throw new IllegalArgumentException("vectorOfVectors is null");
        }

        int size = vectorOfVectors.size();

        int maxSize = 0;

        for (int i = 0; i < size; i++)
        {
            Vector<?> v = (Vector<?>) vectorOfVectors.elementAt(i);
            int newSize = v.size();
            maxSize = maxSize > newSize ? maxSize : newSize;
        }

        return maxSize;
    }

    /**
     * This method checks whether an object is contained inside any of the
     * Vectors contained in a Vector of Vectors.
     *
     * @param containingVector This is the Vector containing a set of Vectors,
     *            one of which must contain <code>obj</code> in order for this
     *            method to return true.
     *
     * @param obj The object that we are looking for in the Vector of Vectors.
     *
     * @return <code>true</code> if the object is contained in the Vector of
     *         Vectors, and <code>false</code> otherwise.
     */
    public static final boolean isContainedIn(Vector<?> containingVector, Object obj)
    {
        int size = containingVector.size();

        for (int k = 0; k < size; k++)
        {
            Vector<?> bottomVector = (Vector<?>) containingVector.elementAt(k);
            if (bottomVector.contains(obj))
            {
                return true;
            }
        } // end for k
        return false;
    }

    /**
     * This method indicates whether the <code>subset</code> Vector is in fact
     * a subset of the <code>superSet</code> Vector. The order of objects in
     * the two Vectors does not matter. This method treats equal objects as a
     * single object, so if you had a subset of integers (1,2,3,1,1,4,5), and a
     * superset of integers (1,2,3,4,5), this method would return true.
     *
     * @param subset The Vector whose contained objects must all be in the
     *            <code>superset</code> for this method to return true.
     *
     * @param superSet The Vector whose objects must include all those in the
     *            <code>subset</code> for this method to return true.
     *
     * @return <code>true</code> if all objects in <code>subset</code> are
     *         contained in <code>superSet</code>.
     */
    public static final boolean isSubsetOf(Vector<?> subset, Vector<?> superSet)
    {
        for (Enumeration<?> e = subset.elements(); e.hasMoreElements();)
        {
            if (!superSet.contains(e.nextElement()))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * This method indicates whether the <code>sublist</code> Vector is in
     * fact an unordered sublist of the <code>superList</code> Vector. The
     * order of objects in the two Vectors does not matter. This method treats
     * each object as a different object even if it is "equal", so if you had a
     * subset of integers (1,2,3,1,1,4,5), and a superset of integers
     * (1,2,3,4,5), this method would return <code>false</code>.
     *
     * @param sublist The Vector whose contained objects must all be in the
     *            <code>superList</code> for this method to return true.
     *
     * @param superList The Vector whose objects must include all those in the
     *            <code>sublist</code> for this method to return true.
     *
     * @return <code>true</code> if all objects in <code>sublist</code> are
     *         contained in <code>superList</code>.
     */
    public static final boolean isUnOrderedSublistOf(Vector<?> sublist, Vector<?> superList)
    {
        if (sublist == null || superList == null)
        {
            final String msg = "At least one of sublist [" + sublist + "] or superList ["
                    + superList + "] is null";

            throw new IllegalArgumentException(msg);
        }

        Vector<?> subClone = (Vector<?>) sublist.clone();

        for (Enumeration<?> e = superList.elements(); e.hasMoreElements();)
        {
            subClone.removeElement(e.nextElement());
        }
        return subClone.isEmpty();
    }

    /**
     * This method checks whether two objects are equal. It determines whether
     * one or both of them are null, and if not, then calls "equals" between
     * them.
     *
     * @param first The first object to compare.
     *
     * @param second The second object to compare.
     *
     * @return <code>true</code> if the objects are equal, and false
     *         otherwise.
     */
    public static final boolean objectsAreEqual(Object first, Object second)
    {
        if (first == null && second == null)
        {
            return true;
        }
        else if (first == null || second == null)
        {
            return false;
        }
        return first.equals(second);
    }

    /**
     * This method checks whether two ordered arrays of data are equal. It calls
     * "equals" all the way down the two arrays determining that the Object at
     * the same index in <code>first</code> and <code>second</code> are
     * "equal".
     *
     * @param first The first array to compare.
     *
     * @param second The second array to compare.
     *
     * @return <code>true</code> if the arrays are equal, and false otherwise.
     */
    public static final boolean orderedArraysAreEqual(Object[] first, Object[] second)
    {
        if (first == null && second == null)
        {
            return true;
        }
        else if (first == null || second == null)
        {
            return false;
        }
        else if (first.length != second.length)
        {
            return false;
        }

        for (int i = 0; i < first.length; i++)
        {
            if (!objectsAreEqual(first[i], second[i]))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * This method converts a vector of objects into an array of objects. The
     * array will be of type <code>clazz</code>, and therefore assumes that
     * every object in <code>v</code> will be of type <code>clazz</code> or
     * some subclass of that.
     *
     * @param v The vector of objects to be converted into an array.
     *
     * @param clazz The desired class of the array. All objects in
     *            <code>v</code> must be implicitly castable to this class.
     *
     * @return An array of type <code>clazz []</code> containing all the
     *         objects in <code>v</code> in the same order as they appeared in
     *         <code>v</code>.
     */
    public static final Object[] vectorToArray(Vector<Object> v, Class<?> clazz)
    {
        if (v == null || clazz == null)
        {
            throw new IllegalArgumentException("at least one of v [" + v + "] or clazz + [" + clazz
                    + "] is null");
        }
        int size = v.size();
        Object[] toReturn = (Object[]) Array.newInstance(clazz, size);
        if (size > 0)
        {
            v.copyInto(toReturn);
        }
        return toReturn;
    }

    /**
     * This method converts a list of objects into an array of objects. The
     * array will be of type <code>clazz</code>, and therefore assumes that
     * every object in <code>l</code> will be of type <code>clazz</code> or
     * some subclass of that.
     *
     * @param l The list of objects to be converted into an array.
     *
     * @param clazz The desired class of the array. All objects in
     *            <code>l</code> must be implicitly castable to this class.
     *
     * @return An array of type <code>clazz []</code> containing all the
     *         objects in <code>l</code> in the same order as they appeared in
     *         <code>l</code>.
     */
    public static final Object[] listToArray(List<?> l, Class<?> clazz)
    {
        if (l == null || clazz == null)
        {
            throw new IllegalArgumentException("at least one of l [" + l + "] or clazz + [" + clazz
                    + "] is null");
        }
        int size = l.size();
        Object[] toReturn = (Object[]) Array.newInstance(clazz, size);
        if (size > 0)
        {
            toReturn = l.toArray(toReturn);
        }
        return toReturn;
    }

    /**
     * This method converts an array of Object into an array of class
     * <code>clazz</code>. Because the array will be of type
     * <code>clazz</code>, it assumes that every object in <code>v</code>
     * will be of type <code>clazz</code> or some subclass of that. This
     * method can be used on List.toArray() arrays.
     *
     * @param array The array of Object to be converted into an array of class
     *            <code>clazz</code>.
     *
     * @param clazz The desired class of the array. All objects in
     *            <code>v</code> must be implicitly castable to this class.
     *
     * @return An array of type <code>clazz []</code> containing all the
     *         objects in <code>v</code> in the same order as they appeared in
     *         <code>array</code>.
     */
    public static final Object[] arrayToArray(Object[] array, Class<?> clazz)
    {
        if (array == null || clazz == null)
        {
            throw new IllegalArgumentException("at least one of array [" + array + "] or clazz + ["
                    + clazz + "] is null");
        }
        int size = array.length;
        Object[] toReturn = (Object[]) Array.newInstance(clazz, size);
        if (size > 0)
        {
            System.arraycopy(array, 0, toReturn, 0, array.length);
        }
        return toReturn;
    }

    /**
     * This method converts a vector of objects into an array of objects. The
     * array will be the same as the class of the object that is the first
     * element of the Vector. Therefore it is assumed that people using this
     * call will have a vector of objects all of the same class (no subclassing
     * etc, since this would affect the overall array). Also, since the class is
     * obtained from the objects, the Vector may not be empty for this call to
     * work.
     *
     * @param v The vector of objects to be converted into an array. A call to
     *            {@link java.util.Vector#firstElement()}will return the first
     *            element and access the class from it, therefore this object
     *            cannot be empty. If it is null or empty, an
     *            IllegalArgumentException will be thrown.
     *
     * @return An array of type <code>v.firstElement().getClass() []</code>
     *         containing all the objects in <code>v</code> in the same order
     *         as they appeared in <code>v</code>.
     */
    public static Object[] vectorToArray(Vector<Object> v)
    {
        if (v == null || v.size() == 0)
        {
            throw new IllegalArgumentException("v [" + v + "] is null or empty");
        }
        Class<?> clazz = v.firstElement().getClass();
        return vectorToArray(v, clazz);
    }

    /**
     * This method inserts a buffer value into an existing message just after
     * each of its new lines. So if a line of <code>data</code> contains new
     * line characters, each one of these will end up being preceded by a
     * <code>valueToInsert</code> String. The entire line will have a newline
     * appended to it.
     *
     * @param buf The stringBuffer in which to construct the new string.
     *
     * @param valueToInsert The value to insert just after each new line in
     *            <code>originalString</code>.
     *
     * @param originalString The original string with newlines.
     *
     * @param newline This value indicates whether a newline preceeded the
     *            <code>originalString</code> in <code>buf</code>. If it
     *            did (this value will be true) then the
     *            <code>valueToInsert</code> should be inserted into
     *            <code>buf</code> before anything else.
     *
     * @return the StringBuffer passed in as <code>buf</code>. This allows
     *         for chained calls. The returned buffer will have a newline at the
     *         end of it.
     */
    public static StringBuffer insertDataString(StringBuffer buf,
                                                String valueToInsert,
                                                String originalString,
                                                boolean newline)
    {
        return insertDataString(buf, valueToInsert, originalString, newline, true);
    }

    /**
     * This method inserts a buffer value into an existing message just after
     * each of its new lines. So if a line of <code>data</code> contains new
     * line characters, each one of these will end up being preceded by a
     * <code>valueToInsert</code> String. The entire line will have a newline
     * appended to it.
     *
     * @param buf The stringBuffer in which to construct the new string.
     *
     * @param valueToInsert The value to insert just after each new line in
     *            <code>originalString</code>.
     *
     * @param originalString The original string with newlines.
     *
     * @param newline This value indicates whether a newline preceeded the
     *            <code>originalString</code> in <code>buf</code>. If it
     *            did (this value will be true) then the
     *            <code>valueToInsert</code> should be inserted into
     *            <code>buf</code> before anything else.
     *
     * @return the StringBuffer passed in as <code>buf</code>. This allows
     *         for chained calls. The returned buffer will have a newline at the
     *         end of it.
     */
    public static StringBuffer insertDataString(StringBuffer buf,
                                                String valueToInsert,
                                                String originalString,
                                                boolean newline,
                                                boolean addNewlineAtEnd)
    {
        if (originalString == null || originalString.length() < 1)
        {
            if (!newline)
            {
                buf.append('\n');
            }
            return buf;
        }

        if (newline)
        {
            buf.append(valueToInsert);
        }

        int start = 0;
        int lf = originalString.indexOf('\n');

        if (lf < 0)
        {
            // no newlines
            buf.append(originalString);
            if (addNewlineAtEnd)
            {
                buf.append('\n');
            }
            return buf;
        }

        if (lf == originalString.length() - 1)
        {
            // one newline at end
            buf.append(originalString);
            return buf;
        }

        buf.append(originalString.substring(0, lf + 1));

        start = lf + 1;

        while (start < originalString.length())
        {
            buf.append(valueToInsert);

            lf = originalString.indexOf('\n', start);
            if (lf < 0)
            {
                buf.append(originalString.substring(start));
                if (addNewlineAtEnd)
                {
                    buf.append('\n');
                }
                return buf;
            }

            buf.append(originalString.substring(start, lf + 1));
            start = lf + 1;
        }
        return buf;
    }

    /**
     * This method converts a string of data into a list of Name Value Pairs.
     *
     * @param line The line of data to be parsed.
     *
     * @param interNvpSeparators The separators to be used to separate one
     *            name-value pair string from another in the <code>line</code>.
     *            N.B. You cannot specify the same character in this string and
     *            in the <code>intraNvpSeparators</code>.
     *
     * @param intraNvpSeparators The separators to be used to separate the name
     *            and the value in a sinlge name-value pair. N.B. You cannot
     *            specify the same character in this string and in the
     *            <code>interNvpSeparators</code>.
     *
     * @return The array of name value pairs that were specified in the string.
     *
     * @throws IllegalArgumentException when there are no
     *             <code>intraNvpSeparators</code> in a particular name value
     *             pair.
     */
    public static final NameValuePair[] produceNvps(String line,
                                                    String interNvpSeparators,
                                                    String intraNvpSeparators)
    {
        StringTokenizer st = new StringTokenizer(line, interNvpSeparators);

        NameValuePair[] nvps = new NameValuePair[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens())
        {
            String nvpString = st.nextToken();
            int separatorIndex = nvpString.indexOf(intraNvpSeparators);

            if (separatorIndex < 0)
            {
                throw new IllegalArgumentException("Failed to parse [" + nvpString
                        + "] - expecting a [" + intraNvpSeparators + "]");
            }

            // get data type
            String name = nvpString.substring(0, separatorIndex).trim();

            String value = nvpString.substring(separatorIndex + intraNvpSeparators.length()).trim();

            nvps[count++] = new NameValuePair(name, value);
        }
        return nvps;
    }

    /**
     * This method is the same as <code>Thread.sleep()</code>, but does not
     * throw <code>InterruptedException</code>.
     *
     * @param millis the length of time to sleep in milliseconds.
     */
    public static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException ex)
        {
            logger.warning("Caught unexpected interrupted exception - coming out of wait");
        }
    }

    /**
     * This method constructs a string starting with the string
     * <code>toPad</code> and adds the string <code>toPadWith</code> until
     * the total length of the new string is <code>totalLength</code>. The
     * string <code>toPadWith</code> may be repeated many times.
     *
     * @param toPad The starting string.
     *
     * @param toPadWith The string to add to the starting string
     *            <code>toPad</code>.
     *
     * @param totalLength The final length of the returned string buffer.
     *
     * @return a stringbuffer of length <code>totalLength</code> containing
     *         toPad, and padded with the string <code>toPadWith</code>.
     */
    public static StringBuffer pad(String toPad, String toPadWith, int totalLength)
    {
        if (toPad == null || toPadWith == null || toPadWith.length() <= 0)
        {
            throw new IllegalStateException("Either toPad [" + toPad + "] or toPadWith ["
                    + toPadWith + "] is null or toPadWith is 0 length");
        }

        int length = toPad.length();
        int additionalLength = toPadWith.length();

        StringBuffer sb = new StringBuffer(totalLength);

        sb.append(toPad);
        int currentLength = length;
        int stoppingLength = totalLength - additionalLength;

        while (currentLength < stoppingLength)
        {
            sb.append(toPadWith);
            currentLength += additionalLength;
        }
        sb.append(toPadWith.substring(0, totalLength - sb.length()));
        return sb;
    }

    /**
     * This method gets the short class name for an object.
     *
     * @param o the object whose short class name is desired.
     *
     * @return the final part of the class name (hence the name as declared in
     *         the source file).
     */
    public static String getShortClassName(Object o)
    {
        return getShortClassName(o.getClass());
    }

    /**
     * This method gets the short class name for the supplied class.
     *
     * @param c the class whose short name is desired.
     *
     * @return the final part of the class name (hence the name as declared in
     *         the source file).
     */
    public static String getShortClassName(Class<?> c)
    {
        String className = c.getName();

        return getLastToken(className, DOT);
    }

    /**
     * This method gets the last value in a string separated by the
     * <code>delimiter</code>. In the case of a fully specified classname,
     * with a delimiter of DOT, the value <code>toParse</code> of
     * au.com.zinescom.util.UsefulOperations would become UsefulOperations. If
     * the delimiter is not found in the string, the whole string is returned.
     *
     * @param toParse The string whose final token is sought.
     *
     * @param delimiter The value to tokenise the string with.
     */
    public static String getLastToken(String toParse, String delimiter)
    {
        int index = toParse.lastIndexOf(delimiter);

        if (index < 0)
        {
            return toParse;
        }
        return toParse.substring(index + 1);
    }

    /**
     * This method produces a stringified version of the class printing the
     * shorthand name of the class and a list of the names of the public fields
     * and their values.
     *
     * @param withPublicFields The object to be stringified.
     *
     * @return a stringified version of the class printing the shorthand name of
     *         the class and a list of the names of the public fields and their
     *         values.
     */
    public static String produceString(Object withPublicFields, boolean includeSuperFields)
    {
        final String METHOD_NAME = "convert(obj toConvert, obj toPopulate)";

        Field[] fieldAry = null;

        try
        {
            if (includeSuperFields)
            {
                Class<?> superClass = withPublicFields.getClass();
                Vector<Field[]> fieldArrays = new Vector<Field[]>();
                int fullCount = 0;

                while (superClass != null)
                {
                    fieldAry = superClass.getDeclaredFields();
                    fieldArrays.add(fieldAry);
                    superClass = superClass.getSuperclass();
                    fullCount += fieldAry.length;
                }
                fieldAry = new Field[fullCount];
                int fldArySize = fieldArrays.size();
                int currentPos = 0;
                for (int i = fldArySize - 1; i >= 0; i--)
                {
                    Field[] ary = (Field[]) fieldArrays.elementAt(i);
                    int length = ary.length;
                    System.arraycopy(ary, 0, fieldAry, currentPos, length);
                    currentPos += length;
                }
            }
            else
            {
                fieldAry = withPublicFields.getClass().getDeclaredFields();
            }
            AccessibleObject.setAccessible(fieldAry, true);
        }
        catch (SecurityException se)
        {
            if (fieldAry == null) // can't continue only if first call fails
            {
                final String msg = METHOD_NAME + ": Security Issue : " + se;
                throw new IllegalStateException(msg);
            }
        }

        int maxLength = 0;
        Field currentField = null;
        int fLength = fieldAry.length;
        String[] names = new String[fLength];
        String[] values = new String[fLength];

        for (int i = 0; i < fLength; i++)
        {
            currentField = fieldAry[i];
            String fieldName = currentField.getName();
            names[i] = fieldName;
            int length = fieldName.length();

            maxLength = maxLength > length ? maxLength : length;

            try
            {
                values[i] = String.valueOf(currentField.get(withPublicFields));
            }
            catch (IllegalAccessException e)
            {
                final String msg = "IllegalAccessException on current field [" + currentField
                        + "] : " + e;
                logger.info(msg);
                values[i] = "Unknown";
            }
            // catch (Exception e)
            // {
            // final String msg = "Unknown error on current field [" +
            // CurrentField + "] : " + e;
            // Logger.logInformative("Error",msg, METHOD_NAME);
            // }
        }

        StringBuffer sb = new StringBuffer();
        String className = getShortClassName(withPublicFields);
        sb.append(className).append("\n");
        final String PRE_SPACES = "   ";
        final String _EQUALS_ = " = ";

        for (int i = 0; i < fieldAry.length; i++)
        {
            StringBuffer tmpSb = new StringBuffer(PRE_SPACES);
            tmpSb.append(pad(names[i], SPACE, maxLength)).append(_EQUALS_);
            String value = values[i];
            if (value.indexOf(NEWLINE) != STRING_NOT_FOUND)
            {
                String s = pad(SPACE, SPACE, tmpSb.length()).toString();
                insertDataString(tmpSb, s, value, false);
            }
            else
            {
                tmpSb.append(values[i]).append(NEWLINE);
            }

            sb.append(tmpSb.toString());
        }
        return sb.toString();
    }

    /**
     * This method produces a stringified version of the class printing the
     * shorthand name of the class and a list of the names of the public fields
     * and their specific values. This calls is the same as
     * {@link #produceString(Object, boolean) produceString(object, false)}.
     *
     * @param withPublicFields The object to be stringified.
     *
     * @return a stringified version of the class printing the shorthand name of
     *         the class and a list of the names of the (public) fields of the
     *         specific class and their values.
     */
    public static String produceString(Object withPublicFields)
    {
        return produceString(withPublicFields, false);
    }

    /**
     * This method converts an array of objects to a single string. It does this
     * by calling the supplied <code>method</code> on each object separating
     * each object with the given separator. If the supplied <code>method</code>
     * is not available, then toString() will be used.
     * <P>
     * If the separator is null, no separation is used.
     *
     * @param array the array of object to convert
     *
     * @param method The method to call in order to access the required data
     *            from the object. The accessed data will have toString() called
     *            on it. If this is null, toString will be used.
     *
     * @return a string representation of the array
     */
    public static String[] convertArray(Object[] array, String method)
    {
        if (array == null)
        {
            throw new IllegalArgumentException("array is null");
        }

        Method m = null;

        String[] toReturn = new String[array.length];

        for (int i = 0; i < array.length; i++)
        {
            Object obj = array[i];
            if (obj == null)
            {
                toReturn[i] = null;
            }
            if (method != null)
            {
                Class<?> clazz = obj.getClass();
                Hashtable<String, Method> methodTable = (Hashtable<String, Method>) theClassToMethodTable.get(clazz);
                if (methodTable != null)
                {
                    m = (Method) methodTable.get(method);
                }
                else
                {
                    methodTable = new Hashtable<String, Method>();
                    theClassToMethodTable.put(clazz, methodTable);
                }
                if (m == null)
                {
                    try
                    {
                        m = clazz.getMethod(method, new Class[0]);
                        methodTable.put(method, m);
                    }
                    catch (NoSuchMethodException nsme)
                    {
                        final String msg = "method [" + method + "] is not available for class ["
                                + clazz + "] (using toString()): " + nsme.getMessage();
                        if (logger.isLoggable(Level.FINEST))
                        {
                            logger.fine(msg);
                        }

                        try
                        {
                            m = clazz.getMethod(TO_STRING_METHOD_NAME, new Class[0]);
                        }
                        catch (NoSuchMethodException nsme2)
                        {
                            // there has to be a toString method
                            // it is defined on the Object class
                        }
                    }
                }
            }
            try
            {
                toReturn[i] = m.invoke(obj, EMPTY_OBJECT_ARRAY).toString();
            }
            catch (Exception e)
            {
                final String msg = "Unable to invoke method [" + m + "] on obj [" + obj + "]. : "
                        + e + ". Using toString() instead.";
                if (logger.isLoggable(Level.FINEST))
                {
                    logger.finest(msg);
                }
                toReturn[i] = obj.toString();
            }
        }
        return toReturn;
    }

    /**
     * This method gets a singleton style object of class type
     * <code>clazz</code> using an instance method (either located in
     * <code>instanceMethodTable</code> or named the default "instance")
     *
     * @param clazz The class of the object to access.
     *
     * @param instanceMethodTable The hashtable containing the names of instance
     *            methods mapped to class names.
     *
     * @return The instance of the object of type <code>clazz</code> obtained
     *         by calling the appropriate <b>static </b> instance method on
     *         <code>clazz</code>. If there is no method of the selected name
     *         on the class, null will be returned.
     */
    public static Object getInstanceBasedObject(Object caller,
                                                Class<?> clazz,
                                                Hashtable<?, ?> instanceMethodTable)
    {
        String className = clazz.getName();
        Object toCallOn = null;
        Method instMeth = null;
        String instanceMethodName = getInstanceMethodName(className, instanceMethodTable);

        try
        {
            instMeth = clazz.getMethod(instanceMethodName, new Class[0]);
            toCallOn = instMeth.invoke(null, new Object[0]);
        }
        catch (IllegalAccessException iae)
        {
            final String msg = "Unable to execute static [" + instMeth + "] : " + iae;
            logger.severe(msg);
        }
        catch (InvocationTargetException ite)
        {
            final String msg = "Unable to execute static [" + instMeth + "] : "
                    + ite.getTargetException();
            logger.severe(msg);
        }
        catch (NoSuchMethodException e)
        {
            // leave instance as null - assume static
            final String msg = "There is no instance method of name " + instanceMethodName;
            if (logger.isLoggable(Level.FINEST))
            {
                logger.fine(msg);
            }
        }
        return toCallOn;
    }

    /**
     * This method gets the name of the instance method for the supplied
     * <code>className</code>.
     *
     * @param className The name of the class whose instance method is being
     *            sought.
     *
     * @return The instance name to be used on the class with the supplied fully
     *         qualified <code>className</code>.
     */
    private static String getInstanceMethodName(String className, Hashtable<?, ?> instanceMethodTable)
    {
        String methodName = (String) instanceMethodTable.get(className);
        return (methodName == null) ? DEFAULT_INSTANCE_METHOD_NAME : methodName;
    }

    /**
     * This method creates the instanceMethod name table and then adds any class
     * name to instance method name pairs it finds in <code>instanceMap</code>.
     *
     * @param caller the caller of this method.
     *
     * @param nameValueMap The comma and space separated name value pair list
     *            mapping names to values, with name and value being separated
     *            by a caret.
     *
     * @return the hashtable of names to values.
     */
    public static Hashtable<String, String> produceNameValueMap(Object caller, String nameValueMap)
            throws CreationException
    {
        return produceNameValueMap(caller, nameValueMap, SEPARATORS, CARET);
    }

    /**
     * This method creates an instance method name table and then adds any class
     * name to instance method name pairs it finds in <code>instanceMap</code>.
     *
     * @param caller the caller of this method.
     *
     * @param nameValueMap The <code>interNvpSeparators</code> separated name
     *            value pair list mapping names to values, with name and value
     *            being separated by a <code>intraNvpSeparators</code>.
     *
     * @param interNvpSeparators The inter name value pair separators.
     *
     * @param intraNvpSeparators The intra name value pair separators.
     *
     * @return the hashtable of names to values.
     */
    public static Hashtable<String, String> produceNameValueMap(Object caller,
                                                String nameValueMap,
                                                String interNvpSeparators,
                                                String intraNvpSeparators) throws CreationException
    {
        Hashtable<String, String> map = new Hashtable<String, String>();
        if (nameValueMap == null)
        {
            return map;
        }
        try
        {
            NameValuePair[] nvps = produceNvps(nameValueMap, interNvpSeparators, intraNvpSeparators);
            for (int i = 0; i < nvps.length; i++)
            {
                map.put(nvps[i].getName(), nvps[i].getValue());
            }
        }
        catch (IllegalArgumentException e)
        {
            final String msg = "Could not extract instance method map from [" + nameValueMap
                    + "] : " + e.getMessage();
            logger.severe(msg);
            throw new CreationException(msg);
        }
        return map;
    }

    /**
     * This method gets the first class in a stack trace of this call that does
     * not match the provided strings. If no class can be found after rejecting
     * those that match the provided strings, an IllegalStateException is
     * thrown. NOTE: This method will not work on calls from this class.
     *
     * @param exclusions The list of strings to exclude in the calling class
     *            search.
     *
     * @return The first Class in a stack trace not matching the supplied
     *         strings.
     */
    public static final Class<?> getCallerClass(String[] exclusions)
    {
        StackTraceElement stack[] = (new Throwable()).getStackTrace();

        // First, we should skip this class, but if a compiler inlines this,
        // we should not skip the next level
        int i = 0;
        String className = null;
        while (i < stack.length)
        {
            boolean excluded = false;
            StackTraceElement frame = stack[i];
            i++;
            className = frame.getClassName();
            for (int j = 0; j < exclusions.length; j++)
            {
                if (className.indexOf(exclusions[j]) != -1)
                {
                    excluded = true;
                    break;
                }
            }
            if (!excluded)
            {
                if (className.indexOf(UsefulOperations.class.getName()) != -1)
                {
                    continue;
                }
                try
                {
                    return Class.forName(className);
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                    throw new IllegalStateException("Could not find class for name [" + className
                            + "]");
                }
            }
        }
        throw new IllegalStateException("Could not find class to construct from stack trace ["
                + stack + "]");
    }

    /**
     * This method gets the common ancestor of two classes.
     *
     * @param first The first of two classes to compare for a common ancestor.
     *
     * @param second The second of two classes to compare for a common ancestor.
     *
     * @return The common ancestor.
     */
    public static Class<?> getCommonAncestor(Class<?> first, Class<?> second)
    {
        if (first.isAssignableFrom(second))
        {
            return first;
        }
        else if (second.isAssignableFrom(first))
        {
            return second;
        }
        else
        {
            // find common ancestor
            Class<?> firstSuper = first.getSuperclass();
            return getCommonAncestor(firstSuper, second);
        }
    }

    /**
     * This method prints metadata concerning a resultset.
     *
     * @param caller
     * @param rs
     * @throws SQLException
     */
    public void printMetaData(Object caller, ResultSet rs) throws SQLException
    {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int i = 1; i <= columnCount; i++)
        {
            // Table t = new Table();
            // t.setName(...);
            if (logger.isLoggable(Level.FINEST))
            {
                logger.finest("Column Number (" + i + "):\n" + "Column Name     :  "
                        + rsmd.getColumnName(i) + "\n" +
                        // "Column Type : " +
                        // rsmd.getColumnType(i) + "\n" +

                        // theLogger.info("Column ClassName: " +
                        // rsmd.getColumnClassName(i) + "\n");
                        "Column Type Name:  " + rsmd.getColumnTypeName(i) + "\n"
                        + "Schema Name     :  " + rsmd.getSchemaName(i) + "\n"
                        + "Table Name      :  " + rsmd.getTableName(i) + "\n"
                        + "Precision       :  " + rsmd.getPrecision(i) + "\n"
                        + "Scale           :  " + rsmd.getScale(i) + "\n");
            }
        }
    }

    /**
     * This method runs a few tests.
     *
     * @param args
     */
    public static void main(String[] args)
    {
        Object[] first = new String[]{"first", "second", "third", "fourth", "fifth"};
        Object[] second = new String[]{"first", "second", "fifth", "sixth"};

        Vector<Object> firstList = arrayToVector(first);
        Vector<Object> secndList = arrayToVector(second);

        Vector<Object> diff = getDifference(secndList, firstList);
        System.out.println("Difference between [" + firstList + "] and [" + secndList + "] is "
                + diff);

        Object[] inter = getIntersection(first, second);
        System.out.println("Intersection between [" + firstList + "] and [" + secndList + "] is "
                + arrayToString(inter, null, ","));

    }

    /**
     * This method gets a number that is one less than the supplied
     * <code>number</code>.
     *
     * @param number The number one greater than the return value.
     *
     * @return A number one less than the supplied <code>number</code>.
     */
    public static Number getValueBelow(Object number)
    {
        if (number instanceof Integer)
        {
            return Integer.valueOf(((Integer) number).intValue() - 1);
        }
        else if (number instanceof Float)
        {
            return ((Float) number).floatValue() - 0.1;
        }
        else if (number instanceof Double)
        {
            return Double.valueOf(((Double) number).doubleValue() - 0.1);
        }
        else if (number instanceof Long)
        {
            return Long.valueOf(((Long) number).longValue() - 1);
        }
        else
        {
            throw new UnsupportedOperationException("Objects of type ["
                    + number.getClass().getName() + "] are not supported");
        }
    }

    /**
     * This method gets a number that is one more than the supplied
     * <code>number</code>.
     *
     * @param number The number one less than the return value.
     *
     * @return A number one greater than the supplied <code>number</code>.
     */
    public static Number getValueAbove(Object number)
    {
        if (number instanceof Integer)
        {
            return Integer.valueOf(((Integer) number).intValue() + 1);
        }
        else if (number instanceof Float)
        {
            return ((Float) number).floatValue() + 0.1;
        }
        else if (number instanceof Double)
        {
            return Double.valueOf(((Double) number).doubleValue() + 0.1);
        }
        else if (number instanceof Long)
        {
            return Long.valueOf(((Long) number).longValue() - 1);
        }
        else
        {
            throw new UnsupportedOperationException("Objects of type ["
                    + number.getClass().getName() + "] are not supported");
        }
    }

    /**
     * This method capitalises the first letter of the supplied string and
     * returns the new string,
     *
     * @param toCapitalise The string whose first character will be capitalised.
     *
     * @return a string similar to toCapitalise with a capitalised first
     *         character.
     */
    public static String capitaliseFirst(String toCapitalise)
    {
        return toCapitalise.replaceFirst("^([a-z])", ""
                + Character.toUpperCase(toCapitalise.charAt(0)));
    }

    /**
     * This method checks that a supplied string <code>toMatch</code> is
     * present as a single word somewhere in the supplied <code>line</code>.
     *
     * @param line The line which we are checking for the presence of the word
     *            <code>toMatch</code>.
     *
     * @param toMatch The word we are searching for.
     *
     * @return <code>true</code> if the word <code>toMatch</code> is in the
     *         <code>line</code>.
     */
    public static boolean matchesWord(String line, String toMatch)
    {
        String fullCheck = ".*\\W" + toMatch + "\\W.*|" + "^" + toMatch + "\\W.*|" + ".*\\W"
                + toMatch + "$|" + "^" + toMatch + "$";
        return line.matches(fullCheck);
    }
}
