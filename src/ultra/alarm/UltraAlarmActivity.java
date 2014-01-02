package ultra.alarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * UltraAlarmアプリケーションクラス touchイベントによりプレイヤーを止める機能を持つ
 *
 * @author maro
 *
 */
public class UltraAlarmActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 設定画面に移動するボタン
		Button moveSettingBtn = (Button)findViewById(R.id.moveSettingBottun);
		moveSettingBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// インテントのインスタンス生成
				Intent intent = new Intent(UltraAlarmActivity.this,
						SettingActivity.class);
				// 遷移先のアクティビティを起動
				startActivity(intent);
			}
		});
	}
}