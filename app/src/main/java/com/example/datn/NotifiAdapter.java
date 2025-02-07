package com.example.datn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class NotifiAdapter extends ArrayAdapter<NotificationItem> {

    private Context context;
    private List<NotificationItem> notificationList;

    public NotifiAdapter(Context context, List<NotificationItem> notificationList) {
        super(context, R.layout.notification_item, notificationList);
        this.context = context;
        this.notificationList = notificationList;
        sortNotifications();
    }


    private void sortNotifications() {
        Collections.sort(notificationList, new Comparator<NotificationItem>() {
            @Override
            public int compare(NotificationItem o1, NotificationItem o2) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date date1 = sdf.parse(o1.getTimestamp());
                    Date date2 = sdf.parse(o2.getTimestamp());
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.notification_item, parent, false);


        TextView txtMessage = rowView.findViewById(R.id.notificationMessage);
        TextView txtTime = rowView.findViewById(R.id.notificationTimestamp);
        TextView txtCoordinates = rowView.findViewById(R.id.notificationCoordinates);
        ImageView icon = rowView.findViewById(R.id.iconNotification);


        NotificationItem currentItem = notificationList.get(position);


        txtMessage.setText(currentItem.getMessage());
        txtTime.setText(currentItem.getTimestamp());
        txtCoordinates.setText(currentItem.getCoordinates());
        icon.setImageResource(R.drawable.baseline_notifications_none_24);

        return rowView;
    }
}
