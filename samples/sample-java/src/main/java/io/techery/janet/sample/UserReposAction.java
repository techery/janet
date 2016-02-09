package io.techery.janet.sample;

import com.santarest.annotations.Path;
import com.santarest.annotations.Response;
import com.santarest.annotations.RestAction;

import java.util.ArrayList;

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

    @Override
    public String toString() {
        return "UserReposAction{" +
                "login='" + login + '\'' +
                ", repositories=" + repositories +
                '}';
    }
}
