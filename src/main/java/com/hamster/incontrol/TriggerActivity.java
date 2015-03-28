package com.hamster.incontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;


public class TriggerActivity extends Activity {

    Sensor mSensor;
    TriggerActivity mThisActivity; // used in listeners
    TriggerListBaseAdapter mListAdapterCondition;
    TriggerListBaseAdapter mListAdapterAction;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);
        mThisActivity = this;

        mListAdapterCondition = new TriggerListBaseAdapter(this);
        ListView lv_conds = (ListView) findViewById(R.id.list_cond);
        lv_conds.setAdapter(mListAdapterCondition);

        mListAdapterAction = new TriggerListBaseAdapter(this);
        ListView lv_acts = (ListView) findViewById(R.id.list_act);
        lv_acts.setAdapter(mListAdapterAction);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalConfigStore lcs = new LocalConfigStore(this);
        mSensor = lcs.getSensorById(Integer.parseInt(this.getIntent().getStringExtra("id")), true);
        mListAdapterAction.setAssociatedSensor(mSensor);
        mListAdapterCondition.setAssociatedSensor(mSensor);

        lcs.close();
        this.setTitle(getResources().getString(R.string.title_trigger_of) + mSensor.getSensorName());

        Trigger trg = new Trigger(this, mSensor);

        Describable[] conds = new Describable[]{};
        Describable[] acts = new Describable[]{};
        conds = trg.getAllConditions().toArray(conds);
        acts = trg.getAllActions().toArray(acts);

        mListAdapterCondition.clearAll();
        mListAdapterCondition.addAllItems(conds);
        mListAdapterCondition.notifyDataSetChanged();

        mListAdapterAction.clearAll();
        mListAdapterAction.addAllItems(acts);
        mListAdapterAction.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trigger, menu); // Only a + button. Same here
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_condition_add) {
            showEditDialog(true);
            return true;
        } else if (id == R.id.action_action_add) {
            showEditDialog(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showEditDialog(final boolean isCondition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView;

        if (isCondition) {
            dialogView = inflater.inflate(R.layout.dialog_add_trigger_condition, null);
        } else {
            dialogView = inflater.inflate(R.layout.dialog_add_trigger_action, null);
        }

        if (isCondition)
            builder.setTitle(R.string.menu_add_condition);
        else
            builder.setTitle(R.string.menu_add_action);
        // We don't edit, we delete.

        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        DialogInterface.OnClickListener dialogOKListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Spinner sp;
                EditText et;

                if (isCondition) {
                    sp = (Spinner) dialog.findViewById(R.id.spinner_when);
                    et = (EditText) dialog.findViewById(R.id.et_when);
                    if (et.getText().toString().equals("")) return;
                } else {
                    sp = (Spinner) dialog.findViewById(R.id.spinner_do);
                    et = (EditText) dialog.findViewById(R.id.et_action);
                    if (getResources().getIntArray(R.array.when_id)[sp.getSelectedItemPosition()]
                            == Trigger.ActionType.ACTION_SEND_SMS.ordinal()
                            && et.getText().toString().equals(""))
                        return;
                }

                Trigger trg = mSensor.getTriggerInstance();
                if (isCondition) {
                    trg.addCondition(trg.new Condition(
                            Trigger.convertIntToConditionType(
                                    getResources().getIntArray(R.array.when_id)
                                            [sp.getSelectedItemPosition()]), // Param 1 for new Cond
                            et.getText().toString(), // Param 2 for new Cond
                            trg.getSensor() // Param 3 for new Cond
                    ));
                } else {
                    trg.addAction(trg.new Action(
                                    et.getText().toString(),
                                    null, // Abandoned
                                    Trigger.convertIntToActionType(
                                            getResources().getIntArray(R.array.action_id)
                                                    [sp.getSelectedItemPosition()]))
                    );
                }

                try {
                    mSensor.setTriggerString(trg.toString(), true);
                    if (isCondition) {
                        Describable[] conds = new Describable[]{};
                        conds = trg.getAllConditions().toArray(conds);

                        mListAdapterCondition.clearAll();
                        mListAdapterCondition.addAllItems(conds);
                        mListAdapterCondition.notifyDataSetChanged();
                    } else {
                        Describable[] acts = new Describable[]{};
                        acts = trg.getAllActions().toArray(acts);

                        mListAdapterAction.clearAll();
                        mListAdapterAction.addAllItems(acts);
                        mListAdapterAction.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    Toast.makeText(mThisActivity, R.string.toast_error_upload_trigger,
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (!isCondition) {
            // don't use dialog. It's not inflated. Use dialogView instead
            final EditText et_do = (EditText) dialogView.findViewById(R.id.et_action);
            final Spinner sp_do = (Spinner) dialogView.findViewById(R.id.spinner_do);

            AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == Trigger.ActionType.ACTION_SHOW_NOTIFICATION.ordinal()) {
                        et_do.setVisibility(View.INVISIBLE);
                    } else {
                        et_do.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // will this ever happen?
                }
            };

            // it doesn't support onItemCLICKlistener
            sp_do.setOnItemSelectedListener(itemSelectedListener);
        }

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getResources().getString(R.string.button_cancel),
                (DialogInterface.OnClickListener) null);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getResources().getString(R.string.button_ok), dialogOKListener);

        dialog.show();
    }
}
