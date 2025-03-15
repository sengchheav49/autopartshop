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

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(order);
            }
        });

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewOrderNumber;
        private final TextView textViewOrderDate;
        private final TextView textViewOrderStatus;
        private final TextView textViewOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderNumber = itemView.findViewById(R.id.textViewOrderNumberr);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDatee);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatuss);
            textViewOrderTotal = itemView.findViewById(R.id.textViewOrderTotall);
        }

        public void bind(Order order) {
            textViewOrderNumber.setText(order.getOrderNumber());
            textViewOrderDate.setText(order.getFormattedCreatedAt()); // Use formatted date
            textViewOrderStatus.setText(order.getStatus());
            textViewOrderTotal.setText(order.getFormattedTotalAmount());

            // Set status color based on order status
            if ("Delivered".equals(order.getStatus())) {
                textViewOrderStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.colorSuccess));
            } else if ("Cancelled".equals(order.getStatus())) {
                textViewOrderStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.colorError));
            } else {
                textViewOrderStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.colorWarning));
            }
        }
    }
}