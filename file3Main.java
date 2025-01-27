import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {
    // Задание 1: Работа с потоками ввода-вывода
    public static void fileIOExample() {
        String inputFile = "input.txt";
        String outputFile = "output.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String upperCaseLine = line.toUpperCase();
                writer.write(upperCaseLine);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    // Задание 2: Реализация паттерна Декоратор
    interface TextProcessor {
        String process(String text);
    }

    static class SimpleTextProcessor implements TextProcessor {
        @Override
        public String process(String text) {
            return text;
        }
    }

    static abstract class TextProcessorDecorator implements TextProcessor {
        protected TextProcessor textProcessor;

        public TextProcessorDecorator(TextProcessor textProcessor) {
            this.textProcessor = textProcessor;
        }

        @Override
        public String process(String text) {
            return textProcessor.process(text);
        }
    }

    static class UpperCaseDecorator extends TextProcessorDecorator {
        public UpperCaseDecorator(TextProcessor textProcessor) {
            super(textProcessor);
        }

        @Override
        public String process(String text) {
            return super.process(text).toUpperCase();
        }
    }

    static class TrimDecorator extends TextProcessorDecorator {
        public TrimDecorator(TextProcessor textProcessor) {
            super(textProcessor);
        }

        @Override
        public String process(String text) {
            return super.process(text).trim();
        }
    }

    static class ReplaceDecorator extends TextProcessorDecorator {
        public ReplaceDecorator(TextProcessor textProcessor) {
            super(textProcessor);
        }

        @Override
        public String process(String text) {
            return super.process(text).replace(" ", "_");
        }
    }

    public static void textProcessorExample() {
        TextProcessor processor = new ReplaceDecorator(
                new UpperCaseDecorator(
                        new TrimDecorator(new SimpleTextProcessor())
                )
        );
        String result = processor.process(" Hello world ");
        System.out.println(result);
    }

    // Задание 3: Сравнение производительности IO и NIO
    public static void ioVsNIO() {
        String inputFile = "largefile.txt";
        String outputFileIO = "output_io.txt";
        String outputFileNIO = "output_nio.txt";

        // IO
        long startTimeIO = System.currentTimeMillis();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileIO))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        long endTimeIO = System.currentTimeMillis();
        System.out.println("IO Time: " + (endTimeIO - startTimeIO) + " ms");

        // NIO
        long startTimeNIO = System.currentTimeMillis();
        try (FileInputStream inStream = new FileInputStream(inputFile);
             FileOutputStream outStream = new FileOutputStream(outputFileNIO);
             FileChannel inChannel = inStream.getChannel();
             FileChannel outChannel = outStream.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (inChannel.read(buffer) > 0) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        long endTimeNIO = System.currentTimeMillis();
        System.out.println("NIO Time: " + (endTimeNIO - startTimeNIO) + " ms");
    }

    // Задание 4: Программа с использованием Java NIO
    public static void nioFileCopy() {
        String sourceFile = "largefile.txt";
        String destFile = "copied_largefile.txt";

        try (FileInputStream inStream = new FileInputStream(sourceFile);
             FileOutputStream outStream = new FileOutputStream(destFile);
             FileChannel srcChannel = inStream.getChannel();
             FileChannel destChannel = outStream.getChannel()) {
            srcChannel.transferTo(0, srcChannel.size(), destChannel);
            System.out.println("File copied successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    // Задание 5: Асинхронное чтение файла с использованием NIO.2
    public static void asyncFileRead() {
        Path filePath = Paths.get("largefile.txt");

        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            long position = 0;

            fileChannel.read(buffer, position, buffer, new CompletionHandler<>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    if (result == -1) {
                        System.out.println("End of file reached.");
                        return;
                    }
                    attachment.flip();
                    byte[] data = new byte[attachment.remaining()];
                    attachment.get(data);
                    System.out.println(new String(data));
                    attachment.clear();
                    fileChannel.read(attachment, position, attachment, this);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.err.println("An error occurred: " + exc.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        fileIOExample();
        textProcessorExample();
        ioVsNIO();
        nioFileCopy();
        asyncFileRead();
    }
}
