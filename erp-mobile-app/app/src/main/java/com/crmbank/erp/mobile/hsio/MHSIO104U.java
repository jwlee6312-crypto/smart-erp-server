package com.crmbank.erp.mobile.hsio;

import android.content.Intent;
import android.os.Bundle;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.InboundRegisterActivity;

/**
 * 🚀 [MHSIO104U] 바코드 입고처리 전용 독립 메뉴 (기존 HSIO010U 수동 입고 화면과 완전 분리)
 */
public class MHSIO104U extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, InboundRegisterActivity.class);
        intent.putExtra("IOGBN_MODE", "100");
        startActivity(intent);
        finish();
    }

    @Override protected String getProgramTitle() { return "바코드 입고 처리"; }
    @Override protected String getProgramId() { return "MHSIO104U"; }
}
