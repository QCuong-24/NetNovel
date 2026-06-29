package com.example.netnovel_server.audio.service;

import com.example.netnovel_server.audio.entity.AudioEncoding;
import com.example.netnovel_server.audio.exception.AudioGenerationException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.PollyException;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.TextType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PollyTtsService {

    private final PollyClient pollyClient;

    public PollyTtsService(PollyClient pollyClient) {
        this.pollyClient = pollyClient;
    }

    public byte[] synthesize(
        List<String> textChunks,
        String voiceName,
        String engine,
        BigDecimal speakingRate,
        BigDecimal pitch,
        AudioEncoding audioEncoding
    ) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (String textChunk : textChunks) {
                byte[] audioBytes = synthesizeChunk(textChunk, voiceName, engine, speakingRate, pitch, audioEncoding);
                outputStream.write(audioBytes);
            }

            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new AudioGenerationException("Could not combine audio chunks", exception);
        }
    }

    private byte[] synthesizeChunk(
        String text,
        String voiceName,
        String engine,
        BigDecimal speakingRate,
        BigDecimal pitch,
        AudioEncoding audioEncoding
    ) {
        boolean usesSsml = usesSsml(speakingRate, pitch);
        SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
            .text(usesSsml ? toSsml(text, speakingRate, pitch) : text)
            .textType(usesSsml ? TextType.SSML : TextType.TEXT)
            .voiceId(voiceName)
            .engine(engine)
            .outputFormat(OutputFormat.fromValue(audioEncoding.name().toLowerCase()))
            .build();

        try (ResponseInputStream<SynthesizeSpeechResponse> response = pollyClient.synthesizeSpeech(request)) {
            return response.readAllBytes();
        } catch (SdkClientException exception) {
            throw new AudioGenerationException("AWS Polly credentials or network are not configured correctly", exception);
        } catch (PollyException exception) {
            throw new AudioGenerationException("AWS Polly could not generate audio", exception);
        } catch (IOException exception) {
            throw new AudioGenerationException("Could not read AWS Polly audio response", exception);
        }
    }

    private boolean usesSsml(BigDecimal speakingRate, BigDecimal pitch) {
        return speakingRate.compareTo(BigDecimal.ONE) != 0 || pitch.compareTo(BigDecimal.ZERO) != 0;
    }

    private String toSsml(String text, BigDecimal speakingRate, BigDecimal pitch) {
        int ratePercent = speakingRate.multiply(new BigDecimal("100")).intValue();
        String pitchPercent = pitch.signum() >= 0 ? "+" + pitch.intValue() + "%" : pitch.intValue() + "%";

        return "<speak><prosody rate=\"" + ratePercent + "%\" pitch=\"" + pitchPercent + "\">"
            + escapeSsml(text)
            + "</prosody></speak>";
    }

    private String escapeSsml(String text) {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }
}
