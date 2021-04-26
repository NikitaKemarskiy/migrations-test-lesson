package db.entity;

public class Post {
    private int id;
    private String image;
    private int likes;
    private User user;

    public Post(int id, String image, int likes, User user) {
        this.id = id;
        this.image = image;
        this.likes = likes;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
