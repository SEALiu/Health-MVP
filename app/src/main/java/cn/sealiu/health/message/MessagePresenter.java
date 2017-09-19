package cn.sealiu.health.message;

import android.support.annotation.NonNull;

import cn.sealiu.health.profile.ProfileContract;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class MessagePresenter implements MessageContract.Presenter {

    private final MessageContract.View mMessageView;

    @Override
    public void start() {
        loadMessages();
    }

    public MessagePresenter(@NonNull MessageContract.View messageView) {
        mMessageView = checkNotNull(messageView);
        mMessageView.setPresenter(this);
    }

    @Override
    public void loadMessages() {
        mMessageView.setLoadingIndicator(true);
    }
}
