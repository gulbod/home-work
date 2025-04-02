import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public abstract class Animal extends LivingEntity {
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
