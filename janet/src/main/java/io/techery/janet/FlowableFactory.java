package io.techery.janet;

import io.reactivex.Flowable;

interface FlowableFactory<A> {
    Flowable<ActionState<A>> create(A action);
}
