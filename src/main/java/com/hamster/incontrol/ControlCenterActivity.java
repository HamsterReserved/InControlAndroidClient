package com.hamster.incontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;


public class ControlCenterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_center);

        ListView lv = (ListView) findViewById(R.id.manage_device_list);
        DeviceDetailViewAdapter lv_adapter = new DeviceDetailViewAdapter(this.getApplicationContext());
        lv.setAdapter(lv_adapter);

        loadControllers();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control_center, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_control_center_add) {
            showEditDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadControllers() {
        DeviceDetailViewAdapter lv_adapter = (DeviceDetailViewAdapter)
                ((ListView) findViewById(R.id.manage_device_list)).getAdapter();
        LocalConfigStore lcs = new LocalConfigStore(this.getApplicationContext());
        lcs.open();
        lv_adapter.addToControlCenters(lcs.getControlCenters());
        lcs.close();
    }

    private void showEditDialog() { // TODO Currently only supports adding a new one
        // Copied from net
        AlertDialog.Builder builder;
        final AlertDialog alertDialog;
        Context mContext = this;

        final LocalConfigStore lcs = new LocalConfigStore(mContext);
        lcs.open();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.control_center_edit_dialog,
                null);
        builder = new AlertDialog.Builder(mContext);
        builder.setView(layout)
                .setNegativeButton("Cancel", null)
                .setTitle("Add a new InControl Center");
        alertDialog = builder.create();

        DialogInterface.OnClickListener ocl_positive = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                EditText et_cc_id = (EditText) alertDialog.findViewById(R.id.et_control_id);
                EditText et_cc_name = (EditText) alertDialog.findViewById(R.id.et_control_name);
                EditText et_cc_cred = (EditText) alertDialog.findViewById(R.id.et_control_cred);
                ControlCenter cc = new ControlCenter(getApplicationContext());
                cc.setDeviceId(Integer.parseInt(et_cc_id.getText().toString()));
                cc.setDeviceName(et_cc_name.getText().toString());
                cc.setCredentials(et_cc_cred.getText().toString());
                if (cc.isInfoComplete()) lcs.updateDevice(cc, ControlCenter.INVALID_DEVICE_ID);
            }
        };

        alertDialog.setButton("OK", ocl_positive);
        alertDialog.show();
    }
}
