import lombok.Getter;

import java.util.UUID;

public class Airplane {
    @Getter
    private final String id;
    public Airplane() {
        this.id = UUID.randomUUID().toString();
    }

    public Airplane(String id) {
        this.id = id;
    }
}
