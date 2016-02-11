package io.techery.janet.sample.ui;

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
import io.techery.janet.sample.network.UsersAction;
import io.techery.janet.sample.ui.adapter.UsersAdapter;
import rx.android.schedulers.AndroidSchedulers;

public class UsersActivity extends RxAppCompatActivity {

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.progress_bar)
    View progress;
    @Bind(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private UsersAdapter adapter;

    private ActionPipe<UsersAction> usersPipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        ButterKnife.bind(this);
        usersPipe = App.get(this).getUsersPipe();
        setupRecyclerView();
        swipeRefreshLayout.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        usersPipe.observeWithReplay()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ActionStateSubscriber<UsersAction>()
                        .onStart(() -> showProgressLoading(true))
                        .onSuccess(usersAction -> {
                            adapter.setData(usersAction.getResponse());
                            showProgressLoading(false);
                        })
                        .onFail(throwable -> showProgressLoading(false)));
        loadUsers();
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new UsersAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, position) -> UserReposActivity.start(this, adapter.getItem(position)));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadUsers();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadUsers() {
        usersPipe.send(new UsersAction());
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
}