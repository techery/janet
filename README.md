# Janet

Reactive library for command-based architecture creating. It can be used for both Android and Java.

### What does Janet give?

1. Flexibility and scalability. Scale functionality using [services](/janet/src/main/java/io/techery/janet/ActionService.java)
2. Reactive approach for any actions manipulating with a help of [RXJava](https://github.com/ReactiveX/RxJava)
3. Throw-safety architecture.
 
### Introduction

Janet helps to write clear maintainable code because each individual operation is an individual class where this operation is described. Let's call it as Action. 

But Janet doesn't perform actions. For that Janet uses services that has algorithm of action processing. Each action is linked to service using an annotation that is defined in the method `ActionService.getSupportedAnnotationType()`. Janet is like action router that can send and receive actions using added services. And service knows what to do with the action. 

To use any service add it to the [Builder](/janet/src/main/java/io/techery/janet/Janet.java) using method `addService`

```java
    Janet janet = new Janet.Builder()
             .addService(new HttpActionService(API_URL, new OkClient(), new GsonConverter(new Gson())))
```

At this moment there are 3 services in Janet:

1. [HttpActionService](#httpactionservice) to provide HTTP/HTTPS requests execution
2. [AsyncActionService](#asyncactionservice) to provide support async protocols like [socket.io](http://socket.io/)
3. [CommandActionService](#commandactionservice) to invoke custom logic as [command](#https://en.wikipedia.org/wiki/Command_pattern)   

Also there is an ability to add custom service if needed

After Janet's instance creation all works with action are performed using [ActionPipe](#ActionPipe)  

### ActionPipe

End tool for sending and receiving actions with specific type using RXJava. [ActionPipe](/janet/src/main/java/io/techery/janet/ActionPipe.java) works with actions asynchronously. Create instances using method `Janet.createPipe`.
For example,
```java
    ActionPipe<UsersAction> usersPipe = janet.createPipe(UsersAction.class);
    
    usersPipe.observeSuccess()
                    .subscribe();
    
    usersPipe.createObservable(new UsersAction())
                    .subscribe();
    
    usersPipe.send(new UsersAction());
```


###  HttpActionService

Each HTTP request for [HttpActionService](/janet-http/http-service/src/main/java/io/techery/janet/HttpActionService.java) is an individual class that contains all information about the request and response.  

Http action must be annotated with [@HttpAction](/janet-http/http-service/src/main/java/io/techery/janet/http/annotations/HttpAction.java)
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
    ExampleDataModel exampleDataModel;
    @ResponseHeader("Example-Responseheader-Name")
    String responseHeaderValue;
}
```

### AsyncActionService

[AsyncActionService](/janet-async/async-service/src/main/java/io/techery/janet/AsyncActionService.java) performs actions with annotation [@AsyncAction](/janet-async/async-service/src/main/java/io/techery/janet/async/annotations/AsyncAction.java). Every action is async message that contains message data as a field annotated with [@AsyncMessage](/janet-async/async-service/src/main/java/io/techery/janet/async/annotations/AsyncMessage.java).
 
Also [AsyncActionService](/master/janet-async/async-service/src/main/java/io/techery/janet/AsyncActionService.java) has algorithm to synchronize outcoming and incoming messages. To receive action response may add field with annotation [@SyncedResponse](/master/janet-async/async-service/src/main/java/io/techery/janet/async/annotations/SyncedResponse.java). Type of that field must be a class of incoming action. To link action with its response set class in the annotation implemented by [SyncPredicate](/janet-async/async-service/src/main/java/io/techery/janet/async/SyncPredicate.java) where the condition for synchronization present.
```java
@AsyncAction(value = "test", incoming = true)
public class TestAction {

    @AsyncMessage
    Body body;

    @SyncedResponse(value = TestSyncPredicate.class, timeout = 3000)
    TestAction response;

    @Override public String toString() {
        return "TestAction{" +
                "body=" + body +
                ", response=" + response +
                '}';
    }

    public static class TestSyncPredicate implements SyncPredicate<TestAction, TestAction> {

        @Override public boolean isResponse(TestAction requestAction, TestAction response) {
            return requestAction.body.id == response.body.id;
        }
    }
}
```

### CommandActionService

[CommandActionService](/janet-command/command-service/src/main/java/io/techery/janet/CommandActionService.java) performs actions executing with a help of annotation [@CommandAction](/janet-command/command-service/src/main/java/io/techery/janet/command/annotations/CommandAction.java). Also to create command action it's necessary to implement the interface [CommandActionBase](/janet-command/command-service/src/main/java/io/techery/janet/CommandActionBase.java). It contains the command's methods for running and cancellation. To get command result use method `getResult()`
 
```java
@CommandAction
public class ExampleCommandAction extends CommandActionBase<String> {

    @Override protected String run(CommandCallback callback) throws Throwable {
        //perform logic to return result
    }

    @Override public void cancel() {
        //cancellation if needed
    }
}
```
  

# Download 
[![](https://jitpack.io/v/techery/janet.svg)](https://jitpack.io/#techery/janet)

Grab via Maven
```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
        <url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
    <groupId>com.github.techery.janet</groupId>
    <artifactId>janet</artifactId>
    <version>0.0.9</version>
</dependency>
```
or Gradle:
```groovy
repositories {
    ...
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.techery.janet:janet:0.0.9'
}
```

List of additional artifacts:
```groovy
compile 'com.github.techery.janet:http-service:0.0.9'
apt 'com.github.techery.janet:http-service-compiler:0.0.9'   
compile 'com.github.techery.janet:okhttp:0.0.9'
compile 'com.github.techery.janet:android-apache-client:0.0.9'
compile 'com.github.techery.janet:url-connection:0.0.9'

compile 'com.github.techery.janet:async-service:0.0.9'
apt 'com.github.techery.janet:async-service-compiler:0.0.9'
compile 'com.github.techery.janet:nkzawa-socket.io:0.0.9'
compile 'com.github.techery.janet:socket.io:0.0.9'

compile 'com.github.techery.janet:gson:0.0.9'
compile 'com.github.techery.janet:protobuf:0.0.9'

compile 'com.github.techery.janet:command-service:0.0.9'
```
