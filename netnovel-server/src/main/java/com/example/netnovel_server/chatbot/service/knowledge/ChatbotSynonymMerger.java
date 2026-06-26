package com.example.netnovel_server.chatbot.service.knowledge;

import com.example.netnovel_server.chatbot.model.ChatbotSynonyms;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Merges tag aliases from tags.txt into chatbot synonyms.
 *
 * For every tag line, it keeps existing aliases, adds the English tag,
 * adds Vietnamese translations, and also adds Vietnamese no-diacritic aliases
 * so queries like "tu tien" can still match "tu tiên".
 */
@Component
public class ChatbotSynonymMerger {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    public ChatbotSynonyms mergeTagSynonyms(ChatbotSynonyms baseSynonyms, List<String> tagLines) {
        Map<String, List<String>> mergedTags = new LinkedHashMap<>(baseSynonyms.tags());

        tagLines.stream()
            .map(String::trim)
            .filter(line -> !line.isBlank())
            .forEach(line -> mergeTagLine(mergedTags, line));

        return new ChatbotSynonyms(
            baseSynonyms.genres(),
            baseSynonyms.statuses(),
            baseSynonyms.sorts(),
            mergedTags,
            baseSynonyms.searchWords(),
            baseSynonyms.authorWords(),
            baseSynonyms.titleWords(),
            baseSynonyms.stopwords()
        );
    }

    private void mergeTagLine(Map<String, List<String>> mergedTags, String line) {
        int separatorIndex = line.indexOf(':');
        if (separatorIndex <= 0) {
            return;
        }

        String tag = line.substring(0, separatorIndex).trim();
        String translation = line.substring(separatorIndex + 1).trim();
        if (tag.isBlank()) {
            return;
        }

        LinkedHashSet<String> aliases = new LinkedHashSet<>(mergedTags.getOrDefault(tag, List.of()));
        addAlias(aliases, tag);
        if (!translation.isBlank()) {
            for (String part : translation.split("/")) {
                addAlias(aliases, part.trim());
            }
        }

        mergedTags.put(tag, new ArrayList<>(aliases));
    }

    private void addAlias(LinkedHashSet<String> aliases, String alias) {
        if (alias == null || alias.isBlank()) {
            return;
        }

        aliases.add(alias);

        String asciiAlias = stripVietnameseMarks(alias);
        if (!asciiAlias.equalsIgnoreCase(alias)) {
            aliases.add(asciiAlias);
        }
    }

    private String stripVietnameseMarks(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized)
            .replaceAll("")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ")
            .trim();
    }
}
