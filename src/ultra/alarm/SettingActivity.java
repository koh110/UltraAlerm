package ultra.alarm;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * 設定を行うためのactivity
 * 元画面から遷移してくるactivity
 * @author maro
 *
 */
public class SettingActivity extends Activity{
	/**
	 * 使用するメディアプレイヤーを持つviewクラス
	 */
	private static UltraPlayerView uPlayerView;

	/**
	 * 使用する音源のパス
	 */
	private final String path = "/sdcard/music/UltraSoul.mp3";

	/**
	 * 使用する内部音源
	 */
	private Uri uri;

	/**
	 * 停止可能時間
	 */
	private long startTime = (long) (4.3 * 1000);
	private long endTime = (long) (5.0 * 1000);

	@Override
	public void onCreate(Bundle saveInstanceState){
		super.onCreate(saveInstanceState);
		setContentView(R.layout.setting_activity);

		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.setting_linearLayout);

		// プレイヤー用ビューの生成
		uPlayerView = new UltraPlayerView(this);

		// uriの生成
		uri = Uri.parse("android.resource://"+this.getPackageName()+"/"+R.raw.ultra_soul);

		// 音源のセット
		//uPlayerView.setMusic(path);
		uPlayerView.setMusic(uri);
		// 停止可能時間のセット
		uPlayerView.addTimeRange(startTime, endTime);

		linearLayout.addView(uPlayerView);

		// 元画面に戻るボタン
		Button returnBtn = (Button)findViewById(R.id.returnBottun);
		returnBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// この画面のアクティビティを終了する
				finish();
			}
		});
	}

	/**
	 * アプリケーション終了時に呼ばれる処理
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		// プレイヤーの破棄
		uPlayerView.destroy();
		// アクティビティの終了
		this.finish();
	}
}
