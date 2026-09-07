-- Basic completeness gate. The first row must be: 100 | true.
SELECT COUNT(*) AS concept_count,
       COUNT(*) = 100 AS expected_count
FROM emotion_concept;

-- Must return zero rows.
SELECT name, language
FROM emotion_concept
WHERE BTRIM(name) = ''
   OR BTRIM(language) = ''
   OR BTRIM(meaning) = ''
   OR BTRIM(description) = ''
   OR source_url NOT LIKE 'https://%';

-- Must return zero rows.
SELECT name, language, COUNT(*)
FROM emotion_concept
GROUP BY name, language
HAVING COUNT(*) > 1;

-- Informational: inspect language concentration rather than enforcing quotas.
SELECT language, COUNT(*) AS concept_count
FROM emotion_concept
GROUP BY language
ORDER BY concept_count DESC, language;

-- Before API-key initialization this may be 100. After initialization it must be 0.
SELECT COUNT(*) AS missing_embedding_count
FROM emotion_concept
WHERE embedding IS NULL;

-- After initialization this must return only 512.
SELECT DISTINCT vector_dims(embedding) AS embedding_dimensions
FROM emotion_concept
WHERE embedding IS NOT NULL;
