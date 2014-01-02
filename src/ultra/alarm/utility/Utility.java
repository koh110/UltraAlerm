package ultra.alarm.utility;

/*
 * Copyright 2011 IoriAYANE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import ultra.alarm.R;

public class Utility {
	private static boolean DEBUG = false;

	public static void setDebug(boolean on) {
		DEBUG = on;
	}

	/**
	 * ダイアログの表示
	 *
	 * @param activity
	 * @param title
	 * @param text
	 */
	public static void showDialog(final Activity activity, String title,
			String text) {
		AlertDialog.Builder ad = new AlertDialog.Builder(activity);
		ad.setIcon(R.drawable.ic_menu_more);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.setResult(Activity.RESULT_OK);
			}
		});
		ad.create();
		ad.show();
	}

	/**
	 * トーストを表示する
	 *
	 * @param activity
	 * @param message
	 */
	public static void showToast(final Context context, String message) {
		if (message == null) {
		} else {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * ログを出力する
	 *
	 * @param message
	 */
	public static void outputDebugLog(String message) {
		if (message == null) {
		} else if (!DEBUG) {
		} else {
			Log.d("test", message);
		}
	}

	/**
	 * リストダイアログを表示する
	 *
	 * @param activity
	 * @param title
	 * @param item_list
	 * @param lisner
	 */
	public static void showListDialog(final Activity activity, String title,
			String[] item_list, OnClickListener lisner) {
		activity.setResult(-1);

		AlertDialog.Builder ad = new AlertDialog.Builder(activity);
		ad.setIcon(R.drawable.ic_menu_more);
		ad.setTitle(title);
		ad.setItems(item_list, lisner).show();

	}

	/**
	 * ダイアログの表示
	 *
	 * @param context
	 * @param title
	 * @param text
	 */
	public static void showDialog(final Context context, String title,
			String text, OnClickListener listener) {
		showDialog(context, title, text, listener, null);
	}

	/**
	 * ダイアログの表示
	 *
	 * @param context
	 * @param title
	 *            タイトル
	 * @param text
	 *            本文
	 * @param listener_ok
	 *            OKボタンのリスナ
	 * @param listener_cancel
	 *            キャンセルのリスナ nullの時はキャンセルボタン無し
	 */
	public static void showDialog(final Context context, String title,
			String text, OnClickListener listener_ok,
			OnClickListener listener_cancel) {
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setIcon(R.drawable.ic_menu_more);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setPositiveButton("OK", listener_ok);
		if (listener_cancel != null) {
			ad.setNegativeButton("Cancel", listener_cancel);
		}
		ad.create();
		ad.show();
	}

	/**
	 * ダイアログの表示
	 *
	 * @param context
	 * @param title
	 * @param text
	 */
	public static void showCustumDialog(final Context context, String title,
			String text, View custumItem, OnClickListener listener) {
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setIcon(R.drawable.ic_menu_more);
		ad.setTitle(title);
		if (text != null) {
			ad.setMessage(text);
		}
		ad.setView(custumItem);
		ad.setPositiveButton("OK", listener);
		ad.setNegativeButton("Cancel", null);
		ad.create();
		ad.show();
	}

	/**
	 * SDカードのパスを取得する 使えなければnull 最後に/はない
	 *
	 * @return
	 */
	public static String getSdcardPath() {
		String ret = null;

		String status = Environment.getExternalStorageState();
		File sdcard = Environment.getExternalStorageDirectory();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			// マウントされてない
			// }else if(!sdcard.canWrite()){
			// //使えない
		} else {
			// 使える
			ret = sdcard.toString();
		}

		return ret;
	}

	/**
	 * Wifiがonか調べる
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWifiEnabled(Context context) {
		boolean ret = false;

		if (context == null) {
		} else {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			if (!wifiManager.isWifiEnabled()) {
			} else {
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				int wifiState = wifiManager.getWifiState();

				if (wifiState != WifiManager.WIFI_STATE_ENABLED) {
				} else if (wifiInfo.getSSID() == null) {
				} else if (wifiInfo.getIpAddress() == 0) {
				} else {
					ret = true;
				}
			}
		}

		return ret;
	}

	/**
	 * 一定時間スリープする
	 *
	 * @param time
	 */
	public static void Sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * ファイル→文字列
	 *
	 * @param context
	 *            　コンテキスト（コレがNULLの場合は任意の場所から読める）
	 * @param fileName
	 *            ファイル名
	 * @return 成否
	 */
	public static String file2str(Context context, String fileName) {
		byte[] w = file2data(context, fileName);
		if (w == null) {
			return "";
		} else {
			return new String(w);
		}
	}

	/**
	 * ファイル→バイトデータ
	 *
	 * @param context
	 *            コンテキスト（コレがNULLの場合は任意の場所から読める）
	 * @param fileName
	 *            ファイル名
	 * @return 成否
	 */
	public static byte[] file2data(Context context, String fileName) {
		int size;
		byte[] ret = null;
		byte[] w = new byte[1024];
		InputStream in = null;
		ByteArrayOutputStream out = null;

		try {
			if (context != null) {
				in = context.openFileInput(fileName);
			} else {
				File inFile = new File(fileName);
				in = new FileInputStream(inFile);
			}
			out = new ByteArrayOutputStream();
			while (true) {
				size = in.read(w);
				if (size <= 0) {
					break;
				}
				out.write(w, 0, size);
			}
			in.close();
			out.close();
			ret = out.toByteArray();
		} catch (Exception e) {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e2) {
			}
		}
		return ret;
	}

	/**
	 * 文字列をファイルへ保存する
	 *
	 * @param context
	 *            　コンテキスト（コレがNULLの場合は任意の場所に保存）
	 * @param str
	 * @param fileName
	 *            ファイル名
	 * @return 成否
	 */
	public static boolean str2file(Context context, String str, String fileName) {
		return data2file(context, str.getBytes(), fileName);
	}

	/**
	 * バイトデータをファイルへ保存する
	 *
	 * @param context
	 *            コンテキスト（コレがNULLの場合は任意の場所に保存）
	 * @param data
	 *            内容のバイト列
	 * @param fileName
	 *            ファイル名（コンテキストがNULLの時はフルパス）
	 * @return　成否
	 */
	public static boolean data2file(Context context, byte[] data,
			String fileName) {
		boolean ret = false;
		OutputStream out = null;

		try {
			if (context != null) {
				out = context.openFileOutput(fileName,
						Context.MODE_WORLD_READABLE);
			} else {
				File outFile = new File(fileName);
				out = new FileOutputStream(outFile);
			}
			out.write(data, 0, data.length);
			out.close();
			ret = true;
		} catch (Exception e) {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e2) {

			}
		}
		return ret;
	}

}