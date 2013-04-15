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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

public class NumberPickerDialog extends AlertDialog implements OnClickListener, OnValueChangeListener {
	public interface OnNumberSetListener {
		void onNumberSet (NumberPicker view, int value);
	}

	static final private String VALUE = "value";
	static final private String MIN_VALUE = "min_value";
	static final private String MAX_VALUE = "max_value";

	private final NumberPicker numberPicker;
	private final OnNumberSetListener listener;

	public NumberPickerDialog (Context context, OnNumberSetListener callBack, int min, int max, int value) {
		this (context, 0, callBack, min, max, value);
	}

	public NumberPickerDialog (Context context, int theme, OnNumberSetListener callBack, int min, int max, int value) {
		super (context, theme);
		this.listener = callBack;

		setIcon (0);
		setTitle (R.string.number_picker_dialog_title);

		Context themeContext = getContext ();
		setButton (BUTTON_POSITIVE, themeContext.getText (R.string.number_picker_dialog_button_ok), this);

		LayoutInflater inflater = (LayoutInflater) themeContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate (R.layout.number_picker_dialog, null);
		setView (view);
		this.numberPicker = (NumberPicker) view.findViewById (R.id.number_picker_dialog_number_picker);

		this.numberPicker.setMinValue (min);
		this.numberPicker.setMaxValue (max);
		this.numberPicker.setValue (value);
		this.numberPicker.setOnValueChangedListener (this);
	}

	public void onClick (DialogInterface dialog, int which) {
		if (listener != null) {
			this.numberPicker.clearFocus ();
			this.listener.onNumberSet (this.numberPicker, this.numberPicker.getValue ());
		}
	}

	public NumberPickerDialog setPickerTitle (int resId) {
		setTitle (resId);
		return this;
	}

	public NumberPickerDialog setValue (int value) {
		this.numberPicker.setValue (value);
		return this;
	}

	@Override
	public Bundle onSaveInstanceState () {
		Bundle state = super.onSaveInstanceState ();
		state.putInt (NumberPickerDialog.MIN_VALUE, this.numberPicker.getMinValue ());
		state.putInt (NumberPickerDialog.MAX_VALUE, this.numberPicker.getMaxValue ());
		state.putInt (NumberPickerDialog.VALUE, this.numberPicker.getValue ());
		return state;
	}

	@Override
	public void onRestoreInstanceState (Bundle savedInstanceState) {
		super.onRestoreInstanceState (savedInstanceState);
		int min = savedInstanceState.getInt (NumberPickerDialog.MIN_VALUE);
		int max = savedInstanceState.getInt (NumberPickerDialog.MAX_VALUE);
		int value = savedInstanceState.getInt (NumberPickerDialog.VALUE);
		this.numberPicker.setMinValue (min);
		this.numberPicker.setMaxValue (max);
		this.numberPicker.setValue (value);
	}

	@Override
	public void onValueChange (NumberPicker picker, int oldVal, int newVal) {
		// TODO Auto-generated method stub
		
	}
}
