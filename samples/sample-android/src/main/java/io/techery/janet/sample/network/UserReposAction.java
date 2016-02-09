package io.techery.janet.sample.network;

import com.santarest.annotations.Path;
import com.santarest.annotations.Response;
import com.santarest.annotations.RestAction;

import java.util.ArrayList;

import io.techery.janet.sample.model.Repository;

@RestAction("/users/{login}/repos")
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
