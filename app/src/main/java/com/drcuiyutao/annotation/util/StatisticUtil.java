package com.drcuiyutao.annotation.util;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;

import com.drcuiyutao.lib.annotation.Insert;

/**
 * @author DCH <a href="mailto:chuanhao.dai@drcuiyutao.com">Contract me.</a>
 * @since 2020/4/6
 */
public class StatisticUtil {

    public static final String TAG = "StatisticUtil";

    @Insert(target = View.OnClickListener.class)
    public static void onClick(View v) {
        Log.i(TAG, "onClick v[" + v + "]");
    }

    @Insert(target = DialogInterface.OnClickListener.class)
    public static void onClick(DialogInterface dialog, int which) {
        Log.i(TAG, "onClick dialog[" + dialog + "] which[" + which + "]");
    }

    @Insert(target = AdapterView.OnItemClickListener.class)
    public static void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemClick parent[" + parent + "] view[" + view + "] position[" + position + "] id[" + id + "]");
    }

    @Insert(target = AdapterView.OnItemSelectedListener.class)
    public static void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemSelected parent[" + parent + "] view[" + view + "] position[" + position + "] id[" + id + "]");
    }

    @Insert(target = ExpandableListView.OnGroupClickListener.class)
    public static boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        Log.i(TAG, "onGroupClick parent[" + parent + "] v[" + v + "] groupPosition[" + groupPosition + "] id[" + id + "]");
        return false;
    }

    @Insert(target = ExpandableListView.OnChildClickListener.class)
    public static boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Log.i(TAG, "onChildClick parent[" + parent + "] v[" + v + "] groupPosition[" + groupPosition + "] childPosition[" + childPosition + "] id[" + id + "]");
        return false;
    }

    @Insert(target = RatingBar.OnRatingBarChangeListener.class)
    public static void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        Log.i(TAG, "onRatingChanged ratingBar[" + ratingBar + "] rating[" + rating + "] fromUser[" + fromUser + "]");
    }

    @Insert(target = SeekBar.OnSeekBarChangeListener.class)
    public static void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "onStopTrackingTouch seekBar[" + seekBar + "]");
    }

    @Insert(target = CompoundButton.OnCheckedChangeListener.class)
    public static void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "onCheckedChanged buttonView[" + buttonView + "] isChecked[" + isChecked + "]");
    }

    @Insert(target = RadioGroup.OnCheckedChangeListener.class)
    public static void onCheckedChanged(RadioGroup group, int checkedId) {
        Log.i(TAG, "onCheckedChanged group[" + group + "] checkedId[" + checkedId + "]");
    }

}
