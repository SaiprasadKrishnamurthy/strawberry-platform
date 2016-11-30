package com.sai.strawberry.micro.rest;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventStreamConfig;
import com.sai.strawberry.micro.actor.ESActor;
import com.sai.strawberry.micro.actor.RepositoryActor;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.es.ESFacade;
import com.sai.strawberry.micro.util.CallbackFunctionLibrary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import scala.concurrent.Future;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by saipkri on 08/07/16.
 */
@Api("Rest API for the audit config microservice")
@RestController
public class ConfigResource {

    private final ActorFactory actorFactory;
    private final ESFacade esInitializer;

    private static final ObjectMapper m = new ObjectMapper();

    @Inject
    public ConfigResource(final ActorFactory actorFactory, final ESFacade esInitializer) {
        this.actorFactory = actorFactory;
        this.esInitializer = esInitializer;
    }

    @ApiOperation("Gets all the configs configured in the system")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/configs", method = RequestMethod.GET, produces = "application/json")
    public DeferredResult<ResponseEntity<List<EventStreamConfig>>> allEventConfigs() throws Exception {
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

    @ApiOperation("Saves a config")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/config", method = RequestMethod.PUT, produces = "application/json")
    public DeferredResult<ResponseEntity<?>> save(@RequestBody final EventStreamConfig eventStreamConfig, @RequestParam(name = "cleanupExistingIndex", required = true, defaultValue = "false") final boolean cleanupExistingIndex) throws Exception {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>(10000L);
        ActorRef repositoryActor = actorFactory.newActor(RepositoryActor.class);
        ActorRef esActor = actorFactory.newActor(ESActor.class);
        final ArrayList<Future<Object>> futures = new ArrayList<>();
        Future<Object> savedInDBFuture = Patterns.ask(repositoryActor, eventStreamConfig, RepositoryActor.timeout_in_seconds);
        Future<Object> savedInESFuture = Patterns.ask(esActor, Arrays.asList(cleanupExistingIndex, eventStreamConfig), RepositoryActor.timeout_in_seconds);
        futures.add(savedInDBFuture);
        futures.add(savedInESFuture);
        final Future<Iterable<Object>> aggregate = Futures.sequence(futures, actorFactory.executionContext());

        final Future<Object> results = aggregate.map(
                new Mapper<Iterable<Object>, Object>() {
                    public EventStreamConfig apply(Iterable<Object> coll) {
                        final Iterator<Object> it = coll.iterator();
                        final EventStreamConfig one = (EventStreamConfig) it.next();
                        final EventStreamConfig two = (EventStreamConfig) it.next();
                        return two;
                    }
                }, actorFactory.executionContext());

        OnFailure failureCallback = CallbackFunctionLibrary.onFailure(t -> deferredResult.setErrorResult(new ResponseEntity<>(t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)));

        results.onSuccess(new OnSuccess<Object>() {
            public void onSuccess(final Object results) {
                deferredResult.setResult(new ResponseEntity<>(results, HttpStatus.CREATED));
            }
        }, actorFactory.executionContext());

        results.onFailure(failureCallback, actorFactory.executionContext());
        return deferredResult;
    }
}
