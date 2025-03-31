import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;

public class FullSimulation {

    // Основной класс для запуска симуляции
    public static void main(String[] args) {
        Island island = new Island(100, 20);
        IslandSimulation simulation = new IslandSimulation(island);
        simulation.start();

        Runtime.getRuntime().addShutdownHook(new Thread(simulation::stop));
    }

    // Класс с настройками симуляции
    public static class SimulationSettings {
        public static final int PLANT_GROWTH_RATE = 10;
        public static final int SIMULATION_TICK_DELAY_MS = 1000;
        public static final int STATISTICS_PRINT_INTERVAL = 5000;
        public static final int INITIAL_PLANTS_PER_LOCATION = 5;
        public static final double INITIAL_ANIMAL_SPAWN_CHANCE = 0.3;
    }

    // Класс острова
    public static class Island {
        private final Location[][] locations;
        private final int width;
        private final int height;

        public Island(int width, int height) {
            this.width = width;
            this.height = height;
            this.locations = new Location[width][height];
            initializeLocations();
            populateIsland();
        }

        private void initializeLocations() {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    locations[x][y] = new Location(x, y, this);
                }
            }
        }

        private void populateIsland() {
            Random random = new Random();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Добавляем растения
                    for (int i = 0; i < SimulationSettings.INITIAL_PLANTS_PER_LOCATION; i++) {
                        locations[x][y].addPlant(new Plant());
                    }

                    // Добавляем животных с определенной вероятностью
                    if (random.nextDouble() < SimulationSettings.INITIAL_ANIMAL_SPAWN_CHANCE) {
                        spawnRandomAnimals(locations[x][y]);
                    }
                }
            }
        }

        private void spawnRandomAnimals(Location location) {
            Random random = new Random();
            Animal[] possibleAnimals = {
                    new Wolf(), new Boa(), new Fox(), new Bear(), new Eagle(),
                    new Horse(), new Deer(), new Rabbit(), new Mouse(), new Goat(),
                    new Sheep(), new Boar(), new Buffalo(), new Duck(), new Caterpillar()
            };

            int animalsToSpawn = random.nextInt(5) + 1;
            for (int i = 0; i < animalsToSpawn; i++) {
                Animal animal = possibleAnimals[random.nextInt(possibleAnimals.length)];
                location.addAnimal(animal);
            }
        }

        public Location getLocation(int x, int y) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                return locations[x][y];
            }
            return null;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }

    // Класс локации
    public static class Location {
        private final int x;
        private final int y;
        private final Island island;
        private final List<Animal> animals = new CopyOnWriteArrayList<>();
        private final List<Plant> plants = new CopyOnWriteArrayList<>();

        public Location(int x, int y, Island island) {
            this.x = x;
            this.y = y;
            this.island = island;
        }

        public synchronized void addAnimal(Animal animal) {
            long sameTypeCount = animals.stream()
                    .filter(a -> a.getClass() == animal.getClass())
                    .count();

            if (sameTypeCount < animal.getMaxPerCell()) {
                animals.add(animal);
                animal.setLocation(this);
            }
        }

        public synchronized void removeAnimal(Animal animal) {
            animals.remove(animal);
        }

        public synchronized void addPlant(Plant plant) {
            if (plants.size() < plant.getMaxPerCell()) {
                plants.add(plant);
                plant.setLocation(this);
            }
        }

        public synchronized void removePlant(Plant plant) {
            plants.remove(plant);
        }

        public List<Location> getAdjacentLocations() {
            List<Location> adjacent = new ArrayList<>();
            int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}}; // Только по горизонтали/вертикали

            for (int[] dir : directions) {
                Location loc = island.getLocation(x + dir[0], y + dir[1]);
                if (loc != null) {
                    adjacent.add(loc);
                }
            }
            return adjacent;
        }

        public List<Animal> getAnimals() { return Collections.unmodifiableList(animals); }
        public List<Plant> getPlants() { return Collections.unmodifiableList(plants); }
    }

    // Класс симуляции острова
    public static class IslandSimulation {
        private final Island island;
        private final ScheduledExecutorService scheduler;
        private final ExecutorService animalExecutor;
        private final Lock statisticsLock = new ReentrantLock();
        private volatile boolean isRunning = false;

        public IslandSimulation(Island island) {
            this.island = island;
            this.scheduler = Executors.newScheduledThreadPool(3);
            this.animalExecutor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors());
        }

        public void start() {
            if (!isRunning) {
                isRunning = true;
                scheduler.scheduleAtFixedRate(this::growPlants,
                        0, SimulationSettings.SIMULATION_TICK_DELAY_MS, TimeUnit.MILLISECONDS);
                scheduler.scheduleAtFixedRate(this::animalLifeCycle,
                        0, SimulationSettings.SIMULATION_TICK_DELAY_MS, TimeUnit.MILLISECONDS);
                scheduler.scheduleAtFixedRate(this::printStatistics,
                        0, SimulationSettings.STATISTICS_PRINT_INTERVAL, TimeUnit.MILLISECONDS);
            }
        }

        private void growPlants() {
            for (int x = 0; x < island.getWidth(); x++) {
                for (int y = 0; y < island.getHeight(); y++) {
                    if (ThreadLocalRandom.current().nextInt(100) < SimulationSettings.PLANT_GROWTH_RATE) {
                        island.getLocation(x, y).addPlant(new Plant());
                    }
                }
            }
        }

        private void animalLifeCycle() {
            List<Future<?>> futures = new ArrayList<>();

            for (int x = 0; x < island.getWidth(); x++) {
                for (int y = 0; y < island.getHeight(); y++) {
                    Location location = island.getLocation(x, y);
                    for (Animal animal : location.getAnimals()) {
                        futures.add(animalExecutor.submit(animal::liveCycle));
                    }
                }
            }

            // Ожидаем завершения всех задач
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error during animal life cycle: " + e.getMessage());
                }
            }
        }

        private void printStatistics() {
            statisticsLock.lock();
            try {
                Map<String, Integer> animalCounts = new HashMap<>();
                int totalPlants = 0;
                int totalAnimals = 0;

                for (int x = 0; x < island.getWidth(); x++) {
                    for (int y = 0; y < island.getHeight(); y++) {
                        Location loc = island.getLocation(x, y);
                        totalPlants += loc.getPlants().size();

                        for (Animal animal : loc.getAnimals()) {
                            String name = animal.getClass().getSimpleName();
                            animalCounts.merge(name, 1, Integer::sum);
                            totalAnimals++;
                        }
                    }
                }

                System.out.println("\n=== Island Statistics ===");
                System.out.printf("Total plants: %d | Total animals: %d%n", totalPlants, totalAnimals);
                System.out.println("Animals by type:");
                animalCounts.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(e -> System.out.printf("%-12s: %d%n", e.getKey(), e.getValue()));

                // Простая карта в консоли
                System.out.println("\nIsland map (sample 10x10):");
                for (int y = 0; y < 10; y++) {
                    for (int x = 0; x < 10; x++) {
                        Location loc = island.getLocation(x, y);
                        if (!loc.getAnimals().isEmpty()) {
                            System.out.print(loc.getAnimals().get(0).getUnicodeSymbol());
                        } else if (!loc.getPlants().isEmpty()) {
                            System.out.print("🌿");
                        } else {
                            System.out.print("·");
                        }
                    }
                    System.out.println();
                }
            } finally {
                statisticsLock.unlock();
            }
        }

        public void stop() {
            isRunning = false;
            scheduler.shutdown();
            animalExecutor.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                if (!animalExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    animalExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                animalExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("Simulation stopped");
        }
    }

    // Абстрактный класс для всех живых существ
    public static abstract class LivingEntity {
        protected double weight;
        protected int maxPerCell;
        protected double foodNeeded;
        protected Location location;
        protected double satiety;
        protected Lock lock = new ReentrantLock();

        public abstract void liveCycle();
        public abstract void die();

        public double getWeight() { return weight; }
        public int getMaxPerCell() { return maxPerCell; }
        public void setLocation(Location location) { this.location = location; }
    }

    // Класс для растений
    public static class Plant extends LivingEntity {
        public Plant() {
            this.weight = 1;
            this.maxPerCell = 200;
            this.foodNeeded = 0;
            this.satiety = 0;
        }

        @Override
        public void liveCycle() {
            // Растения просто существуют
        }

        @Override
        public void die() {
            if (location != null) {
                location.removePlant(this);
            }
        }
    }

    // Абстрактный класс для всех животных
    public static abstract class Animal extends LivingEntity {
        protected int maxSpeed;
        protected String unicodeSymbol;

        public Animal(double weight, int maxPerCell, int maxSpeed, double foodNeeded, String unicodeSymbol) {
            this.weight = weight;
            this.maxPerCell = maxPerCell;
            this.maxSpeed = maxSpeed;
            this.foodNeeded = foodNeeded;
            this.unicodeSymbol = unicodeSymbol;
            this.satiety = foodNeeded * 0.5; // Начальная сытость
        }

        @Override
        public void liveCycle() {
            lock.lock();
            try {
                move();
                eat();
                reproduce();
                satiety -= foodNeeded * 0.1; // Тратим энергию
                if (satiety <= 0) {
                    die();
                }
            } finally {
                lock.unlock();
            }
        }

        public abstract void eat();
        public abstract void reproduce();

        public void move() {
            if (maxSpeed == 0) return; // Для неподвижных животных

            int steps = ThreadLocalRandom.current().nextInt(maxSpeed) + 1;
            for (int i = 0; i < steps; i++) {
                List<Location> adjacent = location.getAdjacentLocations();
                if (!adjacent.isEmpty()) {
                    Location newLocation = adjacent.get(
                            ThreadLocalRandom.current().nextInt(adjacent.size()));

                    if (canMoveTo(newLocation)) {
                        location.removeAnimal(this);
                        newLocation.addAnimal(this);
                        location = newLocation;
                        break; // Перемещаемся только один раз за ход
                    }
                }
            }
        }

        protected boolean canMoveTo(Location location) {
            return true; // По умолчанию можно перемещаться везде
        }

        public String getUnicodeSymbol() {
            return unicodeSymbol;
        }

        @Override
        public void die() {
            if (location != null) {
                location.removeAnimal(this);
            }
        }
    }

    // Абстрактный класс для хищников
    public static abstract class Predator extends Animal {
        protected Map<Class<? extends Animal>, Integer> preyChances = new HashMap<>();

        public Predator(double weight, int maxPerCell, int maxSpeed, double foodNeeded, String unicodeSymbol) {
            super(weight, maxPerCell, maxSpeed, foodNeeded, unicodeSymbol);
        }

        @Override
        public void eat() {
            location.getAnimals().stream()
                    .filter(a -> preyChances.containsKey(a.getClass()))
                    .findFirst()
                    .ifPresent(prey -> {
                        int chance = preyChances.get(prey.getClass());
                        if (ThreadLocalRandom.current().nextInt(100) < chance) {
                            satiety = Math.min(satiety + prey.getWeight(), foodNeeded);
                            prey.die();
                        }
                    });
        }

        @Override
        public void reproduce() {
            if (satiety < foodNeeded * 0.7) return;

            long potentialMates = location.getAnimals().stream()
                    .filter(a -> a.getClass() == this.getClass())
                    .count();

            if (potentialMates >= 2) {
                int offspringCount = ThreadLocalRandom.current().nextInt(3) + 1;
                for (int i = 0; i < offspringCount; i++) {
                    try {
                        Animal offspring = this.getClass().getConstructor().newInstance();
                        location.addAnimal(offspring);
                    } catch (Exception e) {
                        System.err.println("Error creating offspring: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Абстрактный класс для травоядных
    public static abstract class Herbivore extends Animal {
        public Herbivore(double weight, int maxPerCell, int maxSpeed, double foodNeeded, String unicodeSymbol) {
            super(weight, maxPerCell, maxSpeed, foodNeeded, unicodeSymbol);
        }

        @Override
        public void eat() {
            List<Plant> plants = location.getPlants();
            if (!plants.isEmpty()) {
                Plant plant = plants.get(0);
                satiety = Math.min(satiety + plant.getWeight(), foodNeeded);
                plant.die();
            }
        }

        @Override
        public void reproduce() {
            if (satiety < foodNeeded * 0.5) return;

            long potentialMates = location.getAnimals().stream()
                    .filter(a -> a.getClass() == this.getClass())
                    .count();

            if (potentialMates >= 2) {
                int offspringCount = ThreadLocalRandom.current().nextInt(4) + 1;
                for (int i = 0; i < offspringCount; i++) {
                    try {
                        Animal offspring = this.getClass().getConstructor().newInstance();
                        location.addAnimal(offspring);
                    } catch (Exception e) {
                        System.err.println("Error creating offspring: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Конкретные классы хищников
    public static class Wolf extends Predator {
        public Wolf() {
            super(50, 30, 3, 8, "🐺");
            preyChances.put(Rabbit.class, 60);
            preyChances.put(Deer.class, 15);
            preyChances.put(Horse.class, 10);
        }
    }

    public static class Boa extends Predator {
        public Boa() {
            super(15, 30, 1, 3, "🐍");
            preyChances.put(Rabbit.class, 40);
            preyChances.put(Mouse.class, 80);
        }
    }

    public static class Fox extends Predator {
        public Fox() {
            super(8, 30, 2, 2, "🦊");
            preyChances.put(Rabbit.class, 70);
            preyChances.put(Mouse.class, 90);
            preyChances.put(Duck.class, 60);
        }
    }

    public static class Bear extends Predator {
        public Bear() {
            super(500, 5, 2, 80, "🐻");
            preyChances.put(Rabbit.class, 80);
            preyChances.put(Deer.class, 40);
            preyChances.put(Boar.class, 50);
        }
    }

    public static class Eagle extends Predator {
        public Eagle() {
            super(6, 20, 3, 1, "🦅");
            preyChances.put(Rabbit.class, 20);
            preyChances.put(Mouse.class, 90);
            preyChances.put(Fox.class, 10);
        }
    }

    // Конкретные классы травоядных
    public static class Horse extends Herbivore {
        public Horse() {
            super(400, 20, 4, 60, "🐎");
        }
    }

    public static class Deer extends Herbivore {
        public Deer() {
            super(300, 20, 4, 50, "🦌");
        }
    }

    public static class Rabbit extends Herbivore {
        public Rabbit() {
            super(2, 150, 2, 0.45, "🐇");
        }
    }

    public static class Mouse extends Herbivore {
        public Mouse() {
            super(0.05, 500, 1, 0.01, "🐁");
        }
    }

    public static class Goat extends Herbivore {
        public Goat() {
            super(60, 140, 3, 10, "🐐");
        }
    }

    public static class Sheep extends Herbivore {
        public Sheep() {
            super(70, 140, 3, 15, "🐑");
        }
    }

    public static class Boar extends Herbivore {
        public Boar() {
            super(400, 50, 2, 50, "🐗");
        }
    }

    public static class Buffalo extends Herbivore {
        public Buffalo() {
            super(700, 10, 3, 100, "🐃");
        }
    }

    public static class Duck extends Herbivore {
        public Duck() {
            super(1, 200, 4, 0.15, "🦆");
        }

        @Override
        public void eat() {
            super.eat(); // Сначала пробуем растения

            if (satiety < foodNeeded * 0.7) {
                List<Animal> caterpillars = location.getAnimals().stream()
                        .filter(a -> a instanceof Caterpillar)
                        .collect(Collectors.toList());

                if (!caterpillars.isEmpty()) {
                    Animal caterpillar = caterpillars.get(0);
                    satiety += caterpillar.getWeight();
                    caterpillar.die();
                }
            }
        }
    }

    public static class Caterpillar extends Herbivore {
        public Caterpillar() {
            super(0.01, 1000, 0, 0, "🐛");
        }

        @Override
        public void move() {} // Не двигается

        @Override
        public void eat() {
            List<Plant> plants = location.getPlants();
            if (!plants.isEmpty() && ThreadLocalRandom.current().nextInt(10) == 0) {
                satiety += plants.get(0).getWeight() * 0.1;
                plants.get(0).die();
            }
        }

        @Override
        public void reproduce() {
            if (satiety >= foodNeeded * 0.3) {
                int offspringCount = ThreadLocalRandom.current().nextInt(10) + 5;
                for (int i = 0; i < offspringCount; i++) {
                    try {
                        location.addAnimal(new Caterpillar());
                    } catch (Exception e) {
                        System.err.println("Error creating offspring: " + e.getMessage());
                    }
                }
            }
        }
    }
}
