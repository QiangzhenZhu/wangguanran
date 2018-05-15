package com.a10835.easywol.ScrollablePanel;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.a10835.easywol.R;
import com.kelin.scrollablepanel.library.PanelAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kelin on 16-11-18.
 */

public class ScrollablePanelAdapter extends PanelAdapter {
    private static final int TITLE_TYPE = 4;
    private static final int COLUMN_TYPE = 0;
    private static final int RAW_TYPE = 1;
    private static final int CONTENT_TYPE = 2;

    private List<ColumnInfo> columnInfoList = new ArrayList<>();
    private List<RowInfo> rowInfoList = new ArrayList<>();
    private List<List<ContentInfo>> ordersList = new ArrayList<>();


    @Override
    public int getRowCount() {
        return columnInfoList.size() + 1;
    }

    @Override
    public int getColumnCount() {
        return rowInfoList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int row, int column) {
        int viewType = getItemViewType(row, column);
        switch (viewType) {
            case RAW_TYPE:
                setRowView(column, (RawViewHolder) holder);
                break;
            case COLUMN_TYPE:
                setColumnView(row, (ColumnViewHolder) holder);
                break;
            case CONTENT_TYPE:
                setContentView(row, column, (ContentViewHolder) holder);
                break;
            case TITLE_TYPE:
                break;
            default:
                setContentView(row, column, (ContentViewHolder) holder);
                break;
        }
    }

    public int getItemViewType(int row, int column) {
        if (column == 0 && row == 0) {
            return TITLE_TYPE;
        }
        if (column == 0) {
            return COLUMN_TYPE;
        }
        if (row == 0) {
            return RAW_TYPE;
        }
        return CONTENT_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RAW_TYPE:
                return new RawViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_row_info, parent, false));
            case COLUMN_TYPE:
                return new ColumnViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_column_info, parent, false));
            case CONTENT_TYPE:
                return new ContentViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_content_info, parent, false));
            case TITLE_TYPE:
                return new TitleViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_title, parent, false));
            default:
                break;
        }
        return new ContentViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_content_info, parent, false));
    }

    private void setRowView(int pos, RawViewHolder viewHolder) {
        RowInfo rowInfo = rowInfoList.get(pos - 1);
        if (rowInfo != null && pos > 0) {
            viewHolder.stringTextView.setText(rowInfo.getString());
        }
    }

    private void setColumnView(int pos, ColumnViewHolder viewHolder) {
        ColumnInfo columnInfo = columnInfoList.get(pos - 1);
        if (columnInfo != null && pos > 0) {
            viewHolder.columnNumberTextView.setText(columnInfo.getColumnNumber());
        }
    }

    private void setContentView(final int row, final int column, ContentViewHolder viewHolder) {
        final ContentInfo contentInfo = ordersList.get(row - 1).get(column - 1);
        if (contentInfo != null) {
            if (contentInfo.getStatus() == ContentInfo.Status.BLANK) {
                viewHolder.view.setBackgroundResource(R.drawable.bg_white_gray_stroke);
                viewHolder.contentTextView.setText("");
            } else if (contentInfo.getStatus() == ContentInfo.Status.SIGN_OUT) {
//                viewHolder.contentTextView.setText(contentInfo.isBegin() ? contentInfo.getContent() : "");
                viewHolder.contentTextView.setText(contentInfo.getContent());
                viewHolder.view.setBackgroundResource(contentInfo.isBegin() ? R.drawable.bg_room_red_begin_with_stroke : R.drawable.bg_room_red_with_stroke);
            } else if (contentInfo.getStatus() == ContentInfo.Status.SIGN_IN) {
//                viewHolder.contentTextView.setText(contentInfo.isBegin() ? contentInfo.getContent() : "");
                viewHolder.contentTextView.setText(contentInfo.getContent());
                viewHolder.view.setBackgroundResource(contentInfo.isBegin() ? R.drawable.bg_room_blue_begin_with_stroke : R.mipmap.bg_room_blue_middle);
            }
            if (contentInfo.getStatus() != ContentInfo.Status.BLANK) {
                viewHolder.itemView.setClickable(true);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if (contentInfo.isBegin()) {
                            Toast.makeText(v.getContext(), "name:" + contentInfo.getContent(), Toast.LENGTH_SHORT).show();
//                        } else {
//                            int i = 2;
//                            while (column - i >= 0 && ordersList.get(row - 1).get(column - i).getId() == contentInfo.getId()) {
//                                i++;
//                            }
//                            final ContentInfo info = ordersList.get(row - 1).get(column - i + 1);
//                            Toast.makeText(v.getContext(), "name:" + info.getContent(), Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
            } else {
                viewHolder.itemView.setClickable(false);
            }
        }
    }

    private static class RawViewHolder extends RecyclerView.ViewHolder {
        public TextView stringTextView;

        public RawViewHolder(View itemView) {
            super(itemView);
            this.stringTextView = (TextView) itemView.findViewById(R.id.string);
        }

    }

    private static class ColumnViewHolder extends RecyclerView.ViewHolder {
        public TextView columnNumberTextView;

        public ColumnViewHolder(View view) {
            super(view);
            this.columnNumberTextView = (TextView) view.findViewById(R.id.column_number);
        }
    }

    private static class ContentViewHolder extends RecyclerView.ViewHolder {
        public TextView contentTextView;
        public View view;

        public ContentViewHolder(View view) {
            super(view);
            this.view = view;
            this.contentTextView = (TextView) view.findViewById(R.id.content);
        }
    }

    private static class TitleViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        public TitleViewHolder(View view) {
            super(view);
            this.titleTextView = (TextView) view.findViewById(R.id.title);
        }
    }

    public void setColumnInfoList(List<ColumnInfo> columnInfoList) {
        this.columnInfoList = columnInfoList;
    }

    public void setRowInfoList(List<RowInfo> rowInfoList) {
        this.rowInfoList = rowInfoList;
    }

    public void setOrdersList(List<List<ContentInfo>> ordersList) {
        this.ordersList = ordersList;
    }
}
