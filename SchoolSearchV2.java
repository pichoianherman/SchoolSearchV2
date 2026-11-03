import java.io.*;
import java.util.*;

class StudentRecord {
    String stLastname;
    String stFirstname;
    int grade;
    int classroom;
    int bus;

    public StudentRecord(String stLastname, String stFirstname, int grade, int classroom, int bus) {
        this.stLastname = stLastname;
        this.stFirstname = stFirstname;
        this.grade = grade;
        this.classroom = classroom;
        this.bus = bus;
    }
}

class TeacherRecord {
    String tLastname;
    String tFirstname;
    int classroom;

    public TeacherRecord(String tLastname, String tFirstname, int classroom) {
        this.tLastname = tLastname;
        this.tFirstname = tFirstname;
        this.classroom = classroom;
    }
}

public class SchoolSearchV2 {
    private static List<StudentRecord> students = new ArrayList<>();
    private static List<TeacherRecord> teachers = new ArrayList<>();

    public static void main(String[] args) {
        loadData();
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            long startTime = System.nanoTime();
            if (line.equalsIgnoreCase("Q") || line.equalsIgnoreCase("Quit")) {
                System.out.println("Quit bye!");
                break;
            }

            processQuery(line, startTime);
        }
        scanner.close();
    }

    private static void loadData() {
        // Зчитування файлу list.txt
        try (BufferedReader br = new BufferedReader(new FileReader("list.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    students.add(new StudentRecord(
                            parts[0].trim(),
                            parts[1].trim(),
                            Integer.parseInt(parts[2].trim()),
                            Integer.parseInt(parts[3].trim()),
                            Integer.parseInt(parts[4].trim())
                    ));
                }
            }
        } catch (IOException e) {
            System.out.println("Помилка завантаження list.txt: " + e.getMessage());
        }

        // Зчитування файлу teachers.txt
        try (BufferedReader br = new BufferedReader(new FileReader("teachers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    teachers.add(new TeacherRecord(
                            parts[0].trim(),
                            parts[1].trim(),
                            Integer.parseInt(parts[2].trim())
                    ));
                }
            }
        } catch (IOException e) {
            System.out.println("Помилка завантаження teachers.txt: " + e.getMessage());
        }
    }

    private static void processQuery(String query, long startTime) {
        String[] tokens = query.split("\\s+");
        String cmd = tokens[0].toUpperCase();

        if (cmd.startsWith("S")) {
            // S: <LastName> [B]
            if (tokens.length >= 2) {
                String lastName = tokens[1];
                boolean busOnly = tokens.length >= 3 && tokens[2].equalsIgnoreCase("B");

                StringBuilder sb = new StringBuilder();
                for (StudentRecord s : students) {
                    if (s.stLastname.equals(lastName)) {
                        TeacherRecord tr = findTeacherByClassroom(s.classroom);
                        if (busOnly) {
                            sb.append(s.stLastname).append(", ").append(s.stFirstname).append(", ").append(s.bus).append("\n");
                        } else {
                            String tLast = tr != null ? tr.tLastname : "";
                            String tFirst = tr != null ? tr.tFirstname : "";
                            sb.append(s.stLastname).append(", ").append(s.stFirstname).append(", ")
                                    .append(s.grade).append(", ").append(s.classroom).append(", ")
                                    .append(s.bus).append(", ").append(tLast).append(", ").append(tFirst).append("\n");
                        }
                    }
                }
                printResultAndTiming(sb.toString(), startTime);
            }
        } else if (cmd.startsWith("T")) {
            // T: <TeacherLastName>
            if (tokens.length >= 2) {
                String tLastName = tokens[1];
                List<Integer> classrooms = new ArrayList<>();
                for (TeacherRecord tr : teachers) {
                    if (tr.tLastname.equals(tLastName)) {
                        classrooms.add(tr.classroom);
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (StudentRecord s : students) {
                    if (classrooms.contains(s.classroom)) {
                        sb.append(s.stLastname).append(", ").append(s.stFirstname).append("\n");
                    }
                }
                printResultAndTiming(sb.toString(), startTime);
            }
        } else if (cmd.startsWith("B")) {
            // B: <BusNumber>
            if (tokens.length >= 2) {
                int busNum = Integer.parseInt(tokens[1]);
                StringBuilder sb = new StringBuilder();
                for (StudentRecord s : students) {
                    if (s.bus == busNum) {
                        sb.append(s.stLastname).append(", ").append(s.stFirstname).append(", ")
                                .append(s.grade).append(", ").append(s.classroom).append("\n");
                    }
                }
                printResultAndTiming(sb.toString(), startTime);
            }
        } else if (cmd.startsWith("C")) {
            // C: <Classroom> [TEACHER] або C: <Classroom>
            if (tokens.length >= 2) {
                int classNum = Integer.parseInt(tokens[1]);
                if (tokens.length >= 3 && tokens[2].toUpperCase().startsWith("T")) {
                    // Новий запит: C[lassroom]: <Number> T[eacher]
                    StringBuilder sb = new StringBuilder();
                    for (TeacherRecord tr : teachers) {
                        if (tr.classroom == classNum) {
                            sb.append(tr.tLastname).append(", ").append(tr.tFirstname).append("\n");
                        }
                    }
                    printResultAndTiming(sb.toString(), startTime);
                } else {
                    // Список учнів у класі
                    StringBuilder sb = new StringBuilder();
                    for (StudentRecord s : students) {
                        if (s.classroom == classNum) {
                            sb.append(s.stLastname).append(", ").append(s.stFirstname).append("\n");
                        }
                    }
                    printResultAndTiming(sb.toString(), startTime);
                }
            }
        } else if (cmd.startsWith("G")) {
            // G: <Grade> [TEACHER] або G: <Grade>
            if (tokens.length >= 2) {
                int gradeNum = Integer.parseInt(tokens[1]);
                if (tokens.length >= 3 && tokens[2].toUpperCase().startsWith("T")) {
                    // Новий запит: G[rade]: <Number> T[eacher]
                    Set<Integer> classrooms = new HashSet<>();
                    for (StudentRecord s : students) {
                        if (s.grade == gradeNum) {
                            classrooms.add(s.classroom);
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    for (TeacherRecord tr : teachers) {
                        if (classrooms.contains(tr.classroom)) {
                            sb.append(tr.tLastname).append(", ").append(tr.tFirstname).append("\n");
                        }
                    }
                    printResultAndTiming(sb.toString(), startTime);
                } else {
                    // Новий запит: G[rade]: <Number> (усі учні класу/паралелі)
                    StringBuilder sb = new StringBuilder();
                    for (StudentRecord s : students) {
                        if (s.grade == gradeNum) {
                            sb.append(s.stLastname).append(", ").append(s.stFirstname).append("\n");
                        }
                    }
                    printResultAndTiming(sb.toString(), startTime);
                }
            }
        }
    }

    private static TeacherRecord findTeacherByClassroom(int classroom) {
        for (TeacherRecord tr : teachers) {
            if (tr.classroom == classroom) {
                return tr;
            }
        }
        return null;
    }

    private static void printResultAndTiming(String result, long startTime) {
        if (!result.isEmpty()) {
            System.out.print(result);
        }
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println(durationMs + "ms");
    }
}