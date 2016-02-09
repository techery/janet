package io.techery.janet.sample.network;

import com.santarest.annotations.Query;
import com.santarest.annotations.Response;
import com.santarest.annotations.RestAction;

import java.util.ArrayList;

import io.techery.janet.sample.model.User;

@RestAction("/users")
public class UsersAction extends BaseAction {

    @Query("since")
    final int since = 0;

    @Response
    ArrayList<User> response;

    public ArrayList<User> getResponse() {
        return response;
    }
}
