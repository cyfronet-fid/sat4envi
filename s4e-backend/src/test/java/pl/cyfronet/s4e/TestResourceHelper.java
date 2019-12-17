package pl.cyfronet.s4e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResourceHelper {
    private final ResourceLoader resourceLoader;

    public byte[] getAsBytes(String path) {
        try (InputStream inputStream = resourceLoader.getResource(path).getInputStream()) {
            return inputStream.readAllBytes();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            log.warn("Couldn't read file at path '" + path + "'", e);
            throw new RuntimeException(e);
        }
    }

    public String getAsStringInBase64(String path) {
        return Base64.getEncoder().encodeToString(getAsBytes(path));
    }
}
