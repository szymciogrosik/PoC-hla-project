package hla.manager;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.statistic.StatisticExternalObject;
import hla.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class ManagerAmbassador extends BaseAmbassador {
    protected ArrayList<ManagerExternalObject> externalObjects = new ArrayList<>();

    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes, byte[] tag) {
        reflectAttributeValues(theObject, theAttributes, tag, null, null);
    }

    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes, byte[] tag, LogicalTime theTime,
                                       EventRetractionHandle retractionHandle) {
        String objectName = "";
        double time =  convertTime(theTime);

        try {
            objectName = rtiAmbassador.getObjectClassName(rtiAmbassador.getObjectClass(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | ObjectClassNotDefined | ObjectNotKnown rtIinternalError) {
            rtIinternalError.printStackTrace();
        }

        StringBuilder builder = new StringBuilder("Reflection for object: ");

        switch (objectName) {
            case ConfigConstants.QUEUE_OBJ_NAME:
                try {
                    externalObjects.add(new ManagerExternalObject(theAttributes, ManagerExternalObject.ObjectType.QUEUE , time));
                    builder.append(ManagerExternalObject.ObjectType.QUEUE + ", time=").append(time);
                    builder.append(" " + ConfigConstants.QUEUE_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theAttributes.getValue(0)));
                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theAttributes.getValue(1)));
                    builder.append(" " + ConfigConstants.QUEUE_LENGTH_NAME + "=").append(EncodingHelpers.decodeInt(theAttributes.getValue(2)));
                    builder.append( "\n" );
                } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                    arrayIndexOutOfBounds.printStackTrace();
                }
                break;

            case ConfigConstants.CASH_REGISTER_OBJ_NAME:
                try {
                    externalObjects.add(new ManagerExternalObject(theAttributes, ManagerExternalObject.ObjectType.CASH_REGISTER , time));
                    builder.append(ManagerExternalObject.ObjectType.CASH_REGISTER + ", time=").append(time);
                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theAttributes.getValue(0)));
                    builder.append(" " + ConfigConstants.CASH_REGISTER_IS_FREE_NAME + "=").append(EncodingHelpers.decodeBoolean(theAttributes.getValue(1)));
                    builder.append("\n");
                } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                    arrayIndexOutOfBounds.printStackTrace();
                }
                builder.append( "\n" );
                break;

            default:
                builder.append("Undetected interaction.");
        }

        log(builder.toString());
    }

    @Override
    public void discoverObjectInstance(int theObject, int theObjectClass, String objectName) throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError {
        String objName = "";
        try {
            objName = rtiAmbassador.getObjectClassName(rtiAmbassador.getObjectClass(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | ObjectClassNotDefined | ObjectNotKnown rtIinternalError) {
            rtIinternalError.printStackTrace();
        }
        System.out.println("Pojawil sie nowy obiekt typu SimObject: " + objName + ".");
    }
}
