package hcmus.viet;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

class FolderMonitor implements Runnable {
    private WatchService watchService;
    private Path folder;

    private ChatClient chatClient;

    public FolderMonitor(String folderPath, ChatClient chatClient) throws IOException {
        this.folder = Paths.get(folderPath);
        this.chatClient = chatClient;
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    public void startMonitoring() throws IOException {
        registerFolderAndSubfolders(folder);

        System.out.println("Monitoring folder: " + folder.toString());

        while (true) {
            WatchKey watchKey;
            try {
                watchKey = watchService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            try {
                Thread.sleep(500); // Add a delay of 500 milliseconds before resetting the watch key
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            Path parentPath = (Path) watchKey.watchable();

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                Path filename = (Path) event.context();
                Path fullPath = parentPath.resolve(filename);

                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    if (Files.isDirectory(fullPath)) {
                        handleFolderCreated(fullPath);
                        // Register newly created subfolders for monitoring
                        registerFolderAndSubfolders(fullPath);
                    } else {
                        handleFileCreated(fullPath);
                    }
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    if (Files.isDirectory(fullPath)) {
                        handleFolderModified(fullPath);
                    } else {
                        handleFileModified(fullPath);
                    }
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    if (Files.isDirectory(fullPath)) {
                        handleFolderDeleted(fullPath);
                    } else {
                        handleFileDeleted(fullPath);
                    }
                }
            }

            boolean valid = watchKey.reset();
            if (!valid) {
                break;
            }
        }
    }

    private void registerFolderAndSubfolders(Path folder) throws IOException {
        if (!Files.exists(folder)) {
            return; // Folder doesn't exist, skip registration
        }
        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void handleFileCreated(Path filePath) {
        String detail = "File created: " + filePath;
        System.out.println(detail);
        ConnectPanel.addLog(detail + "\n");
        chatClient.sendMessage("1#" + detail);
    }

    private void handleFileModified(Path filePath) {
        String detail = "File modified: " + filePath;
        System.out.println(detail);
        ConnectPanel.addLog(detail + "\n");
        chatClient.sendMessage("3#" + detail);
    }

    private void handleFileDeleted(Path filePath) {
        String detail = "File deleted: " + filePath;
        System.out.println(detail);
        ConnectPanel.addLog(detail + "\n");
        chatClient.sendMessage("2#" + detail);
    }

    private void handleFolderCreated(Path folderPath) {
        String detail = "Folder created: " + folderPath;
        System.out.println(detail);
        ConnectPanel.addLog(detail + "\n");
        chatClient.sendMessage("1#" + detail);
    }

    private void handleFolderModified(Path folderPath) {
        String detail = "Folder modified: " + folderPath;
        System.out.println(detail);
        ConnectPanel.addLog(detail + "\n");
        chatClient.sendMessage("3#" + detail);
    }

    private void handleFolderDeleted(Path folderPath) {
        String detail = "Folder deleted: " + folderPath;
        System.out.println(detail);
        ConnectPanel.addLog(detail + "\n");
        chatClient.sendMessage("2#" + detail);
    }

    @Override
    public void run() {
        try {
            startMonitoring();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
