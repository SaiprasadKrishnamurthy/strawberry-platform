package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.api.EventStreamConfig;
import com.sai.strawberry.micro.es.ESFacade;

import java.util.List;

/**
 * Created by saipkri on 08/09/16.
 */
public class ESActor extends UntypedActor {

    private final ESFacade esFacade;

    public ESActor(final ESFacade esFacade) {
        this.esFacade = esFacade;
    }

    @Override
    public void onReceive(final Object forceRecreateEsIndex) throws Throwable {
        if (forceRecreateEsIndex instanceof List) {
            Boolean force = (Boolean) ((List) forceRecreateEsIndex).get(0);
            EventStreamConfig config = (EventStreamConfig) ((List) forceRecreateEsIndex).get(1);
            esFacade.init(force, config);
            getSender().tell(config, getSelf());
        }
    }
}
