package com.example.cdmaster;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// recycleview.adapter
// recyclerview.holder
public class RowAdaptor extends RecyclerView.Adapter<RowAdaptor.RowAdaptorViewHolder>
{
    @NonNull
    private Context ctx;
    private List<RowCls> RowList;

    public OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeviceNameClick(int position);
        void onDeleteClick(int position);
        void onActionClick(int position);
        void onOtaClick(int position);
        void  onCredClick(int position);
        void  onImageClick(int position);
        void  onAuditClick(int position);
    };

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener = listener;
    }

    public RowAdaptor(@NonNull Context ctx, List<RowCls> rowList)
    {
        this.ctx = ctx;
        RowList = rowList;
    }



    @Override
    public RowAdaptorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater Inflator = LayoutInflater.from(ctx);
        View view = Inflator.inflate(R.layout.row_itm,parent,false);
        RowAdaptorViewHolder holder = new RowAdaptorViewHolder(view, mListener);
        return  holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RowAdaptorViewHolder holder, int position) {
        RowCls rowCls = RowList.get(position);
        holder.Image.setImageDrawable(ctx.getResources().getDrawable(rowCls.getDevImage()));
        holder.TvDevName.setText(rowCls.getDevName());

    }

    @Override
    public int getItemCount() {
        return RowList.size();
    }

    class RowAdaptorViewHolder extends RecyclerView.ViewHolder
    {
        ImageView Image;
        TextView TvDevName,TvAudit;
        Button tempButton_Action,tempButton_Delete,tempButton_Ota,tempButton_Cred;

        public RowAdaptorViewHolder(@NonNull View itemView, final OnItemClickListener  listener) {
            super(itemView);

            Image = itemView.findViewById(R.id.ImageView_Icon);
            TvDevName = itemView.findViewById(R.id.TextView_Name);
            TvAudit = itemView.findViewById(R.id.TextView_Audits);

            tempButton_Delete = itemView.findViewById(R.id.Button_Delete);
            tempButton_Action = itemView.findViewById(R.id.Button_Action);
            tempButton_Ota = itemView.findViewById(R.id.Button_Ota);
            tempButton_Cred = itemView.findViewById(R.id.Button_Cred);

            Image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                    {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION )
                        {
                            // TODO : set all other test style to bold
                            listener.onImageClick(position);  // callback
                        }
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                    {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION )
                        {
                            // TODO : set all other test style to bold
                            listener.onItemClick(position);  // callback
                        }
                    }
                }
            });

            // Button click

            tempButton_Delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION )
                    {
                        listener.onDeleteClick(position);  // callback

                    }
                }
            });

            // Action button click
            tempButton_Action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION )
                    {
                        listener.onActionClick(position); // callback
                    }
                }
            });

            // OTA button click
            tempButton_Ota.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION )
                    {
                        listener.onOtaClick(position); // callback
                    }
                }
            });

            //
            TvDevName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION )
                    {
                        listener.onDeviceNameClick(position); // callback
                    }
                }
            });

            // OTA button click
            tempButton_Cred.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION )
                    {
                        listener.onCredClick(position); // callback
                    }
                }
            });
            TvAudit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION )
                    {
                        listener.onAuditClick(position); // callback
                    }
                }
            });
        }
    }
}
