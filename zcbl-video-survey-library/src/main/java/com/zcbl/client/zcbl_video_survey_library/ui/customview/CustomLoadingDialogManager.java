package com.zcbl.client.zcbl_video_survey_library.ui.customview;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.zcbl.client.zcbl_video_survey_library.R;


/**
 *created by WangLu on 2017年6月22日 下午1:59:31
 *function ：
**/
public class CustomLoadingDialogManager {
	
	private Context mContext;
	private Dialog dialog;

	public CustomLoadingDialogManager(Context context) {
		this.mContext = context;
	}

	public CustomLoadingDialogManager initDialog(){
		if(null == dialog){
			dialog = new Dialog(mContext, R.style.loading_H5);
			DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
			int widthPixels = displayMetrics.widthPixels;
			dialog.setContentView(R.layout.loading_item);
			View view = LayoutInflater.from(mContext).inflate(R.layout.loading_item, null);
			LinearLayout outerLayout = (LinearLayout)view.findViewById(R.id.loading_item_outerlayout);
			android.view.ViewGroup.LayoutParams layoutParams = outerLayout.getLayoutParams();
			layoutParams.height = widthPixels/4 ;
			layoutParams.width = widthPixels/4 ;
			outerLayout.setLayoutParams(layoutParams);
			dialog.setContentView(view);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
		}
		return this ;
	}
	
	public CustomLoadingDialogManager showDialog(){
		if(null != dialog && !dialog.isShowing()){
			dialog.show();
		}
		return this ;
	}
	
	public CustomLoadingDialogManager dismissDialog(){
		
		if(null != dialog){
			if(dialog.isShowing()){
				dialog.dismiss();
			}
		}
		return this ;
	}
	
	public boolean isShowing(){
		if(null != dialog){
			return dialog.isShowing() ;
		}
		return false ;
	}
	
	public void resetDialog(){
		if(null != dialog){
			dialog = null ;
		}
	}
	
	
	
}
