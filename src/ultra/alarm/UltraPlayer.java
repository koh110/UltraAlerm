package ultra.alarm;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.Log;

/**
 * ウルトラ目覚まし用プレイヤー 特定のタイミングで停止ボタンを押さないと停止しない音楽プレイヤー
 *
 * @author maro
 *
 */
public class UltraPlayer implements OnCompletionListener {
	/**
	 * 呼び出されるActivityのコンテキスト
	 */
	private Context context;
	/**
	 * 音楽再生用メディアプレイヤー
	 */
	private MediaPlayer mediaPlayer;
	/**
	 * メディアプレイヤーの再生音源の場所
	 */
	private String musicPath;

	/**
	 * 1ループの間に停止ボタンを押せる回数
	 */
	private int canStopNum;

	/**
	 * ストップボタンを押した回数
	 */
	private int stopCounter;

	/**
	 * プレイヤーを停止出来るタイミングのリスト
	 */
	private ArrayList<TimePair> timeRanges;

	/**
	 * コンストラクタ
	 */
	public UltraPlayer(Context context) {
		// メディアプレイヤーの生成
		mediaPlayer = new MediaPlayer();
		// ストリームタイプの設定
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // 音楽の再生モード
		// ループ再生を行う
		//mediaPlayer.setLooping(true);
		// pathの生成
		musicPath = new String();
		// 停止可能回数の初期化
		canStopNum = 1;
		// timeRangesの生成
		timeRanges = new ArrayList<TimePair>();
		// リスナーのセット
		mediaPlayer.setOnCompletionListener(this);
	}

	public UltraPlayer(Context context, String path) {
		this(context);
		// pathをメディアプレイヤーにset
		this.setMusic(path);
	}

	/**
	 * 再生開始
	 */
	public void start() {
		// 再生開始
		mediaPlayer.start();
	}

	/**
	 * 強制停止
	 */
	public void absolutePause() {
		if (mediaPlayer.isPlaying()) {
			// プレイヤーを停止させる
			mediaPlayer.pause();
		}
	}

	/**
	 * プレイヤーをシークさせる(ミリ秒)
	 *
	 * @param msec
	 */
	public void seekTo(int msec) {
		// 停止回数を初期化する
		stopCounter = 0;
		// シークさせる
		mediaPlayer.seekTo(msec);
	}

	/**
	 * メディアプレイヤーの停止動作のタイミングに呼ぶメソッド
	 */
	public void stopAction() {
		// stopボタンを押した回数を増やす
		stopCounter++;
		// stopボタンを押した数が停止可能回数を上回った時
		if (stopCounter > canStopNum) {
			return;	// 処理を行わない
		}
		// すべての範囲リストを調べる
		for (TimePair range : timeRanges) {
			// 範囲内に収まっていれば
			if (range.isInRange(this.mediaPlayer.getCurrentPosition())) {
				// プレイヤーの停止
				mediaPlayer.pause();
				break;
			}
		}
	}

	/**
	 * 引数のpathにある音楽をメディアプレイヤーにセットする
	 *
	 * @param path
	 */
	public void setMusic(String path) {
		try {
			// メディアプレイヤーに音楽をセット
			mediaPlayer.setDataSource(path);
			// 再生準備
			prepare();
		} catch (Exception e) {
			// エラー内容のダイアログを表示
			new AlertDialog.Builder(context).setTitle("error")
					.setMessage(e.getMessage()).setPositiveButton("OK", null)
					.show();
		}
	}

	/**
	 * uriを指定して音楽をセットする
	 *
	 * @param context
	 * @param uri
	 */
	public void setMusic(Context context, Uri uri) {
		try {
			// メディアプレイヤーに音楽をセット
			mediaPlayer.setDataSource(context, uri);
			// 再生準備
			prepare();
		} catch (Exception e) {
			// エラー内容のダイアログを表示
			new AlertDialog.Builder(context).setTitle("error")
					.setMessage(e.getMessage()).setPositiveButton("OK", null)
					.show();
		}
	}

	/**
	 * アクティビティのonDestroyで呼ぶメソッド
	 */
	public void destroy() {
		// 再生している状態であれば停止する
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		// リソースの解放
		mediaPlayer.release();
		mediaPlayer = null;
	}

	/**
	 * プレイヤーの停止出来るタイミングを加える
	 *
	 * @param startTime
	 *            サウンド開始から停止タイミングが開始するまでの時間(ミリ秒)
	 * @param endTime
	 *            サウンド開始から停止タイミングが終了するまでの時間(ミリ秒)
	 */
	public void addTimeRange(long startTime, long endTime) {
		TimePair timePair = new TimePair(startTime, endTime);
		Log.v("addTP",
				"(" + String.valueOf(timePair.start) + ","
						+ String.valueOf(timePair.end) + ")");
		timeRanges.add(timePair);
	}

	/**
	 * 停止可能時間の削除
	 *
	 * @param index
	 */
	public void removeTimeRange(int index) {
		timeRanges.remove(index);
	}

	/**
	 * 再生準備を行うメソッド
	 */
	private void prepare() {
		try {
			// 再生準備
			mediaPlayer.prepare();
		} catch (Exception e) {
			// エラー内容のダイアログを表示
			new AlertDialog.Builder(context).setTitle("error")
					.setMessage("再生準備の失敗").setPositiveButton("OK", null).show();
		}
	}

	// setter,getter====================================================================
	public void setCanStopNum(int num) {
		if (num < 1) {	// 停止可能回数が規定範囲外の場合
			this.canStopNum = 1;
		}else{	// 通常の場合
			this.canStopNum = num;
		}
	}

	public int getCanStopNum() {
		return this.canStopNum;
	}

	public int getStopCounter(){
		return this.stopCounter;
	}

	/**
	 * 停止可能時間のgetter
	 *
	 * @return
	 */
	public ArrayList<TimePair> getTimeRange() {
		return timeRanges;
	}

	/**
	 * パスのリストを取得する
	 *
	 * @return pathのリスト
	 */
	public String getMusicPath() {
		return musicPath;
	}

	/**
	 * 再生地点からの時間(ミリ秒)を取得
	 *
	 * @return 再生地点からの時間(ミリ秒)
	 */
	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	/**
	 * 再生時間(ミリ秒)の長さを返す
	 *
	 * @return
	 */
	public int getDuration() {
		return mediaPlayer.getDuration();
	}

	/**
	 * start timeとend timeを管理するinnner class
	 *
	 * @author maro
	 *
	 */
	public class TimePair {
		/**
		 * 開始時間
		 */
		private long start;
		/**
		 * 終了時間
		 */
		private long end;

		/**
		 * コンストラクタ
		 *
		 * @param startTime
		 *            開始時間
		 * @param endTime
		 *            終了時間
		 */
		public TimePair(long startTime, long endTime) {
			if (startTime < endTime) {
				this.start = startTime;
				this.end = endTime;
			} else {
				this.start = endTime;
				this.end = startTime;
			}
		}

		/**
		 * 引数の時間が範囲内に存在すればtrueを返す
		 *
		 * @param time
		 * @return timeが範囲内に存在する場合true。それ以外の場合false。
		 */
		boolean isInRange(long time) {
			if (start <= time && time <= end) {
				return true;
			}
			return false;
		}

		// getter=======================================================
		public long getStartTime() {
			return start;
		}

		public long getEndTime() {
			return end;
		}

		public String toString() {
			return "(" + String.valueOf(start) + "," + String.valueOf(end)
					+ ")";
		}
	}

	/**
	 * メディアファイルの再生が終った時のイベント
	 */
	public void onCompletion(MediaPlayer mediaplayer) {
		// スタート地点に戻る
		mediaPlayer.seekTo(0);

		// 再スタート
		mediaPlayer.start();

		stopCounter = 0;
	}
}
