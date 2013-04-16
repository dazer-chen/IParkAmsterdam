/*
 * Copyright (C) 2007 The Android Open Source Project
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

package org.bitpipeline.app.iparkamsterdam;

import org.bitpipeline.app.iparkamsterdam.widget.NumberPicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class NumberPickerDialogOld extends AlertDialog implements OnClickListener {
	static final private String VALUE = "value";
	static final private String MIN_VALUE = "min_value";
	static final private String MAX_VALUE = "max_value";

	private final NumberPicker numberPicker;
	private final OnNumberSetListener listener;

	public NumberPickerDialogOld (Context context, OnNumberSetListener callBack, int min, int max, int value) {
		this (context, R.style.Number_Picker_Dialog, callBack, min, max, value);
	}

	public NumberPickerDialogOld (Context context, int theme, OnNumberSetListener callBack, int min, int max, int value) {
		super (context, theme);
		this.listener = callBack;
		setIcon (R.drawable.ic_dialog_time);
		setTitle (R.string.number_picker_dialog_title);

		Context themeContext = getContext ();
		setButton (AlertDialog.BUTTON_POSITIVE, context.getString (R.string.number_picker_dialog_button_set), this); 
		setButton (AlertDialog.BUTTON_NEGATIVE, context.getString (android.R.string.cancel), (OnClickListener) null);

		LayoutInflater inflater = (LayoutInflater) themeContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate (R.layout.number_picker_dialog_old, null);
		setView (view);
		this.numberPicker = (NumberPicker) view.findViewById (R.id.number_picker_dialog_number_picker);
		this.numberPicker.setRange (min, max);
		this.numberPicker.setCurrent (value);
	}

	public void onClick (DialogInterface dialog, int which) {
		if (listener != null) {
			this.numberPicker.clearFocus ();
			this.listener.onNumberSet (null, this.numberPicker.getCurrent ());
		}
	}

	public NumberPickerDialogOld setPickerTitle (int resId) {
		setTitle (resId);
		return this;
	}

	public NumberPickerDialogOld setValue (int value) {
		this.numberPicker.setCurrent (value);
		return this;
	}

	@Override
	public Bundle onSaveInstanceState () {
		Bundle state = super.onSaveInstanceState ();
		state.putInt (NumberPickerDialogOld.MIN_VALUE, this.numberPicker.getBeginRange ());
		state.putInt (NumberPickerDialogOld.MAX_VALUE, this.numberPicker.getEndRange ());
		state.putInt (NumberPickerDialogOld.VALUE, this.numberPicker.getCurrent ());
		return state;
	}

	@Override
	public void onRestoreInstanceState (Bundle savedInstanceState) {
		super.onRestoreInstanceState (savedInstanceState);
		int min = savedInstanceState.getInt (NumberPickerDialogOld.MIN_VALUE);
		int max = savedInstanceState.getInt (NumberPickerDialogOld.MAX_VALUE);
		int value = savedInstanceState.getInt (NumberPickerDialogOld.VALUE);
		this.numberPicker.setRange (min, max);
		this.numberPicker.setCurrent (value);
	}

}
