package com.sai.strawberry.micro.util;

import akka.dispatch.Mapper;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by saipkri on 11/07/16.
 */
public final class CallbackFunctionLibrary {

    public static <I, O> OnSuccess<I> onSuccess(final Function<I, O> callbackFunctionBody) {
        return new OnSuccess<I>() {
            @Override
            public void onSuccess(final I result) throws Throwable {
                callbackFunctionBody.apply(result);
            }
        };
    }

    public static <I> OnSuccess<I> onSuccess(final Consumer<I> callbackFunctionBody) {
        return new OnSuccess<I>() {
            @Override
            public void onSuccess(final I result) throws Throwable {
                callbackFunctionBody.accept(result);
            }
        };
    }

    public static OnFailure onFailure(final Consumer<Throwable> callbackFunctionBody) {
        return new OnFailure() {
            @Override
            public void onFailure(final Throwable failure) throws Throwable {
                callbackFunctionBody.accept(failure);
            }
        };
    }

    public static <O> Mapper<Object, O> map(final Function<Object, O> conversionFunction) {
        return new Mapper<Object, O>() {
            public O apply(final Object input) {
                return conversionFunction.apply(input);
            }
        };
    }
}
