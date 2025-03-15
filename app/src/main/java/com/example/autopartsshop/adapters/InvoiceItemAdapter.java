package com.example.autopartsshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.models.Order;

import java.util.List;

public class InvoiceItemAdapter extends RecyclerView.Adapter<InvoiceItemAdapter.InvoiceItemViewHolder> {

    private final List<Order.OrderItem> orderItems;

    public InvoiceItemAdapter(List<Order.OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public InvoiceItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceItemViewHolder holder, int position) {
        Order.OrderItem orderItem = orderItems.get(position);
        holder.bind(orderItem);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class InvoiceItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewInvoiceItemName;
        private final TextView textViewInvoiceItemPrice;
        private final TextView textViewInvoiceItemQuantity;
        private final TextView textViewInvoiceItemSubtotal;

        public InvoiceItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewInvoiceItemName = itemView.findViewById(R.id.textViewInvoiceItemName);
            textViewInvoiceItemPrice = itemView.findViewById(R.id.textViewInvoiceItemPrice);
            textViewInvoiceItemQuantity = itemView.findViewById(R.id.textViewInvoiceItemQuantity);
            textViewInvoiceItemSubtotal = itemView.findViewById(R.id.textViewInvoiceItemSubtotal);
        }

        public void bind(Order.OrderItem orderItem) {
            textViewInvoiceItemName.setText(orderItem.getProduct().getName());
            textViewInvoiceItemPrice.setText(orderItem.getFormattedProductPrice());
            textViewInvoiceItemQuantity.setText(String.valueOf(orderItem.getQuantity()));
            textViewInvoiceItemSubtotal.setText(orderItem.getFormattedSubtotal());
        }
    }
}