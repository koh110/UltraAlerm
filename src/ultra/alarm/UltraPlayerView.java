package ultra.alarm;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * プレイヤーのView管理クラス
 *
 * @author maro
 *
 */
public class UltraPlayerView extends TableLayout {
	/**
	 * ActityのContext
	 */
	private Context context;

	/**
	 * 使用するプレイヤー
	 */
	private UltraPlayer player;

	/**
	 * 再生時間取得用ハンドラー
	 */
	private Handler handler;

	/**
	 * ハンドラーを制御するスレッド
	 */
	private ScheduledExecutorService srv;

	/**
	 * ハンドラー制御スレッドが動く時間(ミリ秒) 0.1秒間隔で取得
	 */
	private final int THREAD_TIME_RANGE = 100;

	/**
	 * stopボタンを押した回数を表示するview
	 */
	private TextView g_stopNum;

	/**
	 * 現在時間の表示用テキストエリア
	 */
	private EditText g_crtPos;
	/**
	 * スタート時間の設定用テキストエリア
	 */
	private EditText g_startPos;
	/**
	 * エンド時間設定用テキストエリア
	 */
	private EditText g_endPos;

	/**
	 * 停止可能時間を表示するビュー
	 */
	private TableLayout timeRangeView;

	/**
	 * 再生箇所をいじるシークバー
	 */
	private SeekBar g_seekbar;

	/**
	 * 初期化
	 *
	 * @param context
	 */
	public UltraPlayerView(Context context) {
		super(context);
		// Contextのインスタンス化
		this.context = context;

		// ハンドラーの初期化
		handler = new Handler();

		// プレイヤーの初期化
		player = new UltraPlayer(context);

		// 停止可能時間表示ビューの初期化
		timeRangeView = new TableLayout(context);

		// レイアウトの設定
		layout();

		// 定期的に起動するスレッドの生成
		createScheduledExecutor();
	}

	/**
	 * 定期的に起動するスケジューラの生成
	 */
	private void createScheduledExecutor() {
		srv = Executors.newSingleThreadScheduledExecutor();
		// 現在再生時間を取得して、各GUIを変更する
		srv.scheduleAtFixedRate(new Runnable() {
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// 現在時間
						int currentPosition = player.getCurrentPosition();
						// シークバーの最大値をプレイヤーにセットされている曲のサイズにする
						g_seekbar.setMax(player.getDuration());
						// 現在位置を表示するテキストエリアに値をセット
						g_crtPos.setText(String.valueOf(currentPosition));
						// シークバーをセット
						g_seekbar.setProgress(currentPosition);
						// stopボタンを押した回数をviewにセット
						g_stopNum.setText("停止ボタンを押した回数:"+player.getStopCounter());
					}
				});
			}
		}, 0, THREAD_TIME_RANGE, TimeUnit.MILLISECONDS);
	}

	/**
	 * pathの音楽をsetする
	 *
	 * @param path
	 */
	public void setMusic(String path) {
		player.setMusic(path);
	}

	/**
	 * uriを指定して音楽をsetする
	 *
	 * @param uri
	 */
	public void setMusic(Uri uri) {
		player.setMusic(context, uri);
	}

	/**
	 * プレイヤーの停止可能時間の設定
	 *
	 * @param startTime
	 * @param endTime
	 */
	public void addTimeRange(long startTime, long endTime) {
		// 停止可能時間の設定
		player.addTimeRange(startTime, endTime);
		// 停止可能時間表示の修正
		createTimeRangeTableRow();
	}

	/**
	 * 再生するパスの設定
	 *
	 * @param path
	 */
	public void setMusicPath(String path) {
		player.setMusic(path);
	}

	/**
	 * onDestroyメソッドで呼ぶ プレイヤーを破棄する
	 */
	public void destroy() {
		// スレッドの破棄
		// srv = null;
		srv.shutdown();
		// プレイヤーを終了させる
		player.destroy();
		// すべてのviewを破棄する
		this.removeAllViews();
	}

	/**
	 * viewのレイアウト
	 */
	private void layout() {
		// スクロール表示のためのView
		ScrollView scrollView = new ScrollView(context);
		// 縦方向に表示するためのlinearLayout
		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL); // 縦方向に追加

		// 加えるViewを一時的に入れる変数
		View addView;
		// 0行目の設定============================================================
		// 再生・停止ボタン行を追加
		addView = createColumnStartStopButton();
		linearLayout.addView(addView); //
		// 1行目の設定============================================================
		// 強制停止・0秒にシークする行の追加
		addView = createAbsStopSeekZeroButtonColumn();
		linearLayout.addView(addView); //
		// 2行目の設定============================================================
		// seekbarのset
		addView = createSeekBar();
		linearLayout.addView(addView);
		// 3行目の設定============================================================
		// 停止可能時間を設定するテキストエリア行の追加
		addView = createTimeRangeColumn();
		linearLayout.addView(addView);
		// 4行目の設定============================================================
		// 停止可能時間のビューを追加
		addView = timeRangeView;
		linearLayout.addView(addView);

		// スクロール表示用viewに追加
		scrollView.addView(linearLayout);

		// 自分自身にレイアウトを登録
		this.addView(scrollView);
	}

	/**
	 * TimeRangeの表示を行う行を生成する
	 *
	 * @param tRow
	 */
	private View createTimeRangeColumn() {
		TableLayout tlayout = new TableLayout(context);

		// 0行目==============================================================
		TableRow tr = new TableRow(context);
		// 説明文
		TextView crt = new TextView(context);
		crt.setText("現在再生時間");
		tr.addView(crt);
		TextView start = new TextView(context);
		start.setText("停止可能開始時間");
		tr.addView(start);
		TextView end = new TextView(context);
		end.setText("停止可能終了時間");
		tr.addView(end);
		// テーブルに追加
		tlayout.addView(tr);
		// 1行目==============================================================
		tr = new TableRow(context);
		// 現在時間の表示用テキストエリア
		g_crtPos = new EditText(context);
		// 行1に現在時間表示用テキストエリアを追加
		tr.addView(g_crtPos);
		// スタート時間の設定用テキストエリア
		g_startPos = new EditText(context);
		// テキストエリアの入力制限を数値テキストに設定
		g_startPos.setInputType(InputType.TYPE_CLASS_NUMBER);
		// テキスト内容の設定
		g_startPos.setText("0");
		// 行1にスタート時間の設定用テキストエリアを追加
		tr.addView(g_startPos);
		// 終了時間の設定用テキストエリア
		g_endPos = new EditText(context);
		// テキストエリアの入力制限を数値テキストに設定
		g_endPos.setInputType(InputType.TYPE_CLASS_NUMBER);
		// テキスト内容の設定
		g_endPos.setText("0");
		// 行1に終了時間の設定用テキストエリアを追加
		tr.addView(g_endPos);

		// テーブルに追加
		tlayout.addView(tr);
		// 2行目==============================================================
		tr = new TableRow(context);
		// レイアウト用インスタンスの生成
		TableRow.LayoutParams tlp = new TableRow.LayoutParams();
		// 3列結合
		tlp.span = 3;
		// ボタンの生成
		Button addTimeRangeButton = createTimeRangeButton();
		tr.addView(addTimeRangeButton, tlp);

		// テーブルに追加
		tlayout.addView(tr);
		return tlayout;
	}

	/**
	 * 強制停止ボタンとシークを0に移動するボタンを生成する
	 *
	 * @param tRow
	 */
	private View createAbsStopSeekZeroButtonColumn() {
		LinearLayout llayout = new LinearLayout(context);
		// 強制停止ボタンの生成
		Button absoluteStopBtn = createAbsoluteStopButton();
		// 行に強制停止ボタンを追加
		llayout.addView(absoluteStopBtn);

		// 再生時間を0へ移動するボタンの生成
		Button seekToZeroButton = createSeekToZeroButton();
		// 行にボタンを追加
		llayout.addView(seekToZeroButton);

		return llayout;
	}

	/**
	 * シークバーを生成する
	 *
	 * @return
	 */
	private View createSeekBar() {
		// 再生箇所を変更するためのシークバー
		g_seekbar = new SeekBar(context);
		g_seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					player.seekTo(progress);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO 自動生成されたメソッド・スタブ

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});
		return g_seekbar;
	}

	/**
	 * スタート・ストップボタンの行のViewを生成する
	 *
	 * @param tRow
	 */
	private View createColumnStartStopButton() {
		LinearLayout llayout = new LinearLayout(context);
		// 再生ボタンの生成
		Button startBtn = createStartButton();
		// 行に再生ボタンを追加
		llayout.addView(startBtn);

		// 停止ボタンの生成
		Button stopBtn = createStopButton();
		// 行に停止ボタンを追加
		llayout.addView(stopBtn);

		// stopボタンを押した回数を表示するviewを初期化
		g_stopNum = new TextView(context);
		// 行に停止ボタンを追加
		llayout.addView(g_stopNum);

		return llayout;
	}

	/**
	 * 停止可能時間の表示を動的に変化させる
	 *
	 * @param timeRangeList
	 * @param tRowList
	 */
	private void createTimeRangeTableRow() {
		// 停止可能時間の取得
		final ArrayList<UltraPlayer.TimePair> timeRangeList = player
				.getTimeRange();
		Log.v("size", String.valueOf(timeRangeList.size()));
		// 行の生成
		TableRow tRow = new TableRow(context);
		// 停止可能時間の表示
		for (int i = 0; i < timeRangeList.size(); i++) {
			tRow = new TableRow(context);
			// 停止可能時間表示用のテキストビュー
			TextView text = new TextView(context);
			// テキストのセット
			text.setText(timeRangeList.get(i).toString());

			tRow.addView(text);

			// ボタンの生成
			Button removeBtn = new Button(context);

			// テキストのセット
			removeBtn.setText("削除");

			final int index = i;
			final TableRow f_tRow = tRow;
			// ボタンのイベントリスナーを設定
			removeBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// プレイヤーのタイムレンジを削除
					player.removeTimeRange(index);

					// 行の削除
					timeRangeView.removeView(f_tRow);
					f_tRow.removeAllViews();
					Log.v("size", String.valueOf(timeRangeList.size()));
					Log.v("tnum", String.valueOf(timeRangeView.getChildCount()));
				}
			});
			tRow.addView(removeBtn);
		}

		// タイムレンジ用レイアウトに追加
		timeRangeView.addView(tRow);

		// 再描画
		timeRangeView.invalidate();
	}

	/**
	 * 再生ボタンを生成する
	 *
	 * @return プレイヤー再生機能を持ったボタン
	 */
	private Button createStartButton() {
		// ボタンの生成
		Button startBtn = new Button(context);

		// テキストのセット
		startBtn.setText("再生");

		// ボタンのイベントリスナーを設定
		startBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// プレイヤーを再生
				player.start();
			}
		});

		return startBtn;
	}

	/**
	 * 停止機能を持つボタンを生成する
	 *
	 * @return 停止機能を持つボタン
	 */
	private Button createStopButton() {
		// ボタンの生成
		Button button = new Button(context);

		// テキストのセット
		button.setText("停止");

		// ボタンのイベントリスナーを設定
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// プレイヤーを再生
				player.stopAction();
			}
		});

		return button;
	}

	/**
	 * 強制停止ボタンを生成する
	 *
	 * @return 強制停止機能を持つボタン
	 */
	private Button createAbsoluteStopButton() {
		// ボタンの生成
		Button button = new Button(context);

		// テキストのセット
		button.setText("強制停止");

		// ボタンのイベントリスナーを設定
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// プレイヤーを停止
				player.absolutePause();
			}
		});

		return button;
	}

	/**
	 * 再生位置を0にするボタンの生成
	 *
	 * @return
	 */
	private Button createSeekToZeroButton() {
		// ボタンの生成
		Button button = new Button(context);

		// テキストのセット
		button.setText("seek to 0");

		// ボタンのイベントリスナーを設定
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// プレイヤーの再生位置を0に
				player.seekTo(0);
			}
		});

		return button;
	}

	/**
	 * 停止可能時間の設定を追加するボタンの生成
	 *
	 * @return
	 */
	private Button createTimeRangeButton() {
		// ボタンの生成
		Button button = new Button(context);

		// テキストのセット
		button.setText("追加");

		// ボタンのイベントリスナーを設定
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// EditText変換用インスタンス
				SpannableStringBuilder sb = (SpannableStringBuilder) g_startPos
						.getText();
				// 停止可能時間の取得
				final long startTime = (long) Integer.valueOf(sb.toString());
				sb = (SpannableStringBuilder) g_endPos.getText();
				// 停止可能時間の取得
				final long endTime = Long.valueOf(sb.toString());
				// プレイヤーに停止可能時間を追加
				player.addTimeRange(startTime, endTime);

				// 停止可能時間表示の修正
				createTimeRangeTableRow();
				Log.v("tnum", String.valueOf(timeRangeView.getChildCount()));
			}
		});

		return button;
	}
}
