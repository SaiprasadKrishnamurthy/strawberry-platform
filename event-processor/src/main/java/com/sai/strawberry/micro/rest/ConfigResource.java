package com.sai.strawberry.micro.rest;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventStreamConfig;
import com.sai.strawberry.micro.actor.RepositoryActor;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.util.CallbackFunctionLibrary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import scala.concurrent.Future;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by saipkri on 08/07/16.
 */
@Api("Configuration related API")
@RestController
public class ConfigResource {

    private final ActorFactory actorFactory;

    private static final ObjectMapper m = new ObjectMapper();

    @Value("${kibanaUrl}")
    private String kibanaUrl;

    @Inject
    public ConfigResource(final ActorFactory actorFactory) {
        this.actorFactory = actorFactory;
    }

    @ApiOperation("Gets the configs stored in the system")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/config", method = RequestMethod.GET, produces = "application/json")
    public DeferredResult<ResponseEntity<List<EventStreamConfig>>> configs() throws Exception {
        DeferredResult<ResponseEntity<List<EventStreamConfig>>> deferredResult = new DeferredResult<>(5000L);
        ActorRef repositoryActor = actorFactory.newActor(RepositoryActor.class);

        Future<Object> results = Patterns.ask(repositoryActor, EventStreamConfig.class, RepositoryActor.timeout_in_seconds);
        OnFailure failureCallback = CallbackFunctionLibrary.onFailure(t -> deferredResult.setErrorResult(new ResponseEntity<>(t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)));

        results.onSuccess(new OnSuccess<Object>() {
            public void onSuccess(final Object results) {
                deferredResult.setResult(new ResponseEntity<>((List<EventStreamConfig>) results, HttpStatus.OK));
            }
        }, actorFactory.executionContext());

        results.onFailure(failureCallback, actorFactory.executionContext());
        return deferredResult;
    }

    @ApiOperation("Gets the ops dashboard")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/ops-dashboard", method = RequestMethod.GET, produces = "text/html")
    public DeferredResult<ResponseEntity<String>> dashboard() throws Exception {
        DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>(5000L);
        StringBuilder out = new StringBuilder();
        out.append("<iframe src=\"").append(kibanaUrl)
                .append("/app/kibana#/dashboard/")
                .append("Strawberry-Ops-Dashboard")
                .append("?_g=(refreshInterval:(display:'5%20seconds',pause:!f,section:1,value:5000),time:(from:now-15m,mode:quick,to:now))&_a=(filters:!(),options:(darkTheme:!t),panels:!((col:1,id:Total-number-of-events-processed-metric,panelIndex:1,row:1,size_x:3,size_y:3,type:visualization),(col:4,id:Event-Stream-Processing-Timing-Trend,panelIndex:2,row:1,size_x:9,size_y:3,type:visualization),(col:1,id:Events-Streams-Notification,panelIndex:3,row:4,size_x:12,size_y:5,type:visualization),(col:1,id:Number-of-Events-Trend,panelIndex:4,row:9,size_x:12,size_y:4,type:visualization)),query:(query_string:(analyze_wildcard:!t,query:'*')),title:'Strawberry%20Ops%20Dashboard',uiState:())")
                .append("\"")
                .append(" embed=\"true\" ")
                .append(" width=\"100%\"")
                .append(" height=\"900\" />");

        Future<Object> results = Futures.successful(out);

        results.onSuccess(new OnSuccess<Object>() {
            public void onSuccess(final Object results) {
                deferredResult.setResult(new ResponseEntity<>(results.toString(), HttpStatus.OK));
            }
        }, actorFactory.executionContext());

        return deferredResult;
    }
}
