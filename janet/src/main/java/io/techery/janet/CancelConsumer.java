package io.techery.janet;

interface CancelConsumer<A> {
    void accept(A action);
}
