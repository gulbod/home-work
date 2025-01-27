import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.Month;
import java.time.Period;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.util.OptionalLong;

// 1. Основы LocalDate и LocalTime
class Main {
    public static void main(String[] args) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        System.out.println("Current Date and Time: " + currentDate.atTime(currentTime).format(formatter));

        // Вызоваю методы из других классов
        DateComparison.main(args);
        DaysUntilNewYear.main(args);
        LeapYearChecker.main(args);
        WeekendCounter.main(args);
        ExecutionTime.main(args);
        DateParsing.main(args);
        TimeZoneConverter.main(args);
        AgeCalculator.main(args);
        MonthlyCalendar.main(args);
        RandomDateGenerator.main(args);
        TimeUntilEvent.main(args);
        WorkingHoursCalculator.main(args);
        LocaleDateFormatter.main(args);
        DayOfWeekFinder.main(args);
    }
}

// 2. Сравнение дат
class DateComparison {
    public static String compareDates(LocalDate date1, LocalDate date2) {
        if (date1.isEqual(date2)) {
            return "Dates are equal.";
        } else if (date1.isBefore(date2)) {
            return "Date1 is before Date2.";
        } else {
            return "Date1 is after Date2.";
        }
    }

    public static void main(String[] args) {
        LocalDate date1 = LocalDate.of(2023, 10, 1);
        LocalDate date2 = LocalDate.of(2023, 10, 2);
        System.out.println(compareDates(date1, date2));
    }
}

// 3. Сколько дней до Нового года?
class DaysUntilNewYear {
    public static long daysUntilNewYear() {
        LocalDate today = LocalDate.now();
        LocalDate newYear = LocalDate.of(today.getYear() + 1, 1, 1);
        return today.until(newYear).getDays();
    }

    public static void main(String[] args) {
        System.out.println("Days until New Year: " + daysUntilNewYear());
    }
}

// 4. Проверка високосного года
class LeapYearChecker {
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    public static void main(String[] args) {
        int year = 2024;
        System.out.println("Is " + year + " a leap year? " + isLeapYear(year));
    }
}

// 5. Подсчет выходных за месяц
class WeekendCounter {
    public static int countWeekends(int year, Month month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        int weekends = 0;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7) {
                weekends++;
            }
        }
        return weekends;
    }

    public static void main(String[] args) {
        int year = 2023;
        Month month = Month.OCTOBER;
        System.out.println("Weekends in " + month + "/" + year + ": " + countWeekends(year, month));
    }
}

// 6. Расчет времени выполнения метода
class ExecutionTime {
    public static void measureExecutionTime(Runnable method) {
        long startTime = System.currentTimeMillis();
        method.run();
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
    }

    public static void main(String[] args) {
        measureExecutionTime(() -> {
            int i = 0;
            while (i < 1_000_000) {
                // Пустой цикл
                i++;
            }
        });
    }
}

// 7. Форматирование и парсинг даты
class DateParsing {
    public static LocalDate parseAndAddDays(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return date.plusDays(10);
    }

    public static void main(String[] args) {
        String inputDate = "01-10-2023";
        LocalDate newDate = parseAndAddDays(inputDate);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        System.out.println("Formatted Date: " + newDate.format(outputFormatter));
    }
}

// 8. Конвертация между часовыми поясами
class TimeZoneConverter {
    public static LocalDateTime convertUTCToZone(Instant utcTime, String zoneId) {
        return LocalDateTime.ofInstant(utcTime, ZoneId.of(zoneId));
    }

    public static void main(String[] args) {
        Instant utcTime = Instant.now();
        String zoneId = "Europe/Moscow";
        LocalDateTime moscowDateTime = convertUTCToZone(utcTime, zoneId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        System.out.println("UTC Time: " + utcTime.toString());
        System.out.println("Moscow Time: " + moscowDateTime.format(formatter));
    }
}

// 9. Вычисление возраста по дате рождения
class AgeCalculator {
    public static int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public static void main(String[] args) {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        System.out.println("Age: " + calculateAge(birthDate));
    }
}

// 10. Создание календаря на месяц
class MonthlyCalendar {
    public static void printCalendar(int year, Month month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dayType = (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7) ? "Weekend" : "Workday";
            System.out.println(date + " - " + dayType);
        }
    }

    public static void main(String[] args) {
        int year = 2023;
        Month month = Month.OCTOBER;
        printCalendar(year, month);
    }
}

// 11. Генерация случайной даты в диапазоне
class RandomDateGenerator {
    public static LocalDate generateRandomDate(LocalDate startDate, LocalDate endDate) {
        long startEpoch = startDate.toEpochDay();
        long endEpoch = endDate.toEpochDay();
        OptionalLong randomEpoch = ThreadLocalRandom.current().longs(startEpoch, endEpoch + 1).findAny();

        if (randomEpoch.isPresent()) {
            return LocalDate.ofEpochDay(randomEpoch.getAsLong());
        } else {
            throw new IllegalStateException("Failed to generate a random date within the specified range.");
        }
    }

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        LocalDate randomDate = generateRandomDate(startDate, endDate);
        System.out.println("Случайная дата: " + randomDate);
    }
}

// 12. Расчет времени до заданной даты
class TimeUntilEvent {
    public static String timeUntil(LocalDateTime eventTime) {
        Duration duration = Duration.between(LocalDateTime.now(), eventTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%d часов, %d минут, %d секунд", hours, minutes, seconds);
    }

    public static void main(String[] args) {
        LocalDateTime eventTime = LocalDateTime.of(2023, 12, 31, 23, 59, 59);
        System.out.println("Время до события: " + timeUntil(eventTime));
    }
}

// 13. Вычисление количества рабочих часов
class WorkingHoursCalculator {
    public static long calculateWorkingHours(LocalDateTime start, LocalDateTime end) {
        long workingHours = 0;

        for (LocalDateTime dateTime = start; dateTime.isBefore(end); dateTime = dateTime.plusHours(1)) {
            if (dateTime.getHour() >= 9 && dateTime.getHour() < 18 &&
                    dateTime.getDayOfWeek().getValue() < 6) { // Пн - Пт
                workingHours++;
            }
        }
        return workingHours;
    }

    public static void main(String[] args) {
        LocalDateTime start = LocalDateTime.of(2023, 12, 25, 9, 0);
        LocalDateTime end = LocalDateTime.of(2023, 12, 27, 17, 0);
        System.out.println("Количество рабочих часов: " + calculateWorkingHours(start, end));
    }
}

// 14. Конвертация даты в строку с учетом локали
class LocaleDateFormatter {
    public static String formatDate(LocalDate date, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", locale);
        return date.format(formatter);
    }

    public static void main(String[] args) {
        LocalDate date = LocalDate.now();
        for (String s : Arrays.asList("Дата в формате ru: " + formatDate(date, Locale.forLanguageTag("ru")), "Дата в формате en: " + formatDate(date, Locale.ENGLISH))) {
            System.out.println(s);
        }
    }
}

// 15. Определение дня недели по дате
class DayOfWeekFinder {
    public static String getDayOfWeek(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY -> {
                return "Понедельник";
            }
            case TUESDAY -> {
                return "Вторник";
            }
            case WEDNESDAY -> {
                return "Среда";
            }
            case THURSDAY -> {
                return "Четверг";
            }
            case FRIDAY -> {
                return "Пятница";
            }
            case SATURDAY -> {
                return "Суббота";
            }
            case SUNDAY -> {
                return "Воскресенье";
            }
            default -> {
                return "";
            }
        }
    }

    public static void main(String[] args) {
        LocalDate date = LocalDate.now();
        System.out.println("Сегодня: " + getDayOfWeek(date));
    }
}
