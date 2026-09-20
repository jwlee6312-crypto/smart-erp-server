package com.crmbank.erp.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

/**
 * 🚀 대량 데이터 고속 처리를 위한 고성능 팝업 어댑터 (네비처리/가상화 지원)
 */
public class PopupAdapter extends RecyclerView.Adapter<PopupAdapter.ViewHolder> {

    private final List<Map<String, Object>> items;
    private final String type; // "CUST", "ITEM", "ADDR"
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Map<String, Object> item);
    }

    public PopupAdapter(List<Map<String, Object>> items, String type, OnItemClickListener listener) {
        this.items = items;
        this.type = type;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 기존 팝업 아이템 레이아웃 재사용 (android.R.id.text1 포함된 것)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popup_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);
        
        String title = "";
        String code = "";
        
        if ("CUST".equals(type)) {
            title = getStringVal(item, "custnm");
            code = getStringVal(item, "custcd");
            holder.tvText.setText(String.format("%s (%s)", title, code));
            holder.tvText.setTextSize(14f);
        } else if ("ADDR".equals(type)) {
            // 🚀 배송처 조회 시 주소코드와 전체 주소를 한 줄에 표현 (폰트 축소)
            String trancd = getStringVal(item, "trancd");
            String address = getStringVal(item, "address");
            String d_address = getStringVal(item, "d_address");
            holder.tvText.setText(String.format("[%s] %s %s", trancd, address, d_address));
            holder.tvText.setTextSize(11f); // 폰트 크기 줄임
            holder.tvText.setSingleLine(true);
        } else {
            title = getStringVal(item, "itemnm");
            code = getStringVal(item, "itemcd");
            holder.tvText.setText(String.format("%s (%s)", title, code));
            holder.tvText.setTextSize(14f);
        }
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null) return "";
        Object val = map.get(key.toLowerCase());
        if (val == null) val = map.get(key.toUpperCase());
        return val != null ? String.valueOf(val).trim() : "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // item_popup_list.xml 내부의 TextView ID (android.R.id.text1)
            tvText = itemView.findViewById(android.R.id.text1);
        }
    }
}
