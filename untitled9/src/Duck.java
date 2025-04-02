import java.util.List;
import java.util.stream.Collectors;

public class Duck extends Herbivore {
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
