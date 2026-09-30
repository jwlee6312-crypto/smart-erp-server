package com.crmbank.erp.mobile;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class InboundRegisterAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> itemList;
    private LayoutInflater inflater;

    public InboundRegisterAdapter(Context context, List<Map<String, Object>> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_inbound_register, parent, false);
            holder = new ViewHolder();
            holder.tvItemCode = convertView.findViewById(R.id.tvItemCode);
            holder.tvItemName = convertView.findViewById(R.id.tvItemName);
            holder.tvAutoYnBadge = convertView.findViewById(R.id.tvAutoYnBadge);
            holder.tvLotNo = convertView.findViewById(R.id.tvLotNo);
            holder.tvOrderQty = convertView.findViewById(R.id.tvOrderQty);
            holder.etInboundQty = convertView.findViewById(R.id.etInboundQty);
            
            holder.quantityWatcher = new QuantityWatcher();
            holder.etInboundQty.addTextChangedListener(holder.quantityWatcher);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, Object> item = itemList.get(position);
        holder.quantityWatcher.updatePosition(position);

        holder.tvItemCode.setText(getStringValue(item, "ITEMCD"));
        
        String itemNm = getStringValue(item, "ITEMNM");
        if (itemNm.isEmpty()) itemNm = getStringValue(item, "itemnm");
        holder.tvItemName.setText(itemNm);

        // 💡 autoyn == 'Y' 시리얼 필수 관리 품목 시각적 강조 배지 표시
        String autoYn = getStringValue(item, "autoyn");
        if (autoYn.isEmpty()) autoYn = getStringValue(item, "AUTOYN");
        if ("Y".equalsIgnoreCase(autoYn)) {
            if (holder.tvAutoYnBadge != null) {
                holder.tvAutoYnBadge.setVisibility(View.VISIBLE);
                holder.tvAutoYnBadge.setText("[스캔필수]");
            }
            holder.tvItemName.setTextColor(android.graphics.Color.parseColor("#E65100"));
        } else {
            if (holder.tvAutoYnBadge != null) {
                holder.tvAutoYnBadge.setVisibility(View.GONE);
            }
            holder.tvItemName.setTextColor(android.graphics.Color.parseColor("#333333"));
        }
        
        // 1. 의뢰수량 표시 (ioqty / balqty / qty)
        String orderQty = getStringValue(item, "ioqty");
        if (orderQty.isEmpty()) orderQty = getStringValue(item, "balqty");
        if (orderQty.isEmpty()) orderQty = getStringValue(item, "qty");
        if (orderQty.isEmpty()) orderQty = "0";
        holder.tvOrderQty.setText(orderQty);

        // 2. 스캔수량 표시 (scan_qty - DB에 기록된 실스캔 수량이 있으면 반영, 없으면 0)
        Object scanQtyObj = item.get("scan_qty");
        if (scanQtyObj == null) scanQtyObj = item.get("SCAN_QTY");
        double scanQtyVal = 0.0;
        if (scanQtyObj != null) {
            try { scanQtyVal = Double.parseDouble(String.valueOf(scanQtyObj)); } catch (Exception ignored) {}
        }
        String scanQtyStr = scanQtyVal == (long) scanQtyVal ? String.format(java.util.Locale.getDefault(), "%d", (long) scanQtyVal) : String.valueOf(scanQtyVal);
        holder.etInboundQty.setText(scanQtyStr);

        // 3. 최근 스캔 LOT / 시리얼 번호 표시
        String lotNo = getStringValue(item, "lotno");
        if (lotNo.isEmpty()) lotNo = getStringValue(item, "LOTNO");
        if (lotNo.isEmpty()) lotNo = "-";
        if (holder.tvLotNo != null) holder.tvLotNo.setText(lotNo);

        return convertView;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase());
        if (val == null) val = map.get(key.toLowerCase());
        return val != null ? val.toString() : "";
    }

    static class ViewHolder {
        TextView tvItemCode, tvItemName, tvOrderQty, tvAutoYnBadge, tvLotNo;
        EditText etInboundQty;
        QuantityWatcher quantityWatcher;
    }

    private class QuantityWatcher implements TextWatcher {
        private int position;
        public void updatePosition(int position) { this.position = position; }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (itemList.size() > position) {
                // ?낅젰 ?섎웾??ioqty ?꾨뱶?????
                itemList.get(position).put("ioqty", s.toString());
            }
        }
    }
}
