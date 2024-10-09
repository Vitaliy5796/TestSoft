package ru.sidorov;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar GroupLines.jar <file_path>");
            return;
        }

        String filePath = args[0];

        long startTime = System.currentTimeMillis();

        try {
            Set<String> uniqueLines = Files.lines(Paths.get(filePath))
                    .filter(line -> line.matches("^\"\\d+\"(;\"\\d+\")*$")) // Фильтрация корректных строк
                    .collect(Collectors.toSet());

            // Построение графа для группировки строк
            Map<String, Set<String>> valueToLines = new HashMap<>();
            for (String line : uniqueLines) {
                String[] values = line.split(";");
                for (String value : values) {
                    if (!value.isEmpty()) {
                        valueToLines
                                .computeIfAbsent(value, k -> new HashSet<>())
                                .add(line);
                    }
                }
            }

            List<Set<String>> groups = new ArrayList<>();
            Set<String> visited = new HashSet<>();

            for (String line : uniqueLines) {
                if (!visited.contains(line)) {
                    Set<String> group = new HashSet<>();
                    Queue<String> queue = new LinkedList<>();
                    queue.add(line);

                    while (!queue.isEmpty()) {
                        String currentLine = queue.poll();
                        if (!visited.contains(currentLine)) {
                            visited.add(currentLine);
                            group.add(currentLine);
                            String[] values = currentLine.split(";");

                            for (String value : values) {
                                if (!value.isEmpty()) {
                                    Set<String> connectedLines = valueToLines.get(value);
                                    if (connectedLines != null) {
                                        for (String connectedLine : connectedLines) {
                                            if (!visited.contains(connectedLine)) {
                                                queue.add(connectedLine);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (group.size() > 1) {
                        groups.add(group);
                    }
                }
            }

            groups.sort((g1, g2) -> Integer.compare(g2.size(), g1.size()));

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("output.txt"))) {
                writer.write("Number of groups with more than one element: " + groups.size() + "\n");
                for (int i = 0; i < groups.size(); i++) {
                    writer.write("Group " + (i + 1) + "\n");
                    for (String groupLine : groups.get(i)) {
                        writer.write(groupLine + "\n");
                    }
                    writer.write("\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) / 1000 + "s");
    }
}