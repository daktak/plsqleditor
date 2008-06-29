package plsqleditor.parsers;

import org.eclipse.jface.text.Position;

public class BuiltInFunctionSegment extends Segment
{
    static final Position DUMMY_POS = new Position(0); 
    
    static class Parameter extends Segment.Parameter
    {

        Parameter(String param, String inout, String type, String extraDetails, int offset)
        {
            super(param, inout, type, extraDetails, offset);
        }

        protected boolean isShowingInOut()
        {
            return false;
        }

        boolean isShowingParameterNames()
        {
            return true;
        }

        protected boolean isShowingParameterTypes()
        {
            return false;
        }
        
    }
    
    public BuiltInFunctionSegment(String name, String returnType)
    {
        super(name, DUMMY_POS, SegmentType.Function);
        setReturnType(returnType);
    }

    public String getPresentationName(boolean isShowingParameterList, boolean isShowingReturnType, boolean overrideParameterSettings)
    {
        return super.getPresentationName(isShowingParameterList, isShowingReturnType, false);
    }

    public void addParameter(String paramName, String paramInOut, String paramType, String extraDetails, int offset)
    {
        myParameterList.add(new Parameter(paramName, paramInOut, paramType, extraDetails, offset));
    }
}
