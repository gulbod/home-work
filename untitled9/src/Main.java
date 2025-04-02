public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        // Создаем остров и запускаем симуляцию
        Island island = new Island(100, 20);
        IslandSimulation simulation = new IslandSimulation(island);
        simulation.start();

        // Добавляем хук для остановки симуляции при завершении программы
        Runtime.getRuntime().addShutdownHook(new Thread(simulation::stop));

        // Ждем некоторое время, чтобы увидеть вывод статистики
        try {
            Thread.sleep(20000); // Ждем 20 секунд
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Останавливаем симуляцию
        simulation.stop();
    }
}
