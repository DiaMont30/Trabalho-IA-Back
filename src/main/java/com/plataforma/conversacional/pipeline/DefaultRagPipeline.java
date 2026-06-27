package com.plataforma.conversacional.pipeline;

import com.plataforma.conversacional.entity.DocumentChunk;
import com.plataforma.conversacional.entity.SourceReference;
import com.plataforma.conversacional.retrieval.Retriever;
import com.plataforma.conversacional.retrieval.ScoredChunk;
import com.plataforma.conversacional.strategy.MessageProcessingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultRagPipeline implements RagPipeline {

    private final Retriever retriever;
    private final MessageProcessingStrategy messageProcessingStrategy;
    private final int topK;

    public DefaultRagPipeline(
            Retriever retriever,
            MessageProcessingStrategy messageProcessingStrategy,
            @Value("${app.rag.retrieval.top-k}") int topK) {
        this.retriever = retriever;
        this.messageProcessingStrategy = messageProcessingStrategy;
        this.topK = topK;
    }

    @Override
    public RagResult execute(String query, Long sessionId) {
        List<ScoredChunk> scoredChunks = retriever.retrieve(query, topK);

        StringBuilder context = new StringBuilder();
        List<SourceReference> sources = new ArrayList<>();

        for (ScoredChunk scoredChunk : scoredChunks) {
            DocumentChunk chunk = scoredChunk.getChunk();

            if (!context.isEmpty()) {
                context.append("\n\n");
            }
            context.append(chunk.getContent());

            SourceReference sourceReference = new SourceReference();
            sourceReference.setChunk(chunk);
            sourceReference.setRelevanceScore(scoredChunk.getScore());
            sourceReference.setExcerpt(chunk.getContent().length() > 500
                    ? chunk.getContent().substring(0, 500)
                    : chunk.getContent());

            sources.add(sourceReference);
        }

        String augmentedPrompt = context + "\n\nPergunta: " + query;
        String answer = messageProcessingStrategy.process(augmentedPrompt);

        return new RagResult(answer, sources);
    }
}
