package ieee1516e.client;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class ClientAmbassador extends BaseAmbassador {
    protected ArrayList<ClientExternalObject> externalObjects = new ArrayList<>();

    @Override
    public void reflectAttributeValues( ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrder,
                                        TransportationTypeHandle transport,
                                        SupplementalReflectInfo reflectInfo ) {
        reflectAttributeValues( theObject, theAttributes, tag, sentOrder, transport, null, sentOrder, reflectInfo );
    }

    @Override
    public void reflectAttributeValues( ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrdering,
                                        TransportationTypeHandle theTransport,
                                        LogicalTime timeReceived,
                                        OrderType receivedOrdering,
                                        SupplementalReflectInfo reflectInfo ) {
        String objectName = "";
        double time = convertTime(timeReceived);

        try {
            objectName = rtiAmbassador.getObjectClassName(rtiAmbassador.getKnownObjectClassHandle(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | NotConnected | ObjectInstanceNotKnown | InvalidObjectClassHandle rtIinternalError) {
            rtIinternalError.printStackTrace();
            return;
        }

        StringBuilder builder = new StringBuilder("Reflection for object: ");

        switch (objectName) {
            case ConfigConstants.QUEUE_OBJ_NAME:
                externalObjects.add(new ClientExternalObject(theAttributes, ClientExternalObject.ObjectType.QUEUE, time));
                builder.append(ClientExternalObject.ObjectType.QUEUE + ", time=").append(time);
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
        } catch (RTIinternalError | FederateNotExecutionMember | NotConnected | ObjectInstanceNotKnown | InvalidObjectClassHandle rtIinternalError) {
            rtIinternalError.printStackTrace();
            return;
        }
        System.out.println("Pojawil sie nowy obiekt typu SimObject: " + objName + ".");
    }
}
