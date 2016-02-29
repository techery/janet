# Janet

Reactive library to create command-based architecture. It can be used for both Android and Java.

### What does Janet give?
1. Flexibility and scalability. Scale functionality using [services](https://github.com/techery/janet/blob/master/janet/src/main/java/io/techery/janet/ActionAdapter.java)
2. Reactive approach to manipulate any actions with [RXJava](https://github.com/ReactiveX/RxJava)
3. Throw-safety architecture.
 
### Introduction

Janet helps write clear maintainable code because each an individual operation is an individual class where this operation is described. Let's call it as Action. 

But Janet doesn't perform actions. For that Janet uses services that has algorithm of action processing. Each action is linked to service using an annotation that defined in the method ActionService.getSupportedAnnotationType(). Janet is like action router that can send and receive actions using added services that know what to do with them. 

To use any service add it to the [Builder](https://github.com/techery/janet/blob/readme/janet/src/main/java/io/techery/janet/Janet.java) using method `addService`

```java
    Janet janet = new Janet.Builder()
             .addService(new HttpActionService(API_URL, new OkClient(), new GsonConverter(new Gson())))
```

Currently there are 3 services in Janet:

1. [HttpActionService](#HttpActionService) to provide HTTP/HTTPS requests execution
2. [AsyncActionService](#AsyncActionService) to provide support async protocols like [socket.io](http://socket.io/)
3. [CommandActionService](#CommandActionService) to invoke custom logic as [command](#https://en.wikipedia.org/wiki/Command_pattern)   
Also there is an ability to add custom service if needed

After Janet's instance creation all works with action performs using [ActionPipe](#ActionPipe)  

### ActionPipe

End tool for sending and receiving actions with specific type using RXJava. ActionPipe can work with actions synchronously or asynchronously. Create instances using method `Janet.createPipe`.
For example,
```java
    ActionPipe<UsersAction> usersPipe = janet.createPipe(UsersAction.class);
    
    usersPipe.observeSuccess()
                    .subscribe();
    
    usersPipe.createObservable(new UsersAction())
                    .subscribe();
    
    usersPipe.send(new UsersAction());
```


#### HttpActionService

Each HTTP request in Janet is an individual class that contains all information about the request and response.  

Http action must be annotated with [@HttpAction](https://github.com/techery/janet/blob/readme/janet-http/http-service/src/main/java/io/techery/janet/http/annotations/HttpAction.java)
```java
@HttpAction(value = "/demo", method = HttpAction.Method.GET)
public class ExampleAction {}
```

To configure request, Action fields can be annotated with:
* `@Path` for path value
* `@Query` for request URL parameters
* `@Body` for POST request body
* `@RequestHeader` for request headers
* `@Field` for request fields if request type is `HttpAction.Type.FORM_URL_ENCODED`
* `@Part` for multipart request parts

To process response, special annotations can be used:
* `@Response` for getting response body.
* `@Status` for getting response status. Field types `Integer`, `Long`, `int` or `long` can be used to get status code or use `boolean` to know that request was sent successfully
* `@ResponseHeader` for getting response headers

```java
@HttpAction(value = "/demo/{examplePath}/info",
        type = HttpAction.Type.SIMPLE,
        method = HttpAction.Method.GET)
public class ExampleAction {
    @Path("examplePath")
    String ownerr;
    @Query("repo")
    int query;
    @RequestHeader("Example-Header-Name")
    String requestHeaderValue;
    @Status
    int statusCode;
    @Body
    ExampleModel exampleModel;
    @Response
    ExampleDataModel exampleDataModel;не
    @ResponseHeader("Example-Responseheader-Name")
    String responseHeaderValue;
}
```

### AsyncActionService

### CommandActionService

### Master Build Status
[![](https://jitpack.io/v/techery/janet.svg)](https://jitpack.io/#techery/janet)
