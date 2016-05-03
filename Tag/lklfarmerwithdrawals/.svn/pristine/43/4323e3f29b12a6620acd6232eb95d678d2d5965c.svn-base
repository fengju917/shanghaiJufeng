package com.centerm.lklcpos.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.lkl.farmerwithdrawals.R;

/*
 * 联机交易可配置界面
 */
public class MenuFragment extends Fragment {
	private CheckBox consumeCheckBox;
	private CheckBox undoCheckBox;
	private CheckBox consumeSearchCheckBox;
	private CheckBox recedeCheckBox;
	private CheckBox preWarrantCheckBox;
	private CheckBox preWarrantUndoCheckBox;
	private CheckBox preWarrantOverCheckBox;
	private CheckBox preWarrantOverUndoCheckBox;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.menu_settings_layout, container, false);

		consumeCheckBox = (CheckBox) view.findViewById(R.id.consume_checkbox);
		undoCheckBox = (CheckBox) view.findViewById(R.id.undo_checkbox);
		consumeSearchCheckBox = (CheckBox) view.findViewById(R.id.consume_search_checkbox);
		recedeCheckBox = (CheckBox) view.findViewById(R.id.recede_checkbox);
		preWarrantCheckBox = (CheckBox) view.findViewById(R.id.pre_warrant_checkbox);
		preWarrantUndoCheckBox = (CheckBox) view.findViewById(R.id.pre_warrant_undo_checkbox);
		preWarrantOverCheckBox = (CheckBox) view.findViewById(R.id.pre_warrant_over_checkbox);
		preWarrantOverUndoCheckBox = (CheckBox) view.findViewById(R.id.pre_warrant_over_undo_checkbox);

		consumeCheckBox.setOnCheckedChangeListener(mChangeListener);
		undoCheckBox.setOnCheckedChangeListener(mChangeListener);
		consumeSearchCheckBox.setOnCheckedChangeListener(mChangeListener);
		recedeCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantUndoCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantOverCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantOverUndoCheckBox.setOnCheckedChangeListener(mChangeListener);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		SharedPreferences mSP = getActivity().getSharedPreferences("menu_settings", 0);
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
			SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("menu_settings", 0);
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

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}
