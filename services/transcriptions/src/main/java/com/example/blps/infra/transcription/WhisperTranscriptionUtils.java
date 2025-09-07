package com.example.blps.infra.transcription;

import com.example.blps.exception.TranscriptionException;
import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperJNI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Для запуска компонента необходимо установить модель локально и указать тип модели WHISPER_MODEL_TYPE
 * @link <a href="https://github.com/ggml-org/whisper.cpp?tab=readme-ov-file#quick-start">Здесь инструкция по установке base.en</a>
 * @link <a href="https://github.com/ggml-org/whisper.cpp/blob/master/models/README.md#available-models">Доступные модели</a>
 * Кроме того необходима утилита ffmpeg в PATH
 * @link <a href="https://github.com/Thefrank/ffmpeg-static-freebsd/releases/tag/v7.1">Статический билд для Helios</a>
 */
@Slf4j
@Component
public class WhisperTranscriptionUtils {
    private final WhisperJNI whisper;
    private final WhisperFullParams transcriptionParams;
    private final WhisperContext ctx;

    public WhisperTranscriptionUtils(@Value("${whisper.model_type}") String modelType) throws IOException, URISyntaxException {
        WhisperJNI.loadLibrary();
        WhisperJNI.setLibraryLogger(null);

        this.whisper = new WhisperJNI();

        String model = "ggml-%s.bin".formatted(modelType.toLowerCase());

        this.ctx = whisper.init(Path.of(getClass().getClassLoader().getResource(model).toURI()));

        var params = new WhisperFullParams();
        params.language = "en";
        params.nThreads = 8;

        this.transcriptionParams = params;
    }

    public String getTranscription(InputStream file) {
        Path tmpFile;

        try {
            tmpFile = Files.createTempFile("video", ".mp4");
            Files.copy(file, tmpFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new TranscriptionException("Could not create file", e);
        }

        float[] samples = convertToFloatArray(tmpFile);

        int result = this.whisper.full(ctx, this.transcriptionParams, samples, samples.length);

        if (result != 0) {
            throw new TranscriptionException("Transcription failed with code " + result);
        }

        int numSegments = whisper.fullNSegments(ctx);
        StringBuilder transcription = new StringBuilder();

        for (int i = 0; i < numSegments; i++) {
            String text = whisper.fullGetSegmentText(ctx, i);
            transcription.append(text).append("\n");
        }

        try { Files.deleteIfExists(tmpFile); } catch (IOException ignore) {}

        return transcription.toString();
    }

    public static float[] convertToFloatArray(Path file) {
        List<Float> samples = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", file.toAbsolutePath().toString(),
                "-ac", "1",              // моно
                "-ar", "16000",          // 16kHz
                "-f", "f32le",           // float32 little endian
                "pipe:1"
        );
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            Process process = pb.start();

            try (InputStream is = process.getInputStream()) {
                byte[] buffer = new byte[4096 * 4];
                int read;
                ByteBuffer bb = ByteBuffer.allocate(buffer.length).order(ByteOrder.LITTLE_ENDIAN);
                while ((read = is.read(buffer)) != -1) {
                    bb.clear();
                    bb.put(buffer, 0, read);
                    bb.flip();
                    FloatBuffer fb = bb.asFloatBuffer();
                    while (fb.hasRemaining()) {
                        samples.add(fb.get());
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new TranscriptionException("ffmpeg exited with code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            throw new TranscriptionException("Failed to convert MP4 to float[]", e);
        }

        float[] result = new float[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            result[i] = samples.get(i);
        }
        return result;
    }
}