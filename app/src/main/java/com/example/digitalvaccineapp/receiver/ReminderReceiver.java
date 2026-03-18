package com.example.digitalvaccineapp.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.ui.VaccinationActivity;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String vaccineName = intent.getStringExtra("vaccineName");
        if (vaccineName == null) vaccineName = "Vaccination";

        Intent i = new Intent(context, VaccinationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "VAX_REMINDERS")
                .setSmallIcon(R.drawable.ic_vax)
                .setContentTitle("Vaccine Reminder!")
                .setContentText("It's time for your " + vaccineName + " dose today. Stay protected!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
