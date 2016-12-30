package com.sai.strawberry.micro.rest;

import akka.actor.ActorRef;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.Searchlet;
import com.sai.strawberry.micro.actor.RepositoryActor;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.util.CallbackFunctionLibrary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import scala.concurrent.Future;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by saipkri on 08/07/16.
 */
@Api("Rest API to access and utilize the searchlets available")
@RestController
public class SearchletResource {

    private final ActorFactory actorFactory;


    @Inject
    public SearchletResource(final ActorFactory actorFactory) {
        this.actorFactory = actorFactory;
    }

    @ApiOperation("Gets the list of searchlet info")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/searchlet/{eventStreamConfigId}", method = RequestMethod.GET, produces = "application/json")
    public DeferredResult<ResponseEntity<?>> searchlet(@PathVariable("eventStreamConfigId") final String eventStreamConfigId) throws Exception {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>(5000L);
        ActorRef repositoryActor = actorFactory.newActor(RepositoryActor.class);

        Future<Object> results = Patterns.ask(repositoryActor, EventConfig.class, RepositoryActor.timeout_in_seconds);
        OnFailure failureCallback = CallbackFunctionLibrary.onFailure(t -> deferredResult.setErrorResult(new ResponseEntity<>(t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)));

        results.onSuccess(new OnSuccess<Object>() {
            public void onSuccess(final Object results) {
                List<EventConfig> results1 = (List<EventConfig>) results;
                Searchlet<?> searchlet = (Searchlet<?>) results1.stream()
                        .filter(con -> con.getConfigId().equals(eventStreamConfigId.trim()))
                        .findFirst()
                        .filter(eventConfig -> StringUtils.isNotBlank(eventConfig.getSearchletClass()))
                        .map(con -> {
                            try {
                                return Class.forName(con.getSearchletClass()).getDeclaredConstructor(EventConfig.class).newInstance(con);
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }).orElse(null);
                if (searchlet != null) {
                    deferredResult.setResult(new ResponseEntity<Object>(searchlet.newSearchCriteria(), HttpStatus.OK));
                } else {
                    new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            }
        }, actorFactory.executionContext());

        results.onFailure(failureCallback, actorFactory.executionContext());
        return deferredResult;
    }
}
