package com.logscanner.service;

import com.logscanner.model.ErrorReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class LogScannerService {

    private static final Pattern ERROR_PATTERN =
            Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}.*)\\s+(ERROR|FATAL)\\s+(.*)$");

    // Matches "Caused by:" lines to find root cause
    private static final Pattern CAUSED_BY_PATTERN =
            Pattern.compile("^\\s*Caused by:\\s*(.+)$");

    // Matches stack trace lines (starting with whitespace + at)
    private static final Pattern STACK_TRACE_LINE =
            Pattern.compile("^\\s+at .+$");

    public ErrorReport scanLogFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        List<String> lines = Files.readAllLines(path);

        List<ErrorReport.ErrorEntry> errors = new ArrayList<>();
        int lineNumber = 0;

        while (lineNumber < lines.size()) {
            String line = lines.get(lineNumber);
            Matcher errorMatcher = ERROR_PATTERN.matcher(line);

            if (errorMatcher.matches()) {
                String severity  = errorMatcher.group(2);
                String errorBody = errorMatcher.group(3);

                // Extract exception type from the message
                String errorType    = extractExceptionType(errorBody);
                String errorMessage = errorBody;

                // Collect stack trace lines + look for "Caused by:"
                List<String> stackTrace = new ArrayList<>();
                String rootCause = null;
                int nextLine = lineNumber + 1;

                while (nextLine < lines.size()) {
                    String candidate = lines.get(nextLine);
                    Matcher causedBy = CAUSED_BY_PATTERN.matcher(candidate);

                    if (causedBy.matches()) {
                        rootCause = causedBy.group(1).trim();
                        stackTrace.add(candidate.strip());
                        nextLine++;
                        // Consume the stack frames of the Caused By block
                        while (nextLine < lines.size()
                                && STACK_TRACE_LINE.matcher(lines.get(nextLine)).matches()) {
                            stackTrace.add(lines.get(nextLine).strip());
                            nextLine++;
                        }
                    } else if (STACK_TRACE_LINE.matcher(candidate).matches()) {
                        stackTrace.add(candidate.strip());
                        nextLine++;
                    } else {
                        break; // not a stack-trace line — stop collecting
                    }
                }

                // Fallback: treat the error message itself as the root cause
                if (rootCause == null) {
                    rootCause = errorMessage;
                }

                errors.add(ErrorReport.ErrorEntry.builder()
                        .lineNumber(lineNumber + 1)
                        .severity(severity)
                        .errorType(errorType)
                        .errorMessage(errorMessage)
                        .rootCause(rootCause)
                        .stackTrace(stackTrace)
                        .build());

                lineNumber = nextLine; // jump past consumed lines
            } else {
                lineNumber++;
            }
        }

        log.info("Scanned [{}] — found {} error(s)", filePath, errors.size());

        return ErrorReport.builder()
                .logFilePath(filePath)
                .scannedAt(LocalDateTime.now())
                .totalErrors(errors.size())
                .errors(errors)
                .build();

    }

    // Pull the exception class name from strings like
    // "java.lang.NullPointerException: some message"
    private String extractExceptionType(String message) {
        if (message == null) return "UnknownException";
        int colon = message.indexOf(':');
        String candidate = (colon > 0) ? message.substring(0, colon).trim() : message.trim();
        // Keep only the simple class name (last segment after '.')
        int dot = candidate.lastIndexOf('.');
        return (dot >= 0 && dot < candidate.length() - 1)
                ? candidate.substring(dot + 1)
                : candidate;
    }
}
