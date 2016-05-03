package com.centerm.lklcpos.settings.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.centerm.lklcpos.activity.BaseActivity;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-8-28
 *
 */
public class TransSwitchSetting extends BaseActivity {

	private CheckBox consumeCheckBox;
	private CheckBox undoCheckBox;
	private CheckBox consumeSearchCheckBox;
	private CheckBox recedeCheckBox;
	private CheckBox preWarrantCheckBox;
	private CheckBox preWarrantUndoCheckBox;
	private CheckBox preWarrantOverCheckBox;
	private CheckBox preWarrantOverUndoCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_settings_layout);
		inititle();

		consumeCheckBox = (CheckBox) this.findViewById(R.id.consume_checkbox);
		undoCheckBox = (CheckBox) this.findViewById(R.id.undo_checkbox);
		consumeSearchCheckBox = (CheckBox) this.findViewById(R.id.consume_search_checkbox);
		recedeCheckBox = (CheckBox) this.findViewById(R.id.recede_checkbox);
		preWarrantCheckBox = (CheckBox) this.findViewById(R.id.pre_warrant_checkbox);
		preWarrantUndoCheckBox = (CheckBox) this.findViewById(R.id.pre_warrant_undo_checkbox);
		preWarrantOverCheckBox = (CheckBox) this.findViewById(R.id.pre_warrant_over_checkbox);
		preWarrantOverUndoCheckBox = (CheckBox) this.findViewById(R.id.pre_warrant_over_undo_checkbox);

		consumeCheckBox.setOnCheckedChangeListener(mChangeListener);
		undoCheckBox.setOnCheckedChangeListener(mChangeListener);
		consumeSearchCheckBox.setOnCheckedChangeListener(mChangeListener);
		recedeCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantUndoCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantOverCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantOverUndoCheckBox.setOnCheckedChangeListener(mChangeListener);

		SharedPreferences mSP = this.getSharedPreferences("menu_settings", 0);
		consumeCheckBox.setChecked(mSP.getBoolean("consume", true));
		undoCheckBox.setChecked(mSP.getBoolean("undo", true));
		consumeSearchCheckBox.setChecked(mSP.getBoolean("consumeSearch", true));
		recedeCheckBox.setChecked(mSP.getBoolean("recede", true));
		preWarrantCheckBox.setChecked(mSP.getBoolean("preWarrant", true));
		preWarrantUndoCheckBox.setChecked(mSP.getBoolean("preWarrantUndo", true));
		preWarrantOverCheckBox.setChecked(mSP.getBoolean("preWarrantOver", true));
		preWarrantOverUndoCheckBox.setChecked(mSP.getBoolean("preWarrantOverUndo", true));

	}

	private CompoundButton.OnCheckedChangeListener mChangeListener = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			SharedPreferences mSharedPreferences = TransSwitchSetting.this.getSharedPreferences("menu_settings", 0);
			SharedPreferences.Editor mEditor = mSharedPreferences.edit();
			int id = buttonView.getId();
			switch (id) {
			case R.id.consume_checkbox:
				mEditor.putBoolean("consume", consumeCheckBox.isChecked());
				break;
			case R.id.undo_checkbox:
				mEditor.putBoolean("undo", undoCheckBox.isChecked());
				break;
			case R.id.consume_search_checkbox:
				mEditor.putBoolean("consumeSearch", consumeSearchCheckBox.isChecked());
				break;
			case R.id.recede_checkbox:
				mEditor.putBoolean("recede", recedeCheckBox.isChecked());
				break;
			case R.id.pre_warrant_checkbox:
				mEditor.putBoolean("preWarrant", preWarrantCheckBox.isChecked());
				break;
			case R.id.pre_warrant_undo_checkbox:
				mEditor.putBoolean("preWarrantUndo", preWarrantUndoCheckBox.isChecked());
				break;
			case R.id.pre_warrant_over_checkbox:
				mEditor.putBoolean("preWarrantOver", preWarrantOverCheckBox.isChecked());
				break;
			case R.id.pre_warrant_over_undo_checkbox:
				mEditor.putBoolean("preWarrantOverUndo", preWarrantOverUndoCheckBox.isChecked());
				break;
			default:
				break;
			}
			mEditor.commit();
		}

	};
}
