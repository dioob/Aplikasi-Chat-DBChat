package dioobanu.yahoo.dbchat;

public class Messages {

    private String message,type;
    private boolean seen;
    private long time;
    private String from, tempat, waktu;


    public Messages (String message, boolean seen, long time, String type, String from, String tempat, String waktu){

        this.message=message;
        this.seen=seen;
        this.time=time;
        this.type=type;
        this.from = from;
        this.tempat=tempat;
        this.waktu=waktu;

    }
    public Messages(){}



    public String getMessage() {
        return message; }

    public void setMessage(String message) { this.message = message; }

    public boolean getSeen() {
        return seen; }

    public void setSeen(boolean seen) { this.seen = seen; }

    public long getTime() {
        return time; }

    public void setTime(long time) { this.time = time; }

    public String getType() {
        return type; }

    public void setType(String type) { this.type = type; }

    public String getFrom() {
        return from; }

    public void setFrom(String from) { this.from = from; }

    //untuk mengakses dan mengambil dari firebase
    public String getTempat() {
        return tempat; }

    public void setTempat(String tempat) { this.tempat = tempat; }

    public String getWaktu() {
        return waktu; }

    public void setWaktu(String waktu) { this.waktu = waktu; }


}
