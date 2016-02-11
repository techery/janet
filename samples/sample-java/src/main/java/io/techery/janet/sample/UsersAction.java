package io.techery.janet.sample;


import java.util.ArrayList;

import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.annotations.Query;
import io.techery.janet.http.annotations.Response;

@HttpAction("/users")
public class UsersAction extends BaseAction{

    @Query("since") int since = 0;

    @Response
    ArrayList<User> response;

    @Override
    public String toString() {
        return "UsersAction{" +
                "since=" + since +
                ", response=" + response +
                '}';
    }
}
