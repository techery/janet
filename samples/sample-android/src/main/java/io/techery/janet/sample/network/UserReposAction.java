package io.techery.janet.sample.network;


import java.util.ArrayList;

import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.annotations.Path;
import io.techery.janet.http.annotations.Response;
import io.techery.janet.sample.model.Repository;

@HttpAction("/users/{login}/repos")
public class UserReposAction extends BaseAction {

    @Path("login")
    final String login;

    @Response
    ArrayList<Repository> repositories;

    public UserReposAction(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public ArrayList<Repository> getRepositories() {
        return repositories;
    }
}
