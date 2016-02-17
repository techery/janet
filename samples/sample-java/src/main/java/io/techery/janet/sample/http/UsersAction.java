package io.techery.janet.sample.http;


import java.util.ArrayList;

import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.annotations.Query;
import io.techery.janet.http.annotations.Response;

@HttpAction("/users")
public class UsersAction extends BaseAction {

    @Query("since")
    final int since = 0;

    @Response
    ArrayList<io.techery.janet.sample.http.User> response;

    @Override
    public String toString() {
        return "UsersAction{" +
                "since=" + since +
                ", response=" + response +
                '}';
    }
}
