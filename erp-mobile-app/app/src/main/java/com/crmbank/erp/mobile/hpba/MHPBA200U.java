package com.crmbank.erp.mobile.hpba;
import android.os.Bundle;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.R;
public class MHPBA200U extends BaseActivity {
    @Override protected void onCreate(Bundle s) { super.onCreate(s); setContentView(R.layout.activity_template); }
    @Override protected String getProgramTitle() { return "품목별 표준공정도"; }
    @Override protected String getProgramId() { return "MHPBA_200U"; }
}