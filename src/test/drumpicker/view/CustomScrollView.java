package test.drumpicker.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * スクロールイベントが検知できるスクロールビュー
 * @author miurayasushi
 *
 */
public class CustomScrollView extends android.widget.ScrollView {
	private String TAG = CustomScrollView.class.getSimpleName();

	
	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    
    private ScrollViewListener scrollViewListener = null;
    
    public void setOnScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    /**
     * スクロールイベント検知
     */
    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
}
