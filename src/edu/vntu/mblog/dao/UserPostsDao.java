package edu.vntu.mblog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.EnumSet;

import edu.vntu.mblog.domain.Post;
import edu.vntu.mblog.domain.User;
import edu.vntu.mblog.domain.User.Roles;

public class UserPostsDao extends AbstractDao {
	public UserPostsDao() {}
	
	//savePost(Post user) � �� � Post getPost(long id)
	
	public void savePost(Post userPost) {
		String sql = "insert into posts (owner_id, text) VALUES (?,?)";
		Connection con = getConnection();
		PreparedStatement createSt = null;
		try {
			createSt = con.prepareStatement(sql);
			createSt.setLong(1, userPost.getOwnerId());
			createSt.setString(2, userPost.getText());
			createSt.executeUpdate();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(createSt, con);
		}
	 }
	 
	 public Post getPost(long userId) {
		String sql = "select * from posts where owner_id=?";
		Connection con = getConnection();
		PreparedStatement getSt = null;
		ResultSet results = null;
		try {
			getSt = con.prepareStatement(sql);
			getSt.setLong(1, userId);
			
			results = getSt.executeQuery();

			if (!results.next()) 
				return null;
			
			return convert(results);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(results, getSt, con);
		}
	 }
	 
	 private Post convert(ResultSet rs) throws SQLException {
		 return new Post(
				 rs.getLong("id"),
				 rs.getLong("owner_id"),
				 rs.getString("text"),
				 rs.getTimestamp("stamp")
		 );
	 }
	 
	 public Post getAllUserAndFollowersPosts(long userId) {
			String sql = "select users.login, posts.text from users, posts where users.id=posts.owner_id "
					+ "in (?, select followed_id from users_followers where subscriber_id=?) "
					+ "order by posts.stamp desc limit=? offset=?";
			Connection con = getConnection();
			PreparedStatement getSt = null;
			ResultSet results = null;
			int limit=0;
			int offset=0;
			try {
				getSt = con.prepareStatement(sql);
				getSt.setLong(1, userId);
				getSt.setLong(2, userId);
				getSt.setInt(3, limit);
				getSt.setInt(4, offset);
				
				results = getSt.executeQuery();

				if (!results.next()) 
					return null;
				
				return convert(results);   //?????????????????????????
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				close(results, getSt, con);
			}
		 }


}
