package com.druid.mapcore.notify;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.druid.mapcore.R;


/**
 * Created by LeaAnder on 2017/1/13.
 */
public class PermissionsTips extends Dialog implements View.OnClickListener {

    public interface PermissionClickListener {
        void clickPermissionCancel();

        void clickPermissionConfirm();
    }

    private TextView tv_title;
    private TextView tv_content_tips;
    private TextView tv_content;
    private TextView tv_attention;
    private TextView tv_confirm;
    private TextView tv_cancel;


    private Context context;
    private PermissionClickListener listener;

    public PermissionsTips(Context context) {
        super(context);
        this.context = context;
        getContext().setTheme(R.style.DialogStyle);
        View view = View.inflate(getContext(), R.layout.app_permission_tips, null);
        setContentView(view);
        getWindow().getAttributes().gravity = Gravity.CENTER;
        setCanceledOnTouchOutside(true);

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_content_tips = (TextView) findViewById(R.id.tv_content_tips);
        tv_content = (TextView) findViewById(R.id.tv_content);
        tv_attention = (TextView) findViewById(R.id.tv_attention);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_confirm.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
    }

    public void setTitle(String title) {
        tv_title.setText(title);
    }

    public void setContent(String content) {
        tv_content.setText(content);
    }

    public void setContentGravity(int pos) {
        tv_content.setGravity(pos);
    }

    public void setContentTips(String content) {
        tv_content_tips.setText(content);
    }

    public void setContentTipsGravity(int pos) {
        tv_content_tips.setGravity(pos);
    }

    public void setAttention(String text) {
        tv_attention.setText(text);
    }

    public void setConfirm(String text) {
        tv_confirm.setText(text);
    }

    public void setConfirmBg(int drawable) {
        tv_confirm.setTextColor(drawable);
    }

    public void setCancel(String text) {
        tv_cancel.setText(text);
    }

    public void setListener(PermissionClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_confirm) {
            listener.clickPermissionConfirm();
            stop();
        }
        if (id == R.id.tv_cancel) {
            listener.clickPermissionCancel();
            stop();
        }
    }

    @Override
    public void show() {
        super.show();
    }

    public void stop() {
        if (isShowing()) {
            dismiss();
        }
    }
}
