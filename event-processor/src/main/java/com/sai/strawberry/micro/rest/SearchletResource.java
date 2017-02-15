package com.sai.strawberry.micro.rest;

import akka.actor.ActorRef;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.Searchlet;
import com.sai.strawberry.micro.actor.ESSearchActor;
import com.sai.strawberry.micro.actor.RepositoryActor;
import com.sai.strawberry.micro.config.ActorFactory;
import com.sai.strawberry.micro.model.SearchletQueryTuple;
import com.sai.strawberry.micro.util.CallbackFunctionLibrary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import scala.concurrent.Future;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by saipkri on 08/07/16.
 */
@Api("Rest API to access and utilize the searchlets available")
@RestController
public class SearchletResource {

    private final ActorFactory actorFactory;
    private final String esUrl;
    private final ObjectMapper mapper = new ObjectMapper();


    @Inject
    public SearchletResource(final ActorFactory actorFactory, @Value("${esUrl}") final String esUrl) {
        this.actorFactory = actorFactory;
        this.esUrl = esUrl;
    }

    @ApiOperation("Gets the searchlet info for a config id")
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
                    deferredResult.setResult(new ResponseEntity<>(searchlet.newSearchCriteria(), HttpStatus.OK));
                } else {
                    new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            }
        }, actorFactory.executionContext());

        results.onFailure(failureCallback, actorFactory.executionContext());
        return deferredResult;
    }

    @ApiOperation("Performs a search using a searchlet")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/searchlet/{eventStreamConfigId}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public DeferredResult<ResponseEntity<?>> searchlet(@PathVariable("eventStreamConfigId") final String eventStreamConfigId, @RequestBody final Map searchCriteriaJson) throws Exception {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>(50000L);
        ActorRef repositoryActor = actorFactory.newActor(RepositoryActor.class);
        ActorRef esSearchActor = actorFactory.newActor(ESSearchActor.class);

        Future<Object> results = Patterns.ask(repositoryActor, EventConfig.class, RepositoryActor.timeout_in_seconds);
        OnFailure failureCallback = CallbackFunctionLibrary.onFailure(t -> deferredResult.setErrorResult(new ResponseEntity<>(t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)));

        results.onSuccess(new OnSuccess<Object>() {
            public void onSuccess(final Object results) {
                List<EventConfig> results1 = (List<EventConfig>) results;
                results1.stream()
                        .filter(con -> con.getConfigId().equals(eventStreamConfigId.trim()))
                        .findFirst()
                        .filter(eventConfig -> StringUtils.isNotBlank(eventConfig.getSearchletClass()))
                        .map(con -> {
                            try {
                                Searchlet searchletInstance = (Searchlet) Class.forName(con.getSearchletClass()).getDeclaredConstructor(EventConfig.class).newInstance(con);
                                XContentBuilder queryBuilder = jsonBuilder();
                                String esQuery = searchletInstance.toElasticsearchQuery(mapper.convertValue(searchCriteriaJson, searchletInstance.searchCriteriaClass()), queryBuilder);
                                Future<Object> searchResponse = Patterns.ask(esSearchActor, new SearchletQueryTuple(con.getConfigId(), esQuery), RepositoryActor.timeout_in_seconds * 100);
                                searchResponse.onSuccess(new OnSuccess<Object>() {
                                    public void onSuccess(final Object _results) {
                                        List<Map> searchRes = (List<Map>) _results;
                                        deferredResult.setResult(new ResponseEntity<>(searchRes, HttpStatus.OK));
                                    }
                                }, actorFactory.executionContext());
                                return searchletInstance;
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }).orElseGet(() -> {
                    deferredResult.setResult(new ResponseEntity<>(HttpStatus.NOT_FOUND));
                    return null;
                });

            }
        }, actorFactory.executionContext());

        results.onFailure(failureCallback, actorFactory.executionContext());
        return deferredResult;
    }
}
