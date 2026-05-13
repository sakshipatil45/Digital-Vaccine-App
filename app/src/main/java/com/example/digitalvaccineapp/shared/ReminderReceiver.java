package com.example.digitalvaccineapp.shared;
import com.example.digitalvaccineapp.shared.Vaccination;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.citizen.VaccinationActivity;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String vaccineName = intent.getStringExtra("vaccineName");
        String memberName = intent.getStringExtra("memberName");
        String time = intent.getStringExtra("time");
        String place = intent.getStringExtra("place");
        String date = intent.getStringExtra("date");
        
        if (vaccineName == null) vaccineName = "Vaccination";
        if (memberName == null) memberName = "Family Member";

        // Required format: [Member]: [Vaccine] is scheduled on [Date] at [Time] at [Place]
        String title = "Vaccine Alert: " + memberName;
        String contentText = memberName + "'s " + vaccineName + " is scheduled on " + (date != null ? date : "today") + 
                             " at " + (time != null ? time : "scheduled time") + 
                             " at " + (place != null ? place : "Health Center");

        Intent i = new Intent(context, VaccinationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "VAX_REMINDERS")
                .setSmallIcon(R.drawable.ic_vax)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
