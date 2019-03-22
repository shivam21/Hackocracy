package com.reportmeapp.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.reportmeapp.R;
import com.reportmeapp.preferences.SharedPref;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Created by BHUSRI on 9/8/2017.
 */

public class MySettings extends AppCompatActivity {
    private TextView choosechannels, note;
    private Switch notsound;
    DiscreteSeekBar seekBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mysettings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        choosechannels = (TextView) findViewById(R.id.textView);
        notsound = (Switch) findViewById(R.id.notsound);
        note = (TextView) findViewById(R.id.note);
        seekBar = (DiscreteSeekBar) findViewById(R.id.seekbar);
        final SharedPref pref = new SharedPref(MySettings.this);
        notsound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pref.setSoundStatus(b);
            }
        });
        if (pref.getSoundStatus())
            notsound.setChecked(true);
        seekBar.setProgress(Integer.parseInt(pref.getMiles()));
        note.setText("You will get crime alerts in the range of " + pref.getMiles() + " Miles.");
        seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                pref.setMiles(value);
                note.setText("You will get crime alerts in the range of " + value + " Miles.");
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
        choosechannels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = LayoutInflater.from(MySettings.this).inflate(R.layout.choosechannels, null);
                CheckBox women = v.findViewById(R.id.womenharass);
                CheckBox theft = v.findViewById(R.id.theft);
                CheckBox child = v.findViewById(R.id.child);
                CheckBox corrupt = v.findViewById(R.id.corruption);
                CheckBox other = v.findViewById(R.id.other);
                final SharedPref pref = new SharedPref(MySettings.this);
                if (pref.iswomen())
                    women.setChecked(true);
                if (pref.istheft())
                    theft.setChecked(true);
                if (pref.ischild())
                    child.setChecked(true);
                if (pref.iscorrupt())
                    corrupt.setChecked(true);
                if (pref.isother())
                    other.setChecked(true);
                women.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        pref.setiswomen(b);
                    }
                });
                theft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        pref.setistheft(b);
                    }
                });
                child.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        pref.setischild(b);
                    }
                });
                corrupt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        pref.setiscorrupt(b);
                    }
                });
                other.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        pref.setisother(b);
                    }
                });
                AlertDialog.Builder builder = new AlertDialog.Builder(MySettings.this).setView(v).setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
