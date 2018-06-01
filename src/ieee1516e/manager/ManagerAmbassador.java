package ieee1516e.manager;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class ManagerAmbassador extends BaseAmbassador {
    protected ArrayList<ManagerExternalObject> externalObjects = new ArrayList<>();

    public void reflectAttributeValues( ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrder,
                                        TransportationTypeHandle transport,
                                        hla.rti1516e.FederateAmbassador.SupplementalReflectInfo reflectInfo ) {
        reflectAttributeValues( theObject, theAttributes, tag, sentOrder, transport, null, sentOrder, reflectInfo );
    }

    public void reflectAttributeValues( ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrdering,
                                        TransportationTypeHandle theTransport,
                                        LogicalTime timeReceived,
                                        OrderType receivedOrdering,
                                        FederateAmbassador.SupplementalReflectInfo reflectInfo ) {
        String objectName = "";
        double time =  convertTime(timeReceived);

        try {
            objectName = rtiAmbassador.getObjectClassName(rtiAmbassador.getKnownObjectClassHandle(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | ObjectInstanceNotKnown | InvalidObjectClassHandle | NotConnected rtIinternalError) {
            rtIinternalError.printStackTrace();
        }

        StringBuilder builder = new StringBuilder("Reflection for object: ");

        switch (objectName) {
            case ConfigConstants.QUEUE_OBJ_NAME:
                externalObjects.add(new ManagerExternalObject(theAttributes, ManagerExternalObject.ObjectType.QUEUE , time));
                builder.append(ManagerExternalObject.ObjectType.QUEUE + ", time=").append(time);
                builder.append( "\n" );
                break;

            case ConfigConstants.CASH_REGISTER_OBJ_NAME:
                externalObjects.add(new ManagerExternalObject(theAttributes, ManagerExternalObject.ObjectType.CASH_REGISTER , time));
                builder.append(ManagerExternalObject.ObjectType.CASH_REGISTER + ", time=").append(time);
                builder.append("\n");
                break;

            default:
                builder.append("Undetected interaction.");
        }

        log(builder.toString());
    }

    @Override
    public void discoverObjectInstance( ObjectInstanceHandle theObject,
                                        ObjectClassHandle theObjectClass,
                                        String objectName ) {
        String objName = "";
        try {
            objName = rtiAmbassador.getObjectClassName(rtiAmbassador.getKnownObjectClassHandle(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | ObjectInstanceNotKnown | InvalidObjectClassHandle | NotConnected rtIinternalError) {
            rtIinternalError.printStackTrace();
        }
        System.out.println("Pojawil sie nowy obiekt typu SimObject: " + objName + ".");
    }
}
