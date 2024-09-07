package com.project.spaceshooter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ScoreDatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:D:/SpaceShooter/assets/database.db";
    private Map<Integer, Long> scoreTimestampMap = new HashMap<>();

    public void saveScore(int score) {
        // Kiểm tra xem điểm số đã được lưu chưa
        if (!scoreTimestampMap.containsKey(score)) {
            String sql = "INSERT INTO scores (score, timestamp) VALUES (?, ?)";

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, score);
                long currentTimeMillis = System.currentTimeMillis();
                pstmt.setTimestamp(2, new Timestamp(currentTimeMillis));
                pstmt.executeUpdate();

                // Lưu điểm số và thời gian thực vào Map
                scoreTimestampMap.put(score, currentTimeMillis);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public Map<Integer, String> getHistoricalScores() {
        Map<Integer, String> historicalScores = new HashMap<>();

        String sql = "SELECT score, timestamp FROM scores ORDER BY score DESC"; // Sắp xếp theo điểm số giảm dần

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int score = rs.getInt("score");
                long timestampMillis = rs.getLong("timestamp");

                // Chuyển đổi timestamp thành định dạng giờ
                Timestamp timestamp = new Timestamp(timestampMillis);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedTimestamp = sdf.format(timestamp);

                // Thêm vào Map
                historicalScores.put(score, formattedTimestamp);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return historicalScores;
    }
}
