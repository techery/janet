package io.techery.janet.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.techery.janet.ActionPipe;
import io.techery.janet.helper.ActionStateSubscriber;
import io.techery.janet.sample.App;
import io.techery.janet.sample.R;
import io.techery.janet.sample.model.User;
import io.techery.janet.sample.network.UserReposAction;
import io.techery.janet.sample.ui.adapter.UserReposAdapter;
import rx.android.schedulers.AndroidSchedulers;

public class UserReposActivity extends RxAppCompatActivity {

    private final static String EXTRA_USER = "user";

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.progress_bar)
    View progress;
    @Bind(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private User user;
    private UserReposAdapter adapter;
    private ActionPipe<UserReposAction> userReposPipe;

    public static void start(Context context, User user) {
        Intent intent = new Intent(context, UserReposActivity.class);
        intent.putExtra(EXTRA_USER, user);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        ButterKnife.bind(this);
        userReposPipe = App.get(this).getUserReposPipe();
        restoreState(savedInstanceState);
        setupRecyclerView();
        swipeRefreshLayout.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userReposPipe.observeWithReplay()
                .filter(state -> user.getLogin().equals(state.action.getLogin()))
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ActionStateSubscriber<UserReposAction>()
                        .onStart((action) -> showProgressLoading(true))
                        .onSuccess(action -> {
                            adapter.setData(action.getRepositories());
                            showProgressLoading(false);
                        })
                        .onFail((action, throwable) -> showProgressLoading(false)));
        loadRepos();
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new UserReposAdapter(this);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadRepos();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadRepos() {
        userReposPipe.send(new UserReposAction(user.getLogin()));
    }

    private void showProgressLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }


    @Override
    public void onPause() {
        super.onPause();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_USER, user);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable(EXTRA_USER);
        } else {
            user = getIntent().getParcelableExtra(EXTRA_USER);
        }
    }
}
