package com.team10.mc.SpotHOT.ui.dialog;

import android.app.Dialog;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.team10.mc.SpotHOT.R;
import com.team10.mc.SpotHOT.db.WiFiTethering;
import com.team10.mc.SpotHOT.db.WiFiTethering.SECURITY_TYPE;




public class WiFiTetheringDialog extends Dialog {

    private WiFiTethering entity;

    public WiFiTetheringDialog(PreferenceActivity context, WiFiTethering entity) {
        super(context);
        this.setContentView(R.layout.wifi_save);
        this.entity = entity;
        initDialog();
    }

    private void initDialog() {
        final EditText ssid = (EditText) findViewById(R.id.ssid);
        final EditText password = (EditText) findViewById(R.id.password);
        final Spinner types = (Spinner) findViewById(R.id.securityType);
        final CheckBox defaultWifi = (CheckBox) findViewById(R.id.defaultWifi);

        setTitle(entity == null ? "New WiFi Hotspot" : "Modify WiFi Hotspot");

        final Button saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (entity == null) {
                        entity = new WiFiTethering();
                    }

                    entity.setSsid(ssid.getText().toString());
                    entity.setPassword(password.getText().toString());
                    entity.setType(SECURITY_TYPE.valueOf((String) types.getSelectedItem()));
                    //entity.setHidden(hiddenWifi.isChecked());
                    entity.setDefaultWiFi(defaultWifi.isChecked());

                    dismiss();
                }
            }
        });

        final Button closeBtn = (Button) findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                password.setEnabled(!"OPEN".equalsIgnoreCase(parent.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        initComponents(ssid, password, types, defaultWifi);
    }

    private void initComponents(EditText ssid, EditText password, Spinner types, CheckBox defaultWifi) {
        if (entity != null) {
            ssid.setText(entity.getSsid());
            password.setText(entity.getPassword());
            defaultWifi.setChecked(entity.isDefaultWiFi());
            defaultWifi.setEnabled(!defaultWifi.isChecked());
            //hiddenWifi.setChecked(entity.isHidden());
        }

        String defaultSelection = entity != null ? entity.getType().name() : "WPA2PSK";

        String[] securityValues = getContext().getResources().getStringArray(R.array.securityTypes);
        for (int i = 0; i < securityValues.length; i++) {
            if (securityValues[i].equals(defaultSelection)) {
                types.setSelection(i, true);
                break;
            }
        }
    }

    private boolean validate() {
        final EditText ssid = (EditText) findViewById(R.id.ssid);
        final EditText password = (EditText) findViewById(R.id.password);
        final Spinner types = (Spinner) findViewById(R.id.securityType);

        if (TextUtils.isEmpty(ssid.getText())) {
            ssid.requestFocus();
            ssid.setError("SSID name cannot be empty!");
            return false;
        }

        if (!types.getSelectedItem().equals("OPEN") && (TextUtils.getTrimmedLength(password.getText()) < 8)) {
            password.requestFocus();
            password.setError("Password cannot be shorter than 8 characters!");
            return false;
        }

        return true;
    }

    public WiFiTethering getEntity() {
        return entity;
    }
}
