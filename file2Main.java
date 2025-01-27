import java.util.ArrayList;
import java.util.List;

// Задача 1: Класс базы данных (Singleton)
@SuppressWarnings("all")
class DatabaseConnection {
    private static DatabaseConnection instance;

    private DatabaseConnection() {
        System.out.println("Создано подключение к базе данных.");
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}
class MainDatabase {
    public static void main(String[] args) {
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        DatabaseConnection db2 = DatabaseConnection.getInstance();

        System.out.println("db1 и db2 ссылаются на один и тот же объект: " + (db1 == db2));
    }
}

// Задача 2: Логирование в системе (Singleton)
@SuppressWarnings("all")
class Logger {
    private static Logger instance;
    private final List<String> logs;

    private Logger() {
        logs = new ArrayList<>();
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void addLog(String message) {
        logs.add(message);
    }

    public void printLogs() {
        for (String log : logs) {
            System.out.println(log);
        }
    }
}
class MainLogger {
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        logger.addLog("Первое сообщение лога.");
        logger.addLog("Второе сообщение лога.");

        logger.printLogs();
    }
}

// Задача 3: Реализация статусов заказа (Enum)
enum OrderStatus {
    NEW, IN_PROGRESS, DELIVERED, CANCELLED
}

class Order {
    private OrderStatus status;

    public Order() {
        this.status = OrderStatus.NEW;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void changeStatus(OrderStatus newStatus) {
        if (status == OrderStatus.DELIVERED && newStatus == OrderStatus.CANCELLED) {
            System.out.println("Нельзя отменить доставленный заказ.");
            return;
        }
        this.status = newStatus;
        System.out.println("Статус заказа изменен на: " + status);
    }
}
class MainOrder {
    public static void main(String[] args) {
        Order order = new Order();
        System.out.println("Текущий статус заказа: " + order.getStatus());

        order.changeStatus(OrderStatus.IN_PROGRESS);
        order.changeStatus(OrderStatus.DELIVERED);
        order.changeStatus(OrderStatus.CANCELLED); // Это должно вывести сообщение об ошибке
    }
}

// Задача 4: Сезоны года (Enum)
enum Season {
    WINTER, SPRING, SUMMER, AUTUMN
}

class SeasonTranslator {
    public static String getSeasonName(Season season) {
        return switch (season) {
            case WINTER -> "Зима";
            case SPRING -> "Весна";
            case SUMMER -> "Лето";
            case AUTUMN -> "Осень";
        };
    }
}
class MainSeason {
    public static void main(String[] args) {
        Season season = Season.SUMMER;
        System.out.println("Название сезона: " + SeasonTranslator.getSeasonName(season));
    }
}
