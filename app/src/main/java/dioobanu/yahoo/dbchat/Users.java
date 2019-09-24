package dioobanu.yahoo.dbchat;

public class Users {
    //Harus sama dengan yang ada di firebase database
    public String nama;
    public String status;
    public String image;
    public String thumb_image;

    public Users(String nama, String status, String image, String thumb_image) {
        this.nama = nama;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
    }

    public Users(){

    }

        public String getNama () {
            return nama;
        }

        public void setNama (String nama){
            this.nama = nama;
        }

        public String getStatus () {
            return status;
        }

        public void setStatus (String status){
            this.status = status;
        }

        public String getImage () {
            return image;
        }

        public void setImage (String image){
            this.image = image;
        }

        public String getThumb_image () {
            return thumb_image;
        }

        public void setThumb_image (String thumb_image){
            this.thumb_image = thumb_image;
        }
}
