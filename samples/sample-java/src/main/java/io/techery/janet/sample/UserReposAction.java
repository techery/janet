package io.techery.janet.sample;

import java.util.ArrayList;

import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.annotations.Path;
import io.techery.janet.http.annotations.Response;

@HttpAction("/users/{login}/repos1")
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

    @Override
    public String toString() {
        return "UserReposAction{" +
                "login='" + login + '\'' +
                ", repositories=" + repositories +
                '}';
    }
}
