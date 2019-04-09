package io.github.semlink.propbank.frames;

import com.google.common.base.Stopwatch;

import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import lombok.extern.slf4j.Slf4j;


/**
 * {@link Frameset PropBank Frameset} factory.
 *
 * @author jgung
 */
@Slf4j
@XmlRegistry
public class FramesetFactory {

    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Read {@link FramesetFactory PropBank frame files} at a given directory.
     *
     * @param directory frame directory
     * @param extension frame extension (e.g. ".xml")
     * @return list of frame files
     */
    public static List<Frameset> readFramesets(Path directory, String extension) {
        try {
            List<Frameset> inventories = Files.walk(directory, Integer.MAX_VALUE)
                    .filter(f -> f.toString().endsWith(extension))
                    .map(path -> {
                        try {
                            return readFrameset(Files.newInputStream(path));
                        } catch (Exception e) {
                            throw new RuntimeException("Error reading frame file at " + path.toString(), e);
                        }
                    })
                    .collect(Collectors.toList());
            log.debug("Read {} frame files at {}", inventories.size(), directory.toString());
            return inventories;
        } catch (Exception e) {
            throw new RuntimeException("Error reading frame files at " + directory.toString(), e);
        }
    }

    /**
     * Read a list of {@link Frameset PropBank frame files} at a given directory.
     *
     * @param directory inventory directory
     * @return list of PropBank frame files
     */
    public static List<Frameset> readFramesets(Path directory) {
        return readFramesets(directory, ".xml");
    }

    /**
     * Read a single PropBank frame file
     *
     * @param inputStream frame file input stream
     * @return PropBank frame file
     */
    public static Frameset readFrameset(InputStream inputStream) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature(LOAD_EXTERNAL_DTD, false);
            SAXSource source = new SAXSource(parserFactory.newSAXParser().getXMLReader(), new InputSource(inputStream));
            return (Frameset) JAXBContext.newInstance(Frameset.class, Example.ExampleRelation.class, Example.ExampleRelation.class)
                    .createUnmarshaller().unmarshal(source);
        } catch (Exception e) {
            throw new RuntimeException("Error reading frame file", e);
        }
    }

    /**
     * Read a list of java-serialized {@link Frameset PropBank frame files}.
     *
     * @param inputStream serialized frame files input stream
     * @return list of frame files
     */
    public static List<Frameset> deserializeFrames(InputStream inputStream) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            //noinspection unchecked
            return (List<Frameset>) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException("An error occurred reading serialized frame files", e);
        }
    }

    public static void main(String[] args) throws IOException {
        String outPath = args[1];

        Stopwatch stopwatch = Stopwatch.createStarted();
        // read frames
        List<Frameset> frames = readFramesets(Paths.get(args[0]));
        log.info("Read {} frames in {}", frames.size(), stopwatch);

        // serialize frames
        try (FileOutputStream fos = new FileOutputStream(outPath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(frames);
        }
        try (FileInputStream fis = new FileInputStream(outPath)) {
            Stopwatch sw = Stopwatch.createStarted();
            List<Frameset> deserialized = deserializeFrames(fis);
            log.info("Read {} frames in {}", deserialized.size(), sw);
        }
    }

}
