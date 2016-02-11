package io.techery.janet.sample.network;


import java.util.ArrayList;

import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.annotations.Query;
import io.techery.janet.http.annotations.Response;
import io.techery.janet.sample.model.User;

@HttpAction("/users")
public class UsersAction extends BaseAction {

    @Query("since")
    final int since = 0;

    @Response
    ArrayList<User> response;

    public ArrayList<User> getResponse() {
        return response;
    }
}
