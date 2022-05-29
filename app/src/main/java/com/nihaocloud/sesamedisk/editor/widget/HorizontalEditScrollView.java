package com.nihaocloud.sesamedisk.editor.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.nihaocloud.sesamedisk.R;
import com.nihaocloud.sesamedisk.editor.controller.BlockQuotesController;
import com.nihaocloud.sesamedisk.editor.controller.CodeController;
import com.nihaocloud.sesamedisk.editor.controller.HeaderController;
import com.nihaocloud.sesamedisk.editor.controller.HorizontalRulesController;
import com.nihaocloud.sesamedisk.editor.controller.LinkController;
import com.nihaocloud.sesamedisk.editor.controller.ListController;
import com.nihaocloud.sesamedisk.editor.controller.StrikeThroughController;
import com.nihaocloud.sesamedisk.editor.controller.StyleController;
import com.nihaocloud.sesamedisk.editor.controller.TodoController;
import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.MarkdownEditText;

public class HorizontalEditScrollView extends FrameLayout implements View.OnClickListener,
        View.OnLongClickListener {
    private MarkdownEditText mMarkdownEditText;

    private HeaderController mHeaderController;
    private StyleController mStyleController;
    private HorizontalRulesController mHorizontalRulesController;
    private TodoController mTodoController;
    private StrikeThroughController mStrikeThroughController;
    private CodeController mCodeController;
    private BlockQuotesController mBlockQuotesController;
    private ListController mListController;
    private LinkController mLinkController;

    public HorizontalEditScrollView(Context context) {
        this(context, null);
    }

    public HorizontalEditScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalEditScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_horizontal_scroll, this, true);
    }

    public void setEditTextAndConfig(@NonNull MarkdownEditText markdownEditText, @NonNull MarkdownConfiguration markdownConfiguration) {
        mMarkdownEditText = markdownEditText;
        mHeaderController = new HeaderController(markdownEditText, markdownConfiguration);
        mStyleController = new StyleController(markdownEditText);
        mHorizontalRulesController = new HorizontalRulesController(markdownEditText);
        mTodoController = new TodoController(markdownEditText);
        mStrikeThroughController = new StrikeThroughController(markdownEditText);
        mCodeController = new CodeController(markdownEditText);
        mBlockQuotesController = new BlockQuotesController(markdownEditText);
        mListController = new ListController(markdownEditText);
        mLinkController = new LinkController(markdownEditText);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.img_header1).setOnClickListener(this);
        findViewById(R.id.img_bold).setOnClickListener(this);
        findViewById(R.id.img_italic).setOnClickListener(this);
        findViewById(R.id.img_horizontal_rules).setOnClickListener(this);
        findViewById(R.id.img_todo).setOnClickListener(this);
        findViewById(R.id.img_todo_done).setOnClickListener(this);
        findViewById(R.id.img_strike_through).setOnClickListener(this);
        findViewById(R.id.img_code).setOnClickListener(this);
        findViewById(R.id.img_block_quote).setOnClickListener(this);
        findViewById(R.id.img_block_quote).setOnLongClickListener(this);
        findViewById(R.id.img_unorder_list).setOnClickListener(this);
        findViewById(R.id.img_order_list).setOnClickListener(this);
        findViewById(R.id.img_link).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mMarkdownEditText == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.img_header1:
                mHeaderController.doHeader(1);
                break;
            case R.id.img_bold:
                mStyleController.doBold();
                break;
            case R.id.img_italic:
                mStyleController.doItalic();
                break;
            case R.id.img_horizontal_rules:
                mHorizontalRulesController.doHorizontalRules();
                break;
            case R.id.img_todo:
                mTodoController.doTodo();
                break;
            case R.id.img_todo_done:
                mTodoController.doTodoDone();
                break;
            case R.id.img_strike_through:
                mStrikeThroughController.doStrikeThrough();
                break;
            case R.id.img_code:
                mCodeController.doCode();
                break;
            case R.id.img_block_quote:
                mBlockQuotesController.doBlockQuotes();
                break;
            case R.id.img_unorder_list:
                mListController.doUnOrderList();
                break;
            case R.id.img_order_list:
                mListController.doOrderList();
                break;
            case R.id.img_link:
                mLinkController.doImage();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.img_block_quote:
                mBlockQuotesController.addNestedBlockQuotes();
                break;
        }
        return true;
    }

//    public void handleResult(int requestCode, int resultCode, Intent data) {
//        mImageController.handleResult(requestCode, resultCode, data);
//    }

}
