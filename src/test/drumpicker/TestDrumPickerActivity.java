package test.drumpicker;

import test.drumpicker.view.CustomScrollView;
import test.drumpicker.view.ScrollViewListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TestDrumPickerActivity extends Activity implements ScrollViewListener, OnTouchListener {
	private LinearLayout mLine_0, mLine_1;// ScrollViewのChildView
	private CustomScrollView mScrollView_0, mScrollView_1;// メインのScrollView2個
	private View unit;// これは１つの要素あたりの高さを取得するためのView。
	private String[] row_0 = { "あ", "い", "う", "え", "お", "か", "き", "く", "け", "こ" };// ひだりのドラム要素
	private String[] row_1 = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "JK" };//右のドラム要素
	private int unitHeight;// 要素の単位高さ
	private int to_0, to_1;// スクロールの行き先Y座標
	private final int REPEAT_INTERVAL = 10;// スクロール間隔の時間(ミリ秒)
	private final int MESSAGE_WHAT = 100;// この数字は何でもOK
	private boolean isRepeat_0, isRepeat_1;// 移動中ですを示すフラグ。２つのドラムを何か依存させるときとかにも使う
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mLine_0 = (LinearLayout) findViewById(R.id.main_line_0);
		mLine_1 = (LinearLayout) findViewById(R.id.main_line_1);
		mScrollView_0 = (CustomScrollView) findViewById(R.id.custom_scrollview_0);
		mScrollView_1 = (CustomScrollView) findViewById(R.id.custom_scrollview_1);
		setLine();
		mScrollView_0.setOnTouchListener(this);
		mScrollView_1.setOnTouchListener(this);
		mScrollView_0.setOnScrollViewListener(this);
		mScrollView_1.setOnScrollViewListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// もし自動で移動しているときに触ったらその瞬間ストップ。
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (v.getId() == mScrollView_0.getId())
				isRepeat_0 = false;
			else if (v.getId() == mScrollView_1.getId())
				isRepeat_1 = false;
		}
		return false;
	}

	@Override
	public void onScrollChanged(CustomScrollView scrollView, int x, int y, int oldx, int oldy) {
		// ScrollViewの座標が変わったらここが反応します。この場所にupdateText()を入れておけばスマート
		updateText();
		Log.d("onScrollChanged", "isRepeat_0:" + isRepeat_0 + " isRepeat_1:" + isRepeat_1);
		int currentY = 0;
		int mode = 0;// 0で左1で右を表現。。。でも、昔のコードで何を意図していたのか謎です。
		if (scrollView.getId() == mScrollView_0.getId())
			currentY = mScrollView_0.getScrollY();
		else if (scrollView.getId() == mScrollView_1.getId()) {
			currentY = mScrollView_1.getScrollY();
			mode = 1;
		}
		final int final_mode = mode;
		final int final_currentY = currentY;
		final CustomScrollView final_scrollView = scrollView;
		Log.d("onScrollChanged", "mode:" + mode);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (((final_mode == 0 && !isRepeat_0) || (final_mode == 1 && !isRepeat_1)) && final_scrollView.getScrollY() == final_currentY
						&& final_currentY % unitHeight != 0)
					moveScrollFirst(final_mode);
			}
		}, 100);
	}

	/**
	 * 動き出しはじめを決めるメソッド。一番近い要素を決めてそこに向かって進み始めます。
	 * @param mode
	 * @param currentY
	 */
	protected void moveScrollFirst(final int mode) {
		Message message = new Message();
		message.what = MESSAGE_WHAT;
		if (mode == 0) {// ひだり
			isRepeat_0 = true;
			to_0 = getToPosition(row_0.length, mScrollView_0.getScrollY());
			handler_0.sendMessageDelayed(message, REPEAT_INTERVAL);
		} else {// みぎ
			isRepeat_1 = true;
			to_1 = getToPosition(row_1.length, mScrollView_1.getScrollY());
			handler_1.sendMessageDelayed(message, REPEAT_INTERVAL);
		}
	}

	// 繰り返しハンドラー
	private Handler handler_0 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 2.isRepeatがtrueなら処理を繰り返す
			if (isRepeat_0) {// 3.繰り返し処理
				moveScroll(0);
				// 4.次回処理をセット
				handler_0.sendMessageDelayed(obtainMessage(), REPEAT_INTERVAL);
			}
		}
	};

	// 繰り返しハンドラー右用
	private Handler handler_1 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 2.isRepeatがtrueなら処理を繰り返す
			if (isRepeat_1) {// 3.繰り返し処理
				moveScroll(1);
				// 4.次回処理をセット
				handler_1.sendMessageDelayed(obtainMessage(), REPEAT_INTERVAL);
			}
		}
	};

	/**
	 * 移動量を決めるメソッド
	 * @param mode
	 */
	protected void moveScroll(final int mode) {
		CustomScrollView sv = (mode == 0 ? mScrollView_0 : mScrollView_1);
		int currentY = sv.getScrollY();
		int toY = (mode == 0 ? to_0 : to_1);
		if (toY == currentY) {
			if (mode == 0)
				isRepeat_0 = false;
			if (mode == 1)
				isRepeat_1 = false;
		} else {
			int abs = Math.abs(currentY - toY);
			int deltaY = 1;// ここを1以外にすると振動子になる可能性があります。
			if (abs > 100)
				deltaY = 20;
			else if (abs > 50)
				deltaY = 10;
			else if (abs > 10)
				deltaY = 5;
			if (currentY < toY)
				currentY += deltaY;
			else
				currentY -= deltaY;
			sv.scrollTo(0, currentY);
		}
	}

	//テキストの更新
	private void updateText() {
		//ひだり
		((TextView)findViewById(R.id.left_textview)).setText("ひだり:"+row_0[mScrollView_0.getScrollY() / unitHeight]);
		//みぎ
		((TextView)findViewById(R.id.right_textview)).setText("みぎ:"+row_1[mScrollView_1.getScrollY() / unitHeight]);
	}

	
	//行き先要素番号の決定
	private int getToPosition(int max, int currentY) {
		int index = 0;
		for (int i = 0; i < max; i++) {
			if (currentY < unitHeight * i + unitHeight / 2) {
				index = i;
				break;
			}
		}
		return unitHeight * index;
	}

	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {// WindowFocusChangedのタイミングで単に高さを取得しましょう。onCreate()ではとれません。
		super.onWindowFocusChanged(hasFocus);
		if (unitHeight == 0)
			unitHeight = unit.getHeight();
		updateText();
	}

	/**
	 * ドラム作成
	 */
	private void setLine() {
		ViewGroup viewGroup = null;
		String[] element;
		for (int i = 0; i < 2; i++) {// みぎとひだりなので２回まわします
			if (i == 0) {
				viewGroup = mLine_0;
				element = row_0;
			} else {
				viewGroup = mLine_1;
				element = row_1;
			}
			for (int j = 0; j < element.length; j++) {
				View view = getLayoutInflater().inflate(R.layout.listview_row, null);
				((TextView) view.findViewById(R.id.textview)).setText(element[j]);
				viewGroup.addView(view);
			}
			//ドラム要素0番目の上と最終要素の下にスキマを作る。今回は常に見える要素数が５なので２個ずつ。７個の場合は３個ずつですよ。
			unit = getLayoutInflater().inflate(R.layout.listview_row, null);
			View view_0 = getLayoutInflater().inflate(R.layout.listview_row, null);
			View view_1 = getLayoutInflater().inflate(R.layout.listview_row, null);
			View view_2 = getLayoutInflater().inflate(R.layout.listview_row, null);
			viewGroup.addView(unit, 0);
			viewGroup.addView(view_0, 0);
			viewGroup.addView(view_1, viewGroup.getChildCount());
			viewGroup.addView(view_2, viewGroup.getChildCount());
		}
	}
}