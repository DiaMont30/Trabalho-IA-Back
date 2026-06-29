package com.plataforma.conversacional.storage;

import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.entity.DocumentChunk;
import com.plataforma.conversacional.retrieval.ScoredChunk;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Primary
@Component
public class PostgresVectorStore implements VectorStore {

    private static final int DIMENSION = 768;

    private final JdbcTemplate jdbcTemplate;

    public PostgresVectorStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void storeChunk(DocumentChunk chunk, float[] embedding) {
        storeChunks(List.of(chunk), List.of(embedding));
    }

    @Override
    public void storeChunks(List<DocumentChunk> chunks, List<float[]> embeddings) {
        String sql = "UPDATE document_chunks SET embedding = ?::vector WHERE id = ?";
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String vectorStr = floatArrayToPgVector(embeddings.get(i));
            batchArgs.add(new Object[]{vectorStr, chunks.get(i).getId()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public List<ScoredChunk> searchSimilar(float[] queryEmbedding, int topK, double minScore) {
        String vectorStr = floatArrayToPgVector(queryEmbedding);
        String sql = """
            SELECT dc.id, dc.document_id, dc.content, dc.chunk_index, dc.metadata, dc.created_at,
                   d.original_name,
                   1 - (dc.embedding <=> ?::vector) AS score
            FROM document_chunks dc
            JOIN documents d ON d.id = dc.document_id
            WHERE dc.embedding IS NOT NULL
              AND 1 - (dc.embedding <=> ?::vector) >= ?
            ORDER BY dc.embedding <=> ?::vector
            LIMIT ?
            """;
        return jdbcTemplate.query(sql, this::mapToScoredChunk, vectorStr, vectorStr, minScore, vectorStr, topK);
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        jdbcTemplate.update("UPDATE document_chunks SET embedding = NULL WHERE document_id = ?", documentId);
    }

    private ScoredChunk mapToScoredChunk(ResultSet rs, int rowNum) throws SQLException {
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(rs.getLong("id"));

        Document document = new Document();
        document.setId(rs.getLong("document_id"));
        document.setOriginalName(rs.getString("original_name"));
        chunk.setDocument(document);

        chunk.setContent(rs.getString("content"));
        chunk.setChunkIndex(rs.getInt("chunk_index"));
        chunk.setMetadata(rs.getString("metadata"));
        if (rs.getTimestamp("created_at") != null) {
            chunk.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        double score = rs.getDouble("score");
        return new ScoredChunk(chunk, score);
    }

    private String floatArrayToPgVector(float[] embedding) {
        if (embedding.length != DIMENSION) {
            throw new IllegalArgumentException(
                "Embedding dimension mismatch: expected " + DIMENSION + ", got " + embedding.length);
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
