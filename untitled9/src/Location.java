import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Location {
    private final int x;
    private final int y;
    private final Island island;
    private final List<Animal> animals = new CopyOnWriteArrayList<>();
    private final List<Plant> plants = new CopyOnWriteArrayList<>();
    private final Lock lock = new ReentrantLock();

    public Location(int x, int y, Island island) {
        this.x = x;
        this.y = y;
        this.island = Objects.requireNonNull(island, "Island cannot be null");
    }

    public void addAnimal(Animal animal) {
        lock.lock();
        try {
            long sameTypeCount = animals.stream()
                    .filter(a -> a.getClass() == animal.getClass())
                    .count();

            if (sameTypeCount < animal.getMaxPerCell()) {
                animals.add(animal);
                animal.setLocation(this);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeAnimal(Animal animal) {
        lock.lock();
        try {
            animals.remove(animal);
        } finally {
            lock.unlock();
        }
    }

    public void addPlant(Plant plant) {
        lock.lock();
        try {
            if (plants.size() < plant.getMaxPerCell()) {
                plants.add(plant);
                plant.setLocation(this);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removePlant(Plant plant) {
        lock.lock();
        try {
            plants.remove(plant);
        } finally {
            lock.unlock();
        }
    }

    public List<Location> getAdjacentLocations() {
        List<Location> adjacent = new ArrayList<>();
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}}; // Only horizontal/vertical

        for (int[] dir : directions) {
            Location loc = island.getLocation(x + dir[0], y + dir[1]);
            if (loc != null) {
                adjacent.add(loc);
            }
        }
        return adjacent;
    }

    public List<Animal> getAnimals() {
        lock.lock();
        try {
            return new ArrayList<>(animals); // Return a copy for thread safety
        } finally {
            lock.unlock();
        }
    }

    public List<Plant> getPlants() {
        lock.lock();
        try {
            return new ArrayList<>(plants); // Return a copy for thread safety
        } finally {
            lock.unlock();
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
}