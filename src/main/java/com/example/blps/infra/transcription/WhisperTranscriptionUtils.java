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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Для запуска компонента необходимо установить модель локально и указать к ней путь в WHISPER_MODEL_PATH
 * @link <a href="https://github.com/ggml-org/whisper.cpp?tab=readme-ov-file#quick-start">Здесь инструкция по установке base.en</a>
 * Кроме того необходима утилита ffmpeg в PATH
 * @link <a href="https://github.com/Thefrank/ffmpeg-static-freebsd/releases/tag/v7.1">Статический билд для Helios</a>
 */
@Slf4j
@Component
public class WhisperTranscriptionUtils {
    private final String modelPath;

    public WhisperTranscriptionUtils(
            @Value("${whisper.model_path}") String modelPath
    ) {
        this.modelPath = modelPath;
    }

    public String getTranscription(InputStream videoStream) {
        Path tmpVideo = null;
        Path tmpWav = null;
        try {
            tmpVideo = Files.createTempFile("video", ".mp4");
            Files.copy(videoStream, tmpVideo, StandardCopyOption.REPLACE_EXISTING);

            tmpWav = Files.createTempFile("audio", ".wav");
            ProcessBuilder ffmpegPb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", tmpVideo.toAbsolutePath().toString(),
                    "-ar", "16000",
                    "-ac", "1",
                    "-c:a", "pcm_s16le",
                    tmpWav.toAbsolutePath().toString()
            );
            ffmpegPb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process ffmpeg = ffmpegPb.start();
            if (ffmpeg.waitFor() != 0) {
                throw new TranscriptionException("ffmpeg failed with exit code " + ffmpeg.exitValue());
            }

            ProcessBuilder whisperPb = new ProcessBuilder(
                    "whisper-cli",
                    "-m", modelPath,
                    "-l", "en",
                    "-t", "8",
                    "-f", tmpWav.toAbsolutePath().toString()
            );
            whisperPb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process whisper = whisperPb.start();

            StringBuilder transcription = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(whisper.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    transcription.append(line).append("\n");
                }
            }

            if (whisper.waitFor() != 0) {
                throw new TranscriptionException("whisper-cli failed with exit code " + whisper.exitValue());
            }

            return transcription.toString().trim();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Transcription failed", e);
        } finally {
            try {
                if (tmpVideo != null) Files.deleteIfExists(tmpVideo);
            } catch (IOException ignore) {
            }
            try {
                if (tmpWav != null) Files.deleteIfExists(tmpWav);
            } catch (IOException ignore) {
            }
        }
    }
}