package edu.vntu.mblog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.vntu.mblog.domain.Post;

public class PostsDao extends AbstractDao {
    public PostsDao() {}

    public void create(long authorId, String text) {
        String sql = "insert into posts (owner_id, text) VALUES (?,?)";
        Connection con = getConnection();

        try (PreparedStatement createSt = con.prepareStatement(sql)) {
            createSt.setLong(1, authorId);
            createSt.setString(2, text);
            createSt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void moderate(long postId, Post.State state) {
        String sql = "UPDATE posts SET post_validation_date=NOW(), state=? where id=?";
        Connection con = getConnection();

        try (PreparedStatement createSt = con.prepareStatement(sql)) {
            createSt.setInt(1, state.ordinal());
            createSt.setLong(2, postId);
            createSt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Post> getAll(int offset, int limit) {
        String sql = "SELECT users.login AS login, users.avatar AS avatar, posts.* FROM users, posts "
                + "WHERE users.id = posts.owner_id AND posts.state = ? "
                + "ORDER BY posts.stamp DESC LIMIT ? OFFSET ?";


        Connection con = getConnection();

        try (PreparedStatement getSt = con.prepareStatement(sql)) {
            getSt.setInt(1, Post.State.UNVALIDATED.ordinal());
            getSt.setInt(2, limit);
            getSt.setInt(3, offset);

            ResultSet results = getSt.executeQuery();

            List<Post> posts = new ArrayList<>();

            while (results.next()) {
                posts.add(convert(results));
            }

            return posts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getCountForUser(long userId) {
        String sql = "SELECT COUNT(*) FROM posts WHERE posts.owner_id = ? AND posts.state != ?";

        Connection con = getConnection();

        try (PreparedStatement getSt = con.prepareStatement(sql)) {
            getSt.setLong(1, userId);
            getSt.setInt(2, Post.State.DISABLED.ordinal());

            ResultSet results = getSt.executeQuery();

            if(!results.next()) return 0;

            return results.getInt(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Post> getFeed(long userId, int offset, int limit) {
        String sql = "SELECT users.avatar, users.login AS login, posts.* FROM users, posts "
                + "WHERE users.id = posts.owner_id AND ("
                + "users.id IN (SELECT followed_id FROM user_followers WHERE subscriber_id=?) "
                + "OR users.id = ?)  AND posts.state != ? "
                + "ORDER BY posts.stamp DESC LIMIT ? OFFSET ?";

        Connection con = getConnection();

        try (PreparedStatement getSt = con.prepareStatement(sql)) {
            getSt.setLong(1, userId);
            getSt.setLong(2, userId);
            getSt.setInt(3, Post.State.DISABLED.ordinal());
            getSt.setInt(4, limit);
            getSt.setInt(5, offset);

            ResultSet results = getSt.executeQuery();

            List<Post> posts = new ArrayList<>();

            while (results.next()) {
                posts.add(convert(results));
            }

            return posts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Post convert(ResultSet rs) throws SQLException {
        return new Post(
                rs.getLong("id"),
                rs.getString("login"),
                rs.getString("avatar"),
                rs.getString("text"),
                rs.getTimestamp("stamp")
        );
    }
}
