package com.hamster.incontrol;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;


public class TriggerActivity extends Activity {

    Sensor mSensor;
    TriggerListBaseAdapter mListAdapterCondition;
    TriggerListBaseAdapter mListAdapterAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);

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
        if (id == R.id.action_trigger_add) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
