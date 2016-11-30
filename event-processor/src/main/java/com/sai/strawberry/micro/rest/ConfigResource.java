package com.sai.strawberry.micro.rest;

import akka.actor.ActorRef;
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
}
