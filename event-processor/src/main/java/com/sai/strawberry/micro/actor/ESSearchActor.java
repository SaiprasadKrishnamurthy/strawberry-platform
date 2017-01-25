package com.sai.strawberry.micro.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.EventProcessingContext;
import com.sai.strawberry.micro.model.NotificationTuple;
import com.sai.strawberry.micro.model.ProcessorEvent;
import com.sai.strawberry.micro.model.SearchletQueryTuple;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 08/09/16.
 */
public class ESSearchActor extends UntypedActor {

    private ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate rt = new RestTemplate();
    private final String esUrl;

    public ESSearchActor(final String esUrl) {
        this.esUrl = esUrl;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        System.out.println(" ---- " + message);
        if (message instanceof SearchletQueryTuple) {
            SearchletQueryTuple searchletQueryTuple = (SearchletQueryTuple) message;
            System.out.println(" --- " + searchletQueryTuple.getEsQueryJson());

            List<Map> response = null;
            Map searchResponse = rt.postForObject(esUrl + "/" + searchletQueryTuple.getEventConfigId() + "/_search", searchletQueryTuple.getEsQueryJson(), Map.class, Collections.emptyMap());
            System.out.println(" ---- " + searchResponse);
            Map hits = (Map) searchResponse.get("hits");
            List<Map> hitsList = (List<Map>) hits.get("hits");
            if (!hitsList.isEmpty()) {
                response = hitsList.stream()
                        .map(doc -> (Map) doc.get("_source"))
                        .collect(toList());
            } else {
                // Aggregations.
                Map aggs = (Map) searchResponse.get("aggregations");
                if (aggs != null && !aggs.isEmpty()) {
                    String aggName = aggs.keySet().iterator().next().toString();
                    response = (List<Map>) ((Map) aggs.get(aggName)).get("buckets");
                }
            }
            getSender().tell(response, getSelf());
        } else {
            getSender().tell(Collections.emptyList(), getSelf());
        }
    }
}
