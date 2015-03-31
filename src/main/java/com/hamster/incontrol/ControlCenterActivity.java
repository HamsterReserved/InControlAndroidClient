package com.hamster.incontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.hamster.incontrol.NetworkBackgroundOperator.BackgroundTaskDesc;
import com.hamster.incontrol.NetworkBackgroundOperator.Operation;

public class ControlCenterActivity extends Activity {

    private Handler handler = new Handler();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_center);

        mContext = this;
        ListView lv = (ListView) findViewById(R.id.manage_device_list);
        DeviceDetailViewAdapter lv_adapter = new DeviceDetailViewAdapter(this);
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
            showAddDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadControllers() {
        DeviceDetailViewAdapter lv_adapter = (DeviceDetailViewAdapter)
                ((ListView) findViewById(R.id.manage_device_list)).getAdapter();
        LocalConfigStore lcs = new LocalConfigStore(this.getApplicationContext());
        lv_adapter.clearAll();
        lv_adapter.addToControlCenters(lcs.getControlCenters());
        lcs.close();
    }

    private void showAddDialog() {
        // Copied from net
        Resources res = getResources();
        AlertDialog.Builder builder;
        final AlertDialog alertDialog;
        final Context mContext = this;
        final LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.dialog_edit_control_center, null);
        builder = new AlertDialog.Builder(mContext);
        builder.setView(layout)
                .setNegativeButton(res.getText(R.string.button_cancel), null)
                .setTitle(res.getText(R.string.dialog_title_add_new_center));
        alertDialog = builder.create();

        DialogInterface.OnClickListener ocl_positive = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                EditText et_cc_id = (EditText) alertDialog.findViewById(R.id.et_control_id);
                EditText et_cc_name = (EditText) alertDialog.findViewById(R.id.et_control_name);
                EditText et_cc_cred = (EditText) alertDialog.findViewById(R.id.et_control_cred);

                ControlCenter cc = new ControlCenter(getApplicationContext());
                try {
                    cc.setDeviceId(Integer.parseInt(et_cc_id.getText().toString()));
                } catch (NumberFormatException e) {
                    Toast.makeText(mContext, "Device ID format error!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // -1 if null, which is exactly INVAILD_SENSOR_ID
                cc.setDeviceName(et_cc_name.getText().toString());
                cc.setCredentials(et_cc_cred.getText().toString());

                checkAndAddControlCenter(cc);
            }
        };

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, res.getText(R.string.button_ok), ocl_positive);
        alertDialog.show();
    }


    private void dismissCheckDialog(ProgressDialog dialog) {
        dialog.dismiss();
    }

    private void reallyAddNewControlCenter(ControlCenter cc) {
        LocalConfigStore lcs = new LocalConfigStore(mContext);
        if (cc.isInfoComplete()) lcs.updateDevice(cc, ControlCenter.INVALID_DEVICE_ID);
        lcs.close();
    }

    private void checkAndAddControlCenter(final ControlCenter cc) {
        LocalConfigStore lcs = new LocalConfigStore(mContext);
        ControlCenter[] ccs = lcs.getControlCenters();
        lcs.close();

        if (ccs != null) {
            for (ControlCenter temp_cc : ccs) {
                if (temp_cc.getDeviceId() == cc.getDeviceId()) {
                    // We are just changing name
                    reallyAddNewControlCenter(cc);
                    return;
                }
            }
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Verifying...");
        dialog.show();

        // We are adding a new one
        BackgroundTaskDesc task = new BackgroundTaskDesc(Operation.OPERATION_USER_REGISTRATION,
                new Object[]{cc.getDeviceId(), cc.getDeviceName()},
                new Runnable() {
                    @Override
                    public void run() {
                        dismissCheckDialog(dialog);
                        reallyAddNewControlCenter(cc);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadControllers();
                            }
                        });
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        dismissCheckDialog(dialog);
                        Toast.makeText(mContext, "Not allowed to register this Control Center. Please start pairing process on the machine.", Toast.LENGTH_SHORT).show();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadControllers();
                            }
                        });
                    }
                });
        NetworkBackgroundOperator bg = new NetworkBackgroundOperator(task);
        bg.execute();
    }

}
