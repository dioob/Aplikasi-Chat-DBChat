package dioobanu.yahoo.dbchat;

import android.content.Context;

public class GetTimeTadi {

    private static final int MILLI_DETIK = 1000;
    private static final int MILLI_MENIT = 60 * MILLI_DETIK;
    private static final int MILLI_JAM = 60 * MILLI_MENIT;
    private static final int MILLI_HARI = 24 * MILLI_JAM;


    public static String getTimeTadi(long time, Context ctx) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }
        final long diff = now - time;
        if (diff < MILLI_MENIT) {
            return "Baru saja";
        } else if (diff < 2 * MILLI_MENIT) {
            return "Semenit lalu";
        } else if (diff < 50 * MILLI_MENIT) {
            return diff / MILLI_MENIT + " menit lalu";
        } else if (diff < 90 * MILLI_MENIT) {
            return "Sejam lalu";
        } else if (diff < 24 * MILLI_JAM) {
            return diff / MILLI_JAM + " jam lalu";
        } else if (diff < 48 * MILLI_JAM) {
            return "Kemaren";
        } else {
            return diff / MILLI_HARI + " hari lalu";
        }
    }


}
