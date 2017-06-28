## Janet
Build command-based architecture in reactive manner 

・︎︎ [![Join the chat at https://gitter.im/janet-java/Lobby](https://badges.gitter.im/janet-java/Lobby.svg)](https://gitter.im/janet-java/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
## Introduction
Janet provides infrastructure to build flexible, scalable and resilient 
architecture based on actions, `RxJava`-powered pipes and services to execute those actions.
You can learn more about our framework from this [presentation](https://speakerdeck.com/dirong/janet-build-command-based-architecture-in-reactive-manner) 
  
Lets walk through common flow for http request: 

![overview](/assets/readme-overview.png)

* Janet is equipped with services to deal with different actions:
    ```java
    Janet janet = new Janet.Builder()
        .addService(new HttpActionService(API_URL, httpClient, converter))
        .addService(new SqlActionService(...))
        .addService(new XyzActionService(...))
        .build();
    ```
    
1. Our request is described with `SampleHttpAction` class:
    ```java
    @HttpAction(value = "/demo", method = GET)
    public class SampleHttpAction {
        @Response SampleData responseData;
    }
    ```
    
    Request is sent/observed with it's `ActionPipe`:
    ```java
    // create pipe for action class with janet
    ActionPipe<SampleHttpAction> actionPipe = janet.createPipe(SampleHttpAction.class);
    
    // register request result observer
    actionPipe.observe().subscribe(new ActionStateSubscriber<SampleHttpAction>()
        .onStart(action -> System.out.println("Request is being sent " + action))
        .onProgress((action, progress) -> System.out.println("Request in progress: " + progress))
        .onSuccess(action -> System.out.println("Request finished " + action))
        .onFail((action, throwable) -> System.err.println("Request failed " + throwable))
    );
    
    // send request
    actionPipe.send(new SampleHttpAction());
    // or actionPipe.createObservable(new SampleHttpAction()).subscribe(...) 
    ```
2. Request is forwarded to `Janet` instance upon `send` or `subscribe` call;
3. `Janet` finds suitable service to execute action and routes to it;
4. `ActionService` knows how to deal with action and sets up `progress`/`success`/`fail` statuses;
5. Resulting action is brought back to `Janet` instance;
6. `Janet` routes resulting action with status to dedicated pipes;
7. Dedicated `ActionPipe` notifies all it's observers of action wrapped with current status;

So `Janet` instance itself stands for routing, delegating diff job to other components:

* `ActionPipe` – action operator, the only way to send action and receive result;
* `ActionService` – knows how to deal with action;
* `ActionServiceWrapper` – decorator to put additional logic for underlying service;

### Available services
Janet abilities depends on services.
Currently there are:

* [HttpActionService](https://github.com/techery/janet-http) to provide HTTP/HTTPS requests execution;
* [AsyncActionService](https://github.com/techery/janet-async) to provide support for async protocols, e.g. [socket.io](http://socket.io/);
* [CommandActionService](https://github.com/techery/janet-command) to delegate job back to command `action`.
   
Possible solutions: 
`SqlActionService`, `LocationActionService`, `BillingActionService`, etc.

## Components
### ActionPipe
The only way to operate with `action` – via it's `ActionPipe`.
It's created for particular action class:
```java
// Pipe for users list request
ActionPipe<GetUsersAction> usersPipe = janet.createPipe(GetUsersAction.class);
// Pipe for repositories list request
ActionPipe<GetReposAction> repositoriesPipe = janet.createPipe(GetReposAction.class);
```

Action result is provided via `ActionState` observable:
```java
Observable<ActionState<GetUsersAction>> usersObservable = usersPipe.observe();
```

`ActionState` includes:
* state – `start`/`progress`/`success`/`fail`;
* action instance itself;
* progress value;
* exception for `fail` status

To send new action for execution:
```java
usersPipe.send(new GetUsersAction());
```

To combine sending and observing at once:
```java
usersPipe.createObservable(new GetUsersAction()).subscribe(...);
// every other pipe's observer will get result too
```

Other capabilities:
* observe latest cached result (aka replay(1)) or clear cache;
* observe success-only results;
* observe base parent actions; 
* cancel action execution;
* create safe read-only pipe forks to listen for results only;

### ActionService
`ActionService` is responsible for execution of particular actions. 
It defines what actions it's able to process, so `janet` knows where to route 'em.

Every service should override 3 methods:
* `getSupportedAnnotationType()` - defines what actions are processed by their class annotation;
* `<A> void sendInternal(ActionHolder<A> holder)` – is called upon new action is sent to pipe;
* `<A> void cancel(ActionHolder<A> holder)` – is called upon action is canceled from pipe;

There are several services to look at:
* Simple impl. -> [CommandActionService](https://github.com/techery/janet-command);
* Complex impl. -> [HttpActionService](https://github.com/techery/janet-http).

### ActionServiceWrapper
Decorator for `ActionService` is used to listen for action status or add additional intercepting logic.

Abilities:
* Listen for action flow by statuses;
* Intercept sending completely;
* Intercept fail-status to start a retry;

Simple logging wrapper:
```java
public class LoggingWrapper extends ActionServiceWrapper {

    public LoggingWrapper(ActionService actionService) {
        super(actionService);
    }

    @Override protected <A> boolean onInterceptSend(ActionHolder<A> holder) {
        System.out.println("send " + holder.action());
        return false;
    }

    @Override protected <A> void onInterceptCancel(ActionHolder<A> holder) {
        System.out.println("cancel " + holder.action());
    }

    @Override protected <A> void onInterceptStart(ActionHolder<A> holder) {
        System.out.println("onStart " + holder.action());
    }

    @Override protected <A> void onInterceptProgress(ActionHolder<A> holder, int progress) {
        System.out.println("onProgress " + holder.action() + ", progress " + progress);
    }

    @Override protected <A> void onInterceptSuccess(ActionHolder<A> holder) {
        System.out.println("onSuccess " + holder.action());
    }

    @Override protected <A> void onInterceptFail(ActionHolder<A> holder, JanetException e) {
        System.out.println("onFail " + holder.action());
        e.printStackTrace();
    }
}

@Provides Janet createJanet() {
    return new Janet.Builder()
        .addService(new LoggingWrapper(new HttpActionService(API_URL, httpClient, converter)))
        .build();
}
```

Examples:
* Authorize requests via [AuthWrapper](https://github.com/techery/janet-architecture-sample/blob/master/app/src/main/java/io/techery/sample/service/AuthServiceWrapper.java)
* Log requests via [TimberWrapper](https://gist.github.com/almozavr/ccf620b4c0041552a8b8dbb2204254cb)

Possible solutions: caching middleware, `Dagger` injector, retry policy maker, etc.

## Samples
* [Simple Android app](https://github.com/techery/janet-http-android-sample)
* [Advanced Android app](https://github.com/techery/janet-architecture-sample)
* [Flux-like Android app](https://github.com/techery/janet-flux-todo)

## Janet benefits
1. Flexibility and scalability. Scale functionality using [services](/janet/src/main/java/io/techery/janet/ActionService.java);
2. Reactive approach for actions interaction by [RXJava](https://github.com/ReactiveX/RxJava);
3. Throw-safety architecture.

## Download 
[![](https://jitpack.io/v/techery/janet.svg)](https://jitpack.io/#techery/janet)
[![Build Status](https://travis-ci.org/techery/janet.svg?branch=master)](https://travis-ci.org/techery/janet)

Grab via Maven
```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
        <url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
    <groupId>com.github.techery</groupId>
    <artifactId>janet</artifactId>
    <version>latestVersion</version>
</dependency>
```
or Gradle:
```groovy
repositories {
    ...
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.techery:janet:latestVersion'
}
```

## License

    Copyright (c) 2016 Techery

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
